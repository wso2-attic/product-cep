/**
 * Sample usage pattern for the 'site' module.
 *
 * var site = require('/modules/site.js');
 * if (!site.loadSite('demo')) {
 *    site.createSite('demo', {
 *        "welcomeFiles": ["index.jag"],
 *        "logLevel": "info"
 *    });
 * }
 * site.createFile('demo', 'index.jag', '<\% print("<h2>Hello Ruchira!</h2>"); %\>');
 *
 */


/**
 * Jaggery apps directory relative to the carbon.home.
 * @type {string}
 */
var JAGGERY_APPS_DIR = 'repository/deployment/server/jaggeryapps';

/**
 * Jaggery config file.
 * @type {string}
 */
var JAGGERY_CONF = '/jaggery.conf';

var tenantId = function () {
    //TODO: this is due to a bug in carbon.server.tenantId() method. Need to replace this after that's fixed.
    return org.wso2.carbon.context.PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
};

/**
 * File system path for the Jaggery apps directory.
 */
var appsRoot = function () {
    var process = require('process');
    var tid = tenantId();
    var carbon = require('carbon');
    var home = process.getProperty('carbon.home');
    home = home.replace(/[\\]/g, '/').replace(/^[\/]/g, '');
    return 'file:///' + home + '/' + (tid == carbon.server.superTenant.tenantId ?
        'repository/deployment/server/jaggeryapps' : 'repository/tenants/' + tid + '/jaggeryapps');
};

/**
 * Build the site path by site name.
 * @param name
 * @return {string}
 */
var sitePath = function (name) {
    return appsRoot() + '/' + name;
};

/**
 * Builds Jaggery config path by site name.
 * @param name
 * @return {string}
 */
var configPath = function (name) {
    return sitePath(name) + JAGGERY_CONF;
};

/**
 * Builds the File path for the given file in specified site.
 * @param name
 * @param file
 * @return {string}
 */
var filePath = function (name, file) {
    return sitePath(name) + file;
};

/**
 * Loads site directory as a File object or null when a site cannot be found.
 * @param name
 * @return {File}
 */
var loadSite = function (name) {
    var file = new File(sitePath(name));
    return file.isExists() ? file : null;
};

/**
 * Loads Jaggery conf as a File object or null when a conf file cannot be found.
 * @param name
 * @return {File}
 */
var loadConfig = function (name) {
    var file = new File(configPath(name));
    return file.isExists() ? file : null;
};

/**
 * Lists available sites in the sites directory.
 * @return {*}
 */
var listSites = function () {
    var file = new File(appsRoot());
    return file.isExists() ? file.listFiles() : [];
};

/**
 * Creates a site with the specified name and Jaggery configuration.
 * @param name
 * @param configs
 */
var createSite = function (name, configs) {
    var p = sitePath(name),
        file = new File(p);
    if (file.isExists()) {
        file.del();
    }
    file.mkdir();
    if (configs) {
        createConfig(name, configs);
    }
};

/**
 * Removes the specified site.
 * @param name
 * @return {Boolean}
 */
var removeSite = function (name) {
    var dir = loadSite(name);
    return dir ? dir.del() : true;
};

/**
 * Creates a Jaggery conf in the specified site.
 * @param name
 * @param config
 */
var createConfig = function (name, config) {
    var file = loadConfig(name) || new File(configPath(name));
    file.open('w');
    file.write(stringify(config));
    file.close();
};

/**
 * Remvoes the Jaggery conf in the specified site.
 * @param name
 * @return {*|Boolean}
 */
var removeConfig = function (name) {
    var dir = loadConfig(name);
    return dir ? dir.del() : true;
};

/**
 * Creates a file in the specified site with the given name. Optional content can also be passed.
 * @param site
 * @param file
 * @param content
 */
var createFile = function (site, file, content) {
    return updateFile(site, file, content || '');
};

/**
 * Creates a directory in the specified site.
 * @param site
 * @param dir
 */
var createDir = function (site, dir) {
    var p = sitePath(site),
    file = new File(p + dir);
    file.mkdir();
    return file;
};

/**
 * copy dir structure from a source to a destination
 * @param site
 * @param path
 */
var createDirStructure = function (site, path) {
    var parts = path.split('/');
    var dirPath = '';
    for (var i = 0; i < parts.length - 1; i++) {
        var part = parts[i];
        dirPath = dirPath + '/' + part;
        try {
            createDir(site, dirPath);
        } catch (e) {
            // ASSUME: Error is due to dir already existing, thus ignore
        }
    }
}


