var base64 = require('/modules/base64.js');

var log = new Log();

var SITE_PERMISSIONS = '/permissions/ues/apps/';

var SITE_METADATA = '/_system/governance/ues/site-meta/';

var USER_SITES_PATH = '/_system/config/ues/user-sites/';

var ACCESS_SITE = 'read';

var EDIT_SITE = 'edit';

var loadDefaults = function () {
    var data = {};
    data.files = [];

    data.options = {
        "archivers": {
            "create": ["application/x-tar", "application/x-gzip", "application/x-bzip2", "application/zip", "application/x-rar"],
            "extract": []
        },
        "copyOverwrite": 1,
        "disabled": ["extract"],
        "path": "My Apps",
        "separator": "/",
        "tmbUrl": "",
        "url": "/../"
    };

    return data;
};
var checkSite = function (name) {
    var site = require('/modules/site.js');
    return !site.loadSite(name);
};

var createSite = function (name, username) {
    var site = require('/modules/site.js'),
        store = require('store'),
        um = store.server.userManager(tenantId()),
        user = store.user,
        server = store.server,
        role = user.privateRole(username);
    if (site.loadSite(name)) {
        return false;
    }
    site.createSite(name, {
        "welcomeFiles": ["index.jag", "index.html"],
        "loginConfig": {
            "authMethod": "BASIC"
        },
        "securityConstraints": [
            {
                "securityConstraint": {
                    "webResourceCollection": {
                        "name": name,
                        "urlPatterns": ["/*"],
                        "methods": ["GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE", "CONNECT", "PATCH"]
                    },
                    //"authRoles": [role]
                    "users": [server.current(session).username]
                }
            }
        ]
    });
    um.authorizeRole(role, SITE_PERMISSIONS + name, EDIT_SITE);
    addUserSite(name);
    return true;
};

var addUserSite = function (name) {
    var server = require('store').server,
        registry = server.systemRegistry(tenantId());
    registry.put(USER_SITES_PATH + name, {
        collection: true
    });
};

var removeUserSite = function (name) {
    var server = require('store').server,
        registry = server.systemRegistry(tenantId());
    registry.remove(USER_SITES_PATH + name);
};

var isUserSite = function (name) {
    var server = require('store').server,
        registry = server.systemRegistry(tenantId());
    return registry.exists(USER_SITES_PATH + name);
};

var authorizeRoles = function (name, roles, action) {
    var i,
        um = require('store').server.userManager(tenantId()),
        length = roles.length;
    for (i = 0; i < length; i++) {
        um.authorizeRole(roles[i], SITE_PERMISSIONS + name, action);
    }
};

var authorizeUsers = function (name, users, action) {
    var i,
        store = require('store'),
        prefix = store.user.USER_ROLE_PREFIX,
        um = store.server.userManager(tenantId()),
        length = users.length;
    for (i = 0; i < length; i++) {
        um.authorizeRole(prefix + users[i], SITE_PERMISSIONS + name, action);
    }
};

var listSites = function () {

    var i, temp, length, items,
        site = require('/modules/site.js'),
        sites = site.listSites(),
        data = loadDefaults();
    data.cwd = {
        "date":  new Date(),
        "dirs": 1,
        "hash": "l1_Lw",
        "locked": 1,
        "mime": "directory",
        "name": "My Items",
        "read": 1,
        "size": 0,
        "ts": 1362743061,
        "volumeid": "l1_",
        "write": 1
    };

    var l2 = {
        "date": "Yesterday 17:14",
        "dirs": 1,
        "hash": "l2_Lw",
        "locked": 1,
        "mime": "directory",
        "name": "Shared Favorites",
        "read": 1,
        "size": 0,
        "ts": 1362743061,
        "volumeid": "l2_",
        "write": 1
    };

    data.files.push(data.cwd);
//    data.files.push(l2);
    sites = filterSites(sites);
    items = sites.self;
    length = items.length;
    for (i = 0; i < length; i++) {
        temp = dirData(items[i]);
        temp.phash = data.cwd.hash;
        temp.edit = true;
        data.files.push(temp);
    }
    /*    items = sites.shared.edit;
     length = items.length;
     for (i = 0; i < length; i++) {
     temp = dirData(items[i]);
     temp.phash = l2.hash;
     temp.edit = true;
     data.files.push(temp);
     }*/
    /*items = sites.shared.access;
     length = items.length;
     for (i = 0; i < length; i++) {
     temp = dirData(items[i]);
     temp.phash = l2.hash;
     temp.edit = false;
     data.files.push(temp);
     }*/

    data.api = "2.0";
    data.uplMaxSize = "2M";

    return data;
};

