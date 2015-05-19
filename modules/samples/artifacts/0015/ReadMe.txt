This sample uses 
EventReceiver:  Text default mapping
EventPublisher: logger

Producers:     email
Consumers:     -

Send a mail to wso2cep400@gmail.com, with subject "sensordata".
Mail body needs to be in plain text format with below content.

meta_timestamp:19900813115534,
meta_isPowerSaverEnabled:false,
meta_sensorId:601,
meta_sensorName:temperature,
correlation_longitude:90.34344,
correlation_latitude:20.44345,
humidity:2.3,
sensorValue:20.44345

