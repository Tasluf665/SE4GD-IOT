package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;

import resources.HelloWorldResource;
import resources.LedResource;
import resources.ObservableResource;
import resources.TemperatureResource;

public class SimpleCoapServer {

    public static void main(String[] args) {
        // Get the property file path from the system property
        String propertiesFilePath = System.getProperty("config.file", "Californium3.properties");

        // Load configuration from the properties file
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(propertiesFilePath)) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println("Error reading properties file: " + e.getMessage());
            return;
        }
         
        String ipAddress = properties.getProperty("server.ip", "0.0.0.0");
        int port = Integer.parseInt(properties.getProperty("server.port", "5683"));
        
        
        // Create a new CoAP server
        CoapServer server = new CoapServer();
        
        InetSocketAddress socket = new InetSocketAddress(ipAddress, port);
        CoapEndpoint endpoint = new CoapEndpoint.Builder()
                .setInetSocketAddress(socket)
                .build();
            server.addEndpoint(endpoint);

        System.out.println("CoAP server created with endpoint " + ipAddress + ":" + port);

        if (Boolean.parseBoolean(properties.getProperty("resource.hello-world.enabled", "true"))) {
            HelloWorldResource hello = new HelloWorldResource("hello-world-resource");
            server.add(hello);
            System.out.println("Added HelloWorldResource");
        }

        if (Boolean.parseBoolean(properties.getProperty("resource.temperature.enabled", "true"))) {
            TemperatureResource temperature = new TemperatureResource("temperature");
            server.add(temperature);
            System.out.println("Added TemperatureResource");
        }

        if (Boolean.parseBoolean(properties.getProperty("resource.led.enabled", "true"))) {
            LedResource ledpath = new LedResource("Leds");
            ledpath.add(new LedResource("red"));    // "leds/red"
            ledpath.add(new LedResource("green"));  // "leds/green"
            ledpath.add(new LedResource("blue"));   // "leds/blue"
            server.add(ledpath);
            System.out.println("Added LedResource");
        }

        if (Boolean.parseBoolean(properties.getProperty("resource.observable.enabled", "true"))) {
            ObservableResource obsRes = new ObservableResource("observable-resource");
            obsRes.setObservable(true);
            obsRes.getAttributes().setObservable();
            server.add(obsRes);
            System.out.println("Added ObservableResource");
        }

			

        // Start the server
        server.start();

        System.out.println("CoAP server is running...");
    }

   
}