var listFiles = function (path) {

    path = decode(path);

    if (path == '/') {
        return listSites();
    } else {
        var i, temp,
            site = require('/modules/site.js'),
            data = loadDefaults(),
            o = parsePath(path),
            files = site.listFiles(o.site, o.path),
            length = files.length;

        data.cwd = dirData(site.loadFile(o.site, o.path), path);

        data.cwd.phash = 'l1_' + encodePath(o.path);

        data.files = [];
        for (i = 0; i < length; i++) {
            if (!(/\.swp$/.test(files[i].getName()))) {
                temp = fileData(files[i]);
                temp.phash = data.cwd.hash;
                data.files.push(temp);
            }
        }

        data.options.path = 'My Apps/' + path.replace(/[^a-z0-9\s+\/]/gi, '');

        return data;
    }

};

//TODO: avoid path var
var dirData = function (dir, path) {

    //super hack to work in windows
    //TODO: Refactor this!
    var f = filterPath(dir.getPath());
    f = f.indexOf('/') == 0 ? f.substr(1) : f;
    path = path || f;

    return {
        "date": new Date(),
        "dirs": dir.listFiles().length > 0 ? 1 : 0,
        "hash": "l1_" + encodePath(path),
        "locked": 1,
        "mime": "directory",
        "name": dir.getName().replace(/[^a-z0-9\s+\/\.-]/gi, ''),
        "read": 1,
        "size": dir.listFiles().length > 1 ? dir.listFiles().length + ' items'
            : dir.listFiles().length + ' item',
        "ts": dir.getLastModified() / 1000,
        "write": 1,
        "url": "/" + path
    };
};

var fileData = function (file) {

    var path = filterPath(file.getPath()), mime = require('/modules/mime.js');

    path = path.substr(1);

    return {
        "date": new Date(),
        "hash": "l1_" + encodePath(path),
        "mime": file.isDirectory() ? 'directory' : mime.getType(path),
        "name": file.getName(),
        "read": 1,
        "size": file.isDirectory() ? (file.listFiles().length > 1 ? file.listFiles().length + ' items'
            : file.listFiles().length + ' item')
            : file.getLength(),
        "ts": file.getLastModified() / 1000,
        "write": 1,
        "dirs": 1,
        "url": "/" + path
    };
};

var parsePath = function (path) {

    var site, p, index = path.substring(0).indexOf('/');

    if (index === -1) {
        p = '/';
        site = path.substring(0);
    } else {
        p = path.substring(index);
        site = path.substring(0, index);
    }

    return {
        site: site,
        path: p
    };

    /*
     return{
     site: path,
     path: '/'
     };
     */
};

/**
 * Filter out a given path by removing SITES_HOME path.
 * @param path
 * @return {String}
 */
var filterPath = function (path) {
    var site = require('/modules/site.js');
    return path.substring(site.appsRoot().length);
};

var encodePath = function (path) {
    //TODO: there is an issue with this regex. i.e. it removes all '_' like chars in the filename
    return base64.encode(path.replace(/[^.a-z0-9\s+\/\- ]|\.\./gi, '')).replace(/[^A-Za-z0-9]/g, '');

};


var openTree = function (path) {
    var files = listFiles(path).files;

    return {tree: files};
}

