This sample uses 
InputEventAdaptor:  ws-event-local
EventBuilder:           xml
EventFormatter:         text
OutputEventAdaptor: email

Producers:     pizza-shop
    ant pizzaOrderClient -Dservice=WSEventLocalAdaptorService -Dtopic=PizzaOrder

Consumers:     email
    wso2cep.demo@gmail.com