/**
 * Removes the given directory from the specified site.
 * @param site
 * @param dir
 */
var removeDir = function (site, dir) {
    removeFile(site, dir);
};

/**
 * Updates the given file with the specified content.
 * @param site
 * @param file
 * @param content
 */
var updateFile = function (site, file, content) {
    var s = loadSite(site);
    if (!s) {
        throw new Error('Specified site "' + site + '" does not exists');
    }
    var f = new File(filePath(site, file));
    f.open('w');
    f.write(content);
    f.close();
    return f;
};

/**
 * Removes the given file from the specified site.
 * @param site
 * @param file
 */
var removeFile = function (site, file) {
    var f = new File(filePath(site, file));
    return f.isExists() ? f.del() : true;
};

/**
 * Loads a file from the given site.
 * @param site
 * @param file
 * @return {File}
 */
var loadFile = function (site, file) {
    var f = new File(filePath(site, file));
    if (!f) {
        throw new Error('Specified file "' + file + '" cannot be found');
    }
    return f;
};

/**
 * Check the existence of the file from the given site.
 * @param site
 * @param file
 * @return {File}
 */
var isFileExists = function (site, file) {
    var exists = true;
    var f = new File(filePath(site, file));
    if (!f.isExists()) {
        exists = false;
    }
    return exists;
};

/**
 * Lists all the files available in the given directory.
 * @param site
 * @param path
 * @return {Array}
 */
var listFiles = function (site, path) {
    var files, length, i,
        paths = [],
        p = sitePath(site),
        f = new File(p + path);
    if (!f) {
        return paths;
    }
    files = f.listFiles();
    length = files.length;
    path += (path === '/') ? '' : '/';
    for (i = 0; i < length; i++) {
        paths[i] = new File(filePath(site, path + files[i].getName()));
    }
    return paths;
};

/**
 * Moves the given file into the specified file.
 * @param site
 * @param file
 * @param dest
 */
var moveFile = function (site, file, dest) {
    var f = loadFile(site, file);
    return f.move(filePath(site, dest));
};

/**
 * Filter out a given path by removing appsRoot path.
 * @param site
 * @param path
 * @return {String}
 */
var filterPath = function (site, path) {
    return path.substring(appsRoot().length + 1 + site.length);
};

/**
 * Copy file from a source loc to a distination
 * @param src
 * @param dstSite
 * @param dst
 */
var copyFile = function (src, dstSite, dst) {
    var stream = src.getStream();
    dst = dst + '/' + src.getName();
    var dstf = loadFile(dstSite, dst);
    dstf.open('w');
    dstf.write(stream);
    dstf.close();
};

/**
 * copy dirs from a source to a destination
 * @param src
 * @param dstSite
 * @param dst
 */
var copyDir = function (src, dstSite, dst) {
    if (src.isDirectory()) {
        createDir(dstSite, dst + '/' + src.getName());
        dst = dst + '/' + src.getName();
        var srcFiles = src.listFiles();
        for (var i = 0; i < srcFiles.length; i++) {
            var inFile = srcFiles[i];
            copyDir(inFile, dstSite, dst);
        }
    } else {
        copyFile(src, dstSite, dst);
    }
};

/**
 * copy dir structure from a source to a destination
 * @param site
 * @param path
 */
/*
 var createDirStructure = function (site, path) {
 var parts = path.split('/');
 var dirPath = '';
 for (var i = 0; i < parts.length - 1; i++) {
 var part = parts[i];
 dirPath = dirPath + '/' + part;
 try {
 createDir(site, dirPath);
 } catch (e) {
 // ASSUME: Error is due to dir already existing, thus ignore
 }
 }
 }
 */

/**
 * Duplicates a dir/file
 * @param src
 * @param srcSite
 */
var duplicate = function (src, srcSite) {
    if (src.isDirectory()) {
        var destPath = filterPath(srcSite, src.getPath()).substr(0, filterPath(srcSite, src.getPath()).lastIndexOf('/')) + '/copy-of-' + src.getName();
        createDir(srcSite, destPath);
        var srcFiles = src.listFiles();
        for (var i = 0; i < srcFiles.length; i++) {
            var inFile = srcFiles[i];
            copyDir(inFile, srcSite, destPath);
        }

    } else {
        var destfPath = filterPath(srcSite, src.getPath()).substr(0, filterPath(srcSite, src.getPath()).lastIndexOf('/')) + '/copy-of-' + src.getName();
        var stream = src.getStream();
        var dstf = loadFile(srcSite, destfPath);
        dstf.open('w');
        dstf.write(stream);
        dstf.close();
    }
};