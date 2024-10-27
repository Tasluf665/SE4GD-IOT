package it.univaq.disim.se4as.coap.server;

import org.eclipse.californium.core.CoapServer;

import it.univaq.disim.se4as.coap.server.resources.HumidityResourceMQTT;
import it.univaq.disim.se4as.coap.server.resources.TemperatureResourceMQTT;

public class LivingRoomCoapServer extends CoapServer{
	
	public static void main(String args[]) {
		LivingRoomCoapServer server = new LivingRoomCoapServer();
			 
		TemperatureResourceMQTT temperature = new TemperatureResourceMQTT("temperature");
		server.add(temperature);
 
		HumidityResourceMQTT humidity = new HumidityResourceMQTT("humidity");
		server.add(humidity);
		server.start();
		
	}
	

}
