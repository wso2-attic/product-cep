// *** declare public functions. ***
var create;


(function () {
    // *** private const ***

    /** @type {{ createDir:function, loadFile:function }} */
    var SITE = require('/modules/site.js');
    /** @type {{ server:{ https:string, http:string}, portalGadgets:{ store:string }}} */
    var CONFIG = require('/portal.js').config();
    /** @type {{compile:function}} */
    var HANDLEBARS = require('/modules/handlebars.js').Handlebars;
    /** @type {{encode:function}} */
    var BASE64 = require('/modules/base64.js');
    /** @type {{sso:function}} */
    var DEPLOYER = require('/modules/deployer.js');
    var TEMPLATE_DIR = '/dashboard-template/';

    var PORTAL = require('/portal.js').config();

    // *** private functions ***

    /**
     * create directories recursively.
     * eg : if path = 'a/b/m.txt', 'a' and 'a/b' will
     * be created inside the site.
     *
     * @param appName site name
     * @param path path to a file (eg: a/b/m.txt)
     */
    function createDirs(appName, path) {
        var parts = path.split('/');
        var dirPath = '';
        for (var i = 0; i < parts.length - 1; i++) {
            var part = parts[i];
            dirPath = dirPath + '/' + part;
            try {
                SITE.createDir(appName, dirPath);
            } catch (e) {
                // ASSUME: Error is due to dir already existing, thus ignore
            }
        }
    }

    function copyFile(appName, file, toPath) {
        createDirs(appName, toPath);
        var outFile = SITE.loadFile(appName, toPath);
        outFile.open('w');
        var stream = file.getStream();
        outFile.write(stream);
        stream.getStream().close();
        outFile.close();
    }

    function transformCopyFile(appName, file, toPath, layoutDef) {
        createDirs(appName, toPath);
        var outFile = SITE.loadFile(appName, toPath);
        outFile.open('w');
        file.open('r');
        var template = HANDLEBARS.compile(file.readAll());
        template = template(layoutDef).replace(/\[\[/g, '{{').replace(/\]\]/g, '}}');
        outFile.write(template);
        file.close();
        outFile.close();
    }

    function copyDir(appName, file, toPath, layoutDef) {
        if (file.isExists()) {
            if (file.isDirectory()) {
                var files = file.listFiles();
                for (var i = 0; i < files.length; i++) {
                    var subFile = files[i];
                    copyDir(appName, subFile, toPath + '/' + file.getName());
                }
            } else {
                var name = file.getName();
                if (endsWith(name, '.hbs')) {
                    transformCopyFile(appName, file, toPath + '/' + name.substr(0, name.length - 4), layoutDef);
                } else {

                    copyFile(appName, file, toPath + '/' + file.getName());
                }
            }
        }
    }

    function replaceAll(find, replace, str) {
        return str.replace(new RegExp(find, 'g'), replace);
    }

    function endsWith(str, suffix) {
        return str.indexOf(suffix, str.length - suffix.length) !== -1;
    }

    // *** public functions ***

    /**
     * create and copy dashboard source files to the given dashboard.
     *
     * @param {string} appName
     * @param {*} layoutDef
     */
    create = function (appName, layoutDef) {
        var TEMPL_FILES = new File(TEMPLATE_DIR + 'files');
        var CURRENT_THEME = (function () {
            return require('caramel').configs().themer();
        })();


        var templateDef = /** @type {{copyTheme:Array.<string>,copyRoot:Array.<string>}} */
            require(TEMPLATE_DIR + 'filelist.json');

        var toCopyThemeArr = templateDef.copyTheme;
        var toCopyRootArr = templateDef.copyRoot;


        // copy files listed in filelist.json under copyTheme
        // form current themes dir
        for (var i = 0; i < toCopyThemeArr.length; i++) {
            var fileName = toCopyThemeArr[i];
            var inputFile = new File('/themes/' + CURRENT_THEME + fileName);
            if (inputFile.isExists() && !inputFile.isDirectory()) {
                copyFile(appName, inputFile, fileName);
            }
        }

        // copy files listed in filelist.json under copyRoot
        // form portal root dir.
        for (i = 0; i < toCopyRootArr.length; i++) {
            fileName = toCopyRootArr[i];
            inputFile = new File(fileName);

            if (inputFile.isExists() && !inputFile.isDirectory()) {
                copyFile(appName, inputFile, fileName);
            }
        }

        var appName64 = BASE64.encode(appName).replace(/=/g, '');
        DEPLOYER.sso({'issuer': appName,
            'consumerUrl': PORTAL.ssoConfiguration.appAcsHost + '/' + appName + '/acs',
            'doSign': 'true',
            'singleLogout': 'true',
            'useFQUsername': 'true',
            'doSignResponse': 'true',
            'issuer64': appName64});

        layoutDef['appName'] = appName;
        layoutDef['httpsServerUrl'] = CONFIG.server.https;
        layoutDef['showAssetGadget'] = CONFIG.portalGadgets.store;
        layoutDef['showGadgetTemplates'] = CONFIG.portalGadgets.gadgetTemplates;
        layoutDef['httpServerUrl'] = CONFIG.server.http;

        // copy files in dashboard-template/files
        var files = TEMPL_FILES.listFiles();
        for (i = 0; i < files.length; i++) {
            var subFile = files[i];
           
            copyDir(appName, subFile, '', layoutDef);
        }
        
        return true;
    }
})();

