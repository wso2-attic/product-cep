var PUBLISHER_CACHE = 'publisher.roles.cache';

var PUBLISHER_CACHE_PERIOD = 1000 * 1000; //We cannot work with cahche here, roles and users are dynamically added

var match = function (data, query) {
    var i, item,
        length = data.length,
        items = [];
    for (i = 0; i < length; i++) {
        item = data[i];
        if (item.indexOf(query) === 0) {
            items.push({
                id: item,
                name: item
            });
        }
    }
    return items;
};

var users = function(query) {
    return match(data().users, query);
};

var roles = function (query) {
    return match(data().roles, query);
};

var data = function () {
    var t, data, server, um, roles, prefix, index, i, length, rolez, role,
        store = require('store'),
        user = store.user;
    data = application.get(PUBLISHER_CACHE);
    t = new Date().getTime();
    if (data && ((t - data.updated) > PUBLISHER_CACHE_PERIOD)) {
        return data;
    }
    server = store.server;
    um = server.userManager();
    prefix = user.USER_ROLE_PREFIX;
    index = prefix.length;

    users = [];
    rolez = [];
    roles = um.allRoles();

    length = roles.length;
    for (i = 0; i < length; i++) {
        role = roles[i];
        if (role.indexOf(prefix) === 0) {
            users.push(role.substring(index));
        } else {
            rolez.push(role);
        }
    }
    data = {
        updated: t,
        roles: rolez,
        users: users
    };
    application.put(PUBLISHER_CACHE, data);
    return data;
};

