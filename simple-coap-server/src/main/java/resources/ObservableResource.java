package resources;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class ObservableResource extends CoapResource {

	private int mValue = 0;
	
	public ObservableResource(String name) {
		super(name);
		
		Timer timer = new Timer();
		timer.schedule(new UpdateTask(this), 0, 1000);
		
	}

	
	@Override
    public void handleGET(CoapExchange exchange) {
           exchange.respond(ResponseCode.CONTENT,
        		   mValue+"",
        		   MediaTypeRegistry.TEXT_PLAIN);
    }
	
	private class UpdateTask extends TimerTask {
		private CoapResource mCoapRes;
		public UpdateTask(CoapResource coapRes) {
			mCoapRes = coapRes;
		}
		
		@Override
		public void run() {
			mValue = new Random().nextInt(20);
			mCoapRes.changed();
		}
	} 
}
