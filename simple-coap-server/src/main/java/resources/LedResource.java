package resources;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.BAD_REQUEST;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CHANGED;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class LedResource extends CoapResource {
	String id;
	boolean state;
	String reply="off";
    
	
	
	public LedResource(String id) {
        // resource identifier
	    super(id);
	    this.id=id;
 	   
	    // set display name
        getAttributes().setTitle(id);
    }
    
	@Override
    public void handleGET(CoapExchange exchange) {
           exchange.respond("State of "+id+" led is "+reply);
    }
	
       
    @Override
    public void handlePUT(CoapExchange exchange) {
        byte[] payload = exchange.getRequestPayload();
	    String putstr;
	    try {
               putstr = new String(payload, "UTF-8");
               state=putstr.equals("on") || putstr.equals("1");	
		
               reply=state?"on":"off";
               
               exchange.respond(CHANGED, "request payload:"+putstr);
        } catch (Exception e) {
               e.printStackTrace();
               exchange.respond(BAD_REQUEST, "Invalid input");
        }
    }
    
    
 
    
}