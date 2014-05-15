var getType = function (path) {
    var index = path.lastIndexOf('.');
    var ext = index < path.length ? path.substring(index + 1) : '';
    
    (index==-1) && (ext = 'text');
    
    switch (ext) {
        case 'js':
            return 'application/javascript';
        case 'css':
            return 'text/css';
        case 'jag':
            return 'application/jaggery';
        case 'html':
            return 'text/html';
        case 'png':
            return 'image/png';
        case 'gif':
            return 'image/gif';
        case 'xml':
            return 'text/xml';
        case 'jpeg':
            return 'image/jpeg';
        case 'jpg':
            return 'image/jpg';
        case 'json':
            return 'application/json';
        default :
            return 'text/plain';
    }
};