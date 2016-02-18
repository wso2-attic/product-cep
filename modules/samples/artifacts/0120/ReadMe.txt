This sample demonstrates the usage of map extension.

Producers: http
Consumers: console (logger)

Sample curl request:

curl -i -X POST http://localhost:9763/endpoints/httpReceiver \
    -H "Content-Type: application/json" \
    --data '{"name": "Vehicle", "numOfWheels": 5, "specificAttributesObj": {"temperature": "red","carColur": "#f00"}}'