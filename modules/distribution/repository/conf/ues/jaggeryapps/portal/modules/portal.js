var PORTAL_OPTIONS = 'portal.options';

var STORE_SPACE = 'portal.store.space';

var TENANT_PORTAL = 'tenant.store';

var PORTAL_CONFIG_PATH = '/_system/config/store/configs/portal.json';

var init = function (options) {
    //addRxtConfigs(tenantId);
    var event = require('event');

    event.on('tenantCreate', function (tenantId) {
        var role, roles,
            carbon = require('carbon'),
            mod = require('store'),
            server = mod.server,
            config = require('/portal-tenant.json'),
            system = server.systemRegistry(tenantId),
            um = server.userManager(tenantId);
        system.put(options.tenantConfigs, {
            content: JSON.stringify(config),
            mediaType: 'application/json'
        });
        roles = config.roles;
        for (role in roles) {
            if (roles.hasOwnProperty(role)) {
                if (um.roleExists(role)) {
                    um.authorizeRole(role, roles[role]);
                } else {
                    um.addRole(role, [], roles[role]);
                }
            }
        }
    });
};

/**
 * This is a util method to get the store instance.
 * @param o This can be either the tenantId or the request object.
 * @param session
 * @return {*}
 */
var portal = function (o, session) {
    var user, portal, configs, tenantId,
        carbon = require('carbon'),
        mod = require('store'),
        server = mod.server,
        cached = server.options().cached;

    tenantId = (o instanceof Request) ? server.tenant(o, session).tenantId : o;
    user = server.current(session);
    if (user) {
        portal = session.get(TENANT_PORTAL);
        if (cached && portal) {
            return portal;
        }
        portal = new Portal(tenantId, session);
        session.put(TENANT_PORTAL, portal);
        return portal;
    }
    configs = server.configs(tenantId);
    portal = configs[TENANT_PORTAL];
    if (cached && portal) {
        return portal;
    }
    portal = new Portal(tenantId);
    configs[TENANT_PORTAL] = portal;
    return portal;
};

var Portal = function (tenantId, session) {
    var assetManagers = {},
        mod = require('store'),
        user = mod.user,
        server = mod.server;
    this.tenantId = tenantId;
    this.servmod = server;
    this.assetManagers = assetManagers;
    if (session) {
        this.user = server.current(session);
        this.registry = user.userRegistry(session);
        this.session = session;
        this.userSpace = user.userSpace(this.user);
    }
};

var configs = function (tenantId) {
    var server = require('store').server,
        registry = server.systemRegistry(tenantId);
    return JSON.parse(registry.content(PORTAL_CONFIG_PATH));
};

var options = function () {
    return application.get(PORTAL_OPTIONS);
};

var register = function (user, password, session) {

};

var login = function (user, password, session) {
    var opts = options(),
        carbon = require('carbon');
    session.put(STORE_SPACE, new carbon.user.Space(user.username, opts.storeSpace.space, opts.storeSpace.options));
};

var logout = function (user, session) {
    session.remove(STORE_SPACE);
};

var storeSpace = function () {
    return session.get(STORE_SPACE);
};

var exec = function (fn, request, response, session) {
    var log = new Log(),
        es = require('store'),
        carbon = require('carbon'),
        tenant = es.server.tenant(request, session),
        user = es.server.current(session);
    es.server.sandbox({
        tenantId: tenant.tenantId,
        username: user ? user.username : carbon.user.anonUser
    }, function () {
        var configs = require('/portal.js').config();
        return fn.call(null, {
            tenant: tenant,
            server: es.server,
            sso: configs.ssoConfiguration.enabled,
            usr: es.user,
            user: user,
            portal: require('/modules/portal.js').portal(tenant.tenantId, session),
            configs: configs,
            request: request,
            response: response,
            session: session,
            application: application,
            event: require('event'),
            params: request.getAllParameters(),
            //files: request.getAllFiles(),
            matcher: new URIMatcher(request.getRequestURI()),
            //site: require('/modules/site.js'),
            log: new Log(request.getMappedPath())
        });
    });
};