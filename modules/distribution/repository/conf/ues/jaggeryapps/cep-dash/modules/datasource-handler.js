function getDatasourceHandler(datasource) {

    var datasourceHandler = '';
    switch (datasource) {
        case 'RDBMS':
        {
            datasourceHandler = 'rdbms-handler.js';
            break;
        }
        default:
            break;
    }

    return datasourceHandler;
}