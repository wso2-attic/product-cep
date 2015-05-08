This sample uses 
EventReceiver:  Text/http with default mapping
EventPublisher: logger

Producers:     http
Consumers:     -

ant -Dsn=0013 -Dtopic=topicMap -Dbroker=mb -Dformat=csv -DstreamId=org.wso2.event.sensor.stream:1.0.0
ant -Dsn=0013 -Dtopic=topicText -Dbroker=mb

./wso2cep-samples.sh -sn 0013 -DportOffset=5 -Dqpid.dest_syntax=BURL