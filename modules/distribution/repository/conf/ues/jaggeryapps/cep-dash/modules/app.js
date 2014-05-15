var init = function(options) {
    //addRxtConfigs(tenantId);
    var event = require('event');

    event.on('tenantCreate', function (tenantId) {
        var role, roles,
            carbon = require('carbon'),
            mod = require('store'),
            server = mod.server,
            config = require('/config.json'),
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
        var configs = require('/config.json');
        return fn.call(null, {
            tenant: tenant,
            server: es.server,
            sso: configs.ssoConfiguration.enabled,
            usr: es.user,
            user: user,
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