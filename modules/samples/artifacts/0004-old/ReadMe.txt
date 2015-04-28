This sample uses 
InputEventAdaptor:  email
EventBuilder:           text
EventFormatter:         json
OutputEventAdaptor: jms

Producers:
    to:	        wso2cep.demo@gmail.com
    subject:	SOAPAction:urn:ServiceManagement
    content:    Subscribe PizzaOffers
                to Johan for 20 days

Consumers:     jms

For running JMS samples, please refer http://docs.wso2.org/display/CEP310/Setting+up+CEP+samples