
var getMyBookmarks = function(){
	var list = require('/modules/site.js').listSites();
    
    var sites = [];
    
    for(var i in list){
    	sites.push(list[i].getName());	
    }
    
    return sites;
    
}