var decode = function (path) {
    return base64.decode(path.substr(3)).replace(/[^.a-z0-9\s+\/\- ]|\.\./gi, '');
};

var listFilesForSite = function (path) {
    var decoded_path = decode(path);

    var list = listFiles(path);
    list.files.push({
        "date": "Yesterday 17:10",
        "hash": "l1_" + encodePath(decoded_path),
        "mime": "directory",
        "name": decoded_path,
        "read": 1.0,
        "size": 1558,
        "ts": 1362742858,
        "write": 1.0,
        "dirs": 1.0
    });
    return list;
};


var mkdir = function (path, name) {
    var site = require('/modules/site.js');
    var decoded_path = decode(path);
    var parsedPath = parsePath(decoded_path);
    var fullPath = parsedPath.path + ( parsedPath.path.slice(-1) == '/' ? '' : '/') + name;
    var dir = site.createDir(parsedPath.site, fullPath);
    var data = fileData(dir);
    data.phash = path;
    return {'added': [data]};
};

var mkfile = function (path, name) {
    var site = require('/modules/site.js');
    var parsedPath = parsePath(decode(path));
    var fullPath = parsedPath.path + ( parsedPath.path.slice(-1) == '/' ? '' : '/') + name;
    var file = site.createFile(parsedPath.site, fullPath);
    var data = fileData(file);
    data.phash = path;
    return {'added': [data]};
};

var copy = function (src, dest, tgts) {
    var added = [];

    if (tgts instanceof Array) {
        for (var target in tgts) {
            var data = copyEach(src, dest, tgts[target]);
            added.push(data);
        }
    } else {
        var data = copyEach(src, dest, tgts);
        added.push(data);
    }
    return {'added': added};
};

var cut = function (src, dest, tgts) {
    var added = [];

    if (tgts instanceof Array) {
        for (var target in tgts) {
            var data = copyEach(src, dest, tgts[target]);
            added.push(data);
        }
    } else {
        var data = copyEach(src, dest, tgts);
        added.push(data);
    }
    var rem = remove(tgts);
    return {'added': added,
        'removed': rem.removed};
};

var copyEach = function (src, dest, tgts) {

    var site = require('/modules/site.js');
    //var source = parsePath(decode(src));
    var dst = parsePath(decode(dest));
    var targets = parsePath(decode(tgts));

    var srcFile = site.loadFile(targets.site, targets.path);

    site.copyDir(srcFile, dst.site, dst.path);

    var file = site.loadFile(dst.site, dst.path + '/' + srcFile.getName());
    var data = fileData(file);
    data.phash = dest;

    return data;
};

var duplicate = function (tgts) {
    var site = require('/modules/site.js');
    //var source = parsePath(decode(src));
    var path = decode(tgts);
    var targets = parsePath(decode(tgts));

    var srcFile = site.loadFile(targets.site, targets.path);
    site.duplicate(srcFile, targets.site);

    var destfPath = site.filterPath(targets.site, srcFile.getPath()).substr(0, site.filterPath(targets.site, srcFile.getPath()).lastIndexOf('/')) + '/copy-of-' + srcFile.getName();
    var file = site.loadFile(targets.site, destfPath);
    var data = fileData(file);
    data.phash = "l1_" + encodePath(path.substr(0, path.lastIndexOf('/')));
    return {'added': [data]};
};

var size = function (tgts) {
    var site = require('/modules/site.js');
    //var source = parsePath(decode(src));
    var path = decode(tgts);
    var targets = parsePath(decode(tgts));

    var file = site.loadFile(targets.site, targets.path);

    return {'size': file.isDirectory() ? (file.listFiles().length > 1 ? file.listFiles().length + ' items'
        : file.listFiles().length + ' item')
        : file.getLength()};

};

