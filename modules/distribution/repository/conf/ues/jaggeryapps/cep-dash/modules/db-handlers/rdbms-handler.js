function getDataSources() {
    var isConSuccess = true;

    var dataSources = [];

    var log = new Log();
    var DataSourceManager = Packages.org.wso2.carbon.ndatasource.core.DataSourceManager;
    var mydatasourceManager = new DataSourceManager();

    var coll = mydatasourceManager.getInstance().getDataSourceRepository().getAllDataSources();
    var iterator = coll.iterator();

    while (iterator.hasNext()) {
        var dataSrc = iterator.next().getDSMInfo().getName();
        dataSources.push(dataSrc);
    }


    return dataSources;
}

function validateConnection(connectionSettings) {
    var isConSuccess = true;
    try {
        if(connectionSettings['Connection']){
            var db = new Database(connectionSettings['Connection']);
        }else{
            var db = new Database(connectionSettings['Connection URL'], connectionSettings['Username'], connectionSettings['Password']);
        }
    } catch (e) {
        isConSuccess = false;
    }
    return isConSuccess;
}

function getData(connectionSettings, sql_statement) {
    var dbResult;
    var db;
    try {
        if(connectionSettings['Connection']){
            db = new Database(connectionSettings['Connection']);
        }else{
            db = new Database(connectionSettings['Connection URL'], connectionSettings['Username'], connectionSettings['Password']);
        }
        dbResult = db.query(sql_statement['SQL Statement']);
        return dbResult;
        //if data is not in the tabular format it needs to be formatted here in other handlers

    } catch (e) {
        throw "You have an error in your SQL syntax";

    } finally {
        db.close();
    }


}
