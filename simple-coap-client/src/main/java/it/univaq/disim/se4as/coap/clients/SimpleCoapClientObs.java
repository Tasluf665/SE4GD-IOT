package it.univaq.disim.se4as.coap.clients;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

public class SimpleCoapClientObs extends CoapClient {

	public static void main(String[] args) {
		CoapClient client = new CoapClient("coap://127.0.0.1:5683/observable-resource");

		client.observe(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				System.out.println(response.getResponseText());
			}

			@Override
			public void onError() {
				System.err.println("An error occurred");
			}
		});
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
