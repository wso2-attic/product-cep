This sample uses 
InputEventAdaptor:  jms
EventBuilder:       json
EventFormatter:     map
OutputEventAdaptor: jms

Producers:     stock-quote [Topic: AllStockQuotes]
Consumers:     jms [Topic: BasicStockQuotes]

For running JMS samples, please refer http://docs.wso2.org/display/CEP310/Setting+up+CEP+samples