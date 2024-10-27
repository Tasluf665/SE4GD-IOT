package it.univaq.disim.se4as.coap.server.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class TemperatureResourceMQTT extends CoapResource implements MqttCallback {
	
	 MqttClient client;
	 String temperature;

   
	public TemperatureResourceMQTT(String name) {
        // resource identifier
        super(name);
        // set display name
        getAttributes().setTitle("Living Room Temperature Resource");
        this.subscribe();
    }

	
    @Override
    public void handleGET(CoapExchange exchange) {
	        exchange.respond("Current Living Room Temperature is:"+temperature);
	    }
    
    
    public void subscribe() {
		    try {
		      client = new MqttClient("tcp://localhost:1883", "pahomqttpublish12");
		      
		      client.setCallback(this);
		      client.connect();
		      client.subscribe("/home/livingroom/temperature");
		      
		    } catch (MqttException e) {
		      e.printStackTrace();
		    }
		  }
    
	@Override
	public void connectionLost(Throwable arg0) {
		System.out.println("The connection with the server is lost. !!!!");
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		System.out.println("The delivery has been complete. The delivery token is " + arg0.toString());
		
	}

	@Override
	public void messageArrived(String arg0, MqttMessage message) throws Exception {
		temperature = message.toString();		
	}
	
}