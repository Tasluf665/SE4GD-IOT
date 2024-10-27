package it.univaq.disim.se4as.coap.clients;


import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
 

public class SimpleCoapClient {

    public static void main(String[] args) {
        // Create the CoAP client with the target URI
      //  CoapClient client = new CoapClient("coap://127.0.0.1:5683/.well-known/core");
        CoapClient client = new CoapClient("coap://127.0.0.1:5684/temperature");


        try {
            // Create a GET request
            Request request = new Request(Code.GET);

            // Send the request and receive the response
            CoapResponse coapResp = client.advanced(request);

            // Check if the response is not null
            if (coapResp != null) {
                // Print the response using pretty print for better formatting
                System.out.println(Utils.prettyPrint(coapResp));
            } else {
                System.out.println("No response received.");
            }

        } catch (Exception e) {
            // Catch any exceptions that may occur and print them
            System.err.println("Error occurred while sending the request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure the client is properly shut down to release resources
            client.shutdown();
        }
    }

}
