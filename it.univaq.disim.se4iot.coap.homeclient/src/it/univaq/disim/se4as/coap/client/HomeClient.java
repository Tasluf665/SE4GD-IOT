package it.univaq.disim.se4as.coap.client;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;

public class HomeClient extends CoapClient {

	public static void main(String[] args) {

	//	CoapClient client = new CoapClient("coap://127.0.0.1:5683/Leds/blue");
		CoapClient kitchen = new CoapClient("coap://127.0.0.1:5684/temperature");
		CoapClient livingroom = new CoapClient("coap://127.0.0.1:5685/temperature");

	//	CoapClient client = new CoapClient("coap://127.0.0.1:5683/.well-known/core");
		
		Request request = new Request(Code.GET);
	
		CoapResponse coapResp = kitchen.advanced(request);
		System.out.println(Utils.prettyPrint(coapResp));
		
		coapResp = livingroom.advanced(request);
		System.out.println(Utils.prettyPrint(coapResp));
		
	}

}
