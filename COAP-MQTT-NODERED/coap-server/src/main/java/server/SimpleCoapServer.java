package server;

import org.eclipse.californium.core.CoapServer;
import resources.HelloWorldResource;
import resources.LedResource;
import resources.ObservableResource;
import resources.TemperatureResource;

public class SimpleCoapServer {

    public static void main(String[] args) {
        // Create a new CoAP server
        CoapServer server = new CoapServer();

	    HelloWorldResource hello = new HelloWorldResource("hello-world-resource");
        // Add a resource to the server
        server.add(hello);
        
        TemperatureResource temperature = new TemperatureResource("temperature");
		server.add(temperature);
        
		LedResource ledpath = new LedResource("Leds");
        ledpath.add(new LedResource("red"));	//"leds/red"
		ledpath.add(new LedResource("green"));	//"leds/green"
		ledpath.add(new LedResource("blue"));	//"leds/blue"
	    server.add(ledpath);

		ObservableResource obsRes = new ObservableResource("observable-resource");
		obsRes.setObservable(true);
		obsRes.getAttributes().setObservable();
		server.add(obsRes);
			

        // Start the server
        server.start();

        System.out.println("CoAP server is running...");
    }

   
}