var rename = function (hash, name) {
    var site = require('/modules/site.js'),
        path = decode(hash),
        parentPath = path.substr(0, path.lastIndexOf('/'));

    var parsedPath = parsePath(path);

    var newPath = parsedPath.path.substr(0, parsedPath.path.lastIndexOf('/')) + '/' + name;

    var file = site.moveFile(parsedPath.site, parsedPath.path, newPath);

    var f = site.loadFile(parsedPath.site, newPath);

    var data = {};

    if (f.isDirectory()) {
        data = {
            "added": [
                {
                    "mime": "directory",
                    "ts": 1365153901,
                    "read": 1,
                    "write": 1,
                    "size": 0,
                    "url": '/' + parentPath + newPath,
                    "hash": 'l1_' + base64.encode(newPath).replace(/[^a-z0-9\s+\/]/gi, ''),
                    "name": name,
                    "phash": 'l1_' + base64.encode(parentPath).replace(/[^a-z0-9\s+\/]/gi, '')
                }
            ],
            "removed": [hash]
        };
    } else {
        data = {
            "added": [
                {
                    "mime": "text\/plain",
                    "ts": 1365153901,
                    "read": 1,
                    "write": 1,
                    "size": 0,
                    "url": '/' + parentPath + newPath,
                    "hash": 'l1_' + base64.encode(newPath).replace(/[^a-z0-9\s+\/]/gi, ''),
                    "name": name,
                    "phash": 'l1_' + base64.encode(parentPath).replace(/[^a-z0-9\s+\/]/gi, '')
                }
            ],
            "removed": [hash]

        };
    }

    if (file) {
        return data;
    }

    return false;

};

var remove = function (tgts) {
    var removed = [];

    if (tgts instanceof Array) {
        for (var target in tgts) {
            var data = removeEach(tgts[target]);
            removed.push(data);
        }
    } else {
        var data = removeEach(tgts);
        removed.push(data);
    }
    return {'removed': removed};
};

var removeEach = function (paths) {

    var site = require('/modules/site.js'),
        parsedPath = parsePath(decode(paths));

    site.removeFile(parsedPath.site, parsedPath.path);

    return paths;
};

/*
 * { "assets" : { "site" : {}, "gadget" : {} }}
 * */
var filterSites = function (sites) {
    var i, site, permission, sitez, name, admin,
        store = require('store'),
        server = store.server,
        registry = server.systemRegistry(tenantId()),
        user = store.user,
        space = require('/modules/portal.js').storeSpace(),
        assets = '{}', //space.get('userAssets'), //TODO
        u = server.current(session), //TODO
        um = server.userManager(tenantId()),
        role = user.privateRole(u.username),
        length = sites.length,
        self = [],
        edit = [],
        access = [];
    assets = assets ? parse(assets) : {};
    sitez = assets.site ? assets.site : [];
    admin = u.hasRoles([require('/portal.js').config().adminRole]);
    for (i = 0; i < length; i++) {
        site = sites[i];
        name = site.getName();
        permission = SITE_PERMISSIONS + name;
        if (sitez[siteId(name)]) {
            if (u.isAuthorized(permission, EDIT_SITE)) {
                if (um.isAuthorized(role, permission, EDIT_SITE)) {
                    self.push(site);
                } else {
                    edit.push(site);
                }
            } else if (um.isAuthorized(role, permission, EDIT_SITE)) {
                self.push(site);
            } else if (u.isAuthorized(permission, ACCESS_SITE)) {
                access.push(site);
            }
        } else if (um.isAuthorized(role, permission, EDIT_SITE)) {
            self.push(site);
        } else if (admin && !isUserSite(name)) {
            self.push(site);
        }
    }
    return {
        self: self,
        shared: {
            access: access,
            edit: edit
        }
    };
};

var siteId = function (name) {
    var meta,
        server = require('store').server,
        registry = server.systemRegistry(tenantId()),
        path = SITE_METADATA + name;
    if (!registry.exists(path)) {
        return null;
    }
    meta = parse(registry.content(path).toString());
    return meta.aid;
};

var tenantId = function() {
    return require('/modules/site.js').tenantId();
};
