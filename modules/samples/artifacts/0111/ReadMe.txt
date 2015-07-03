This sample demonstrates how to detect non-occurrences with patterns.

Producers: http
Consumers: http (log service)

Send package arrival events with:
ant -Durl=http://localhost:9763/endpoints/packageArrivalsHTTPReceiver -DfilePath=../../artifacts/0105/arrivalEvents.txt


Send package delivery events with:
ant -Durl=http://localhost:9763/endpoints/packageDeliveryHTTPReceiver -DfilePath=../../artifacts/0105/deliveryEvents.txt