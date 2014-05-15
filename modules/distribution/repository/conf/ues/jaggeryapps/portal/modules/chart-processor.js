function getChartProcessor(chartType) {

    var chartProcessorType = '';
    switch (chartType) {
        case 'Horizontal Bar Chart':
            chartProcessorType = 'horizontal-bar-chart.js';
            break;
        case 'Bubble Chart':
            chartProcessorType = 'bubble-chart.js'
            break;
        case 'Pie Chart':
            chartProcessorType = 'pie-chart.js'
            break;
        case 'Line Plus Bar Chart':
            chartProcessorType = 'line-plus-bar-chart.js'
            break;
        default:
            chartProcessorType = 'default-chart.js'
            break;
    }
    return chartProcessorType;
}