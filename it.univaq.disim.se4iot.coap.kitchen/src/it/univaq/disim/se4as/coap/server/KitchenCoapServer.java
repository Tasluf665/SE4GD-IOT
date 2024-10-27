package it.univaq.disim.se4as.coap.server;

import org.eclipse.californium.core.CoapServer;

import it.univaq.disim.se4as.coap.server.resources.TemperatureResource;

public class KitchenCoapServer extends CoapServer{
	
	public static void main(String args[]) {
		KitchenCoapServer server = new KitchenCoapServer();
		
		TemperatureResource temperature = new TemperatureResource("temperature");
		server.add(temperature);

		server.start();
		
	}
	

}
