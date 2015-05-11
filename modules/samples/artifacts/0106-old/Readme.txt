This sample is related to ws-event event adaptor

This sample uses
InputEventAdaptor:      wso2Event (Through rest api)
EventBuilder:           wso2event
EventFormatter:         text
OutputEventAdaptor:     email

Producers:  build-failure-rest-api

Consumers: email client

Note : - Use the below commands.

1) To define stream : curl -k --user admin:admin https://localhost:9443/datareceiver/1.0.0/streams/ --data @streamdefn1.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
2) To send events : curl -k --user admin:admin https://localhost:9443/datareceiver/1.0.0/stream/buildfail_Statistics/1.3.2/ --data @events1.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
