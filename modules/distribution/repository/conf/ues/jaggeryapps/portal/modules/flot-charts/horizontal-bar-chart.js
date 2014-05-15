function processData(dbResult, mapping, dataColumns,dataLabels,timeSeries) {

    var result = [];
    var ticks = [];
    var tickTmp = [];
    var output = {};

    for (var i = 0, k = 0; i < dbResult.length; i++) {
        for (var j = 0; j < mapping.length; j++) {
            if (!result[j]) {
                result[j] = [];
            }

            if (tickTmp[dbResult[i][mapping[j][dataColumns[0]]]]) {
                var keyVal = tickTmp[dbResult[i][mapping[j][dataColumns[0]]]];
                result[j].push([dbResult[i][mapping[j][dataColumns[1]]], keyVal]);

            } else {
                tickTmp[dbResult[i][mapping[j][dataColumns[0]]]] = k + 1;
                ticks.push([k + 1, dbResult[i][mapping[j][dataColumns[0]]]]);
                result[j].push([dbResult[i][mapping[j][dataColumns[1]]], k + 1]);
                k++;
            }
        }
    }

    for (var j = 0; j < mapping.length; j++) {
        var tmpObj = {};
        tmpObj["label"] = mapping[j]["Series Label"];
        tmpObj["data"] = result[j];
        output["series" + j] = tmpObj;
    }

    var chartOptions = require("chart-options.json");
    chartOptions["yaxis"]["ticks"]= ticks;
    chartOptions.xaxis.axisLabel= dataLabels["X-Axis Label"]
    chartOptions.yaxis.axisLabel= dataLabels["Y-Axis Label"]

    return {0: output, 1: chartOptions};
}
