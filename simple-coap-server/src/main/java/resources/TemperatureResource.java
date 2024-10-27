package resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;


public class TemperatureResource extends CoapResource {
   
	public TemperatureResource(String name) {
        // resource identifier
        super(name);
        // set display name
        getAttributes().setTitle("Temperature Resource");
    }

    @Override
    public void handleGET(CoapExchange exchange) {
	    double t=18+Math.round(Math.random()*12);  //random value in range:18-30
	        exchange.respond(""+t);
	    }
}