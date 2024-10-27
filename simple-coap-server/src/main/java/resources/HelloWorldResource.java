package resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class HelloWorldResource extends CoapResource {

	public HelloWorldResource(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(ResponseCode.CONTENT,
				 "Hello World!",
				 MediaTypeRegistry.TEXT_PLAIN);
	}
}
