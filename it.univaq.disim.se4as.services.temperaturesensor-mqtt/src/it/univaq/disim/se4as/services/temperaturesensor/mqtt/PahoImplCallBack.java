package it.univaq.disim.se4as.services.temperaturesensor.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import it.univaq.disim.se4as.services.sensor.api.Data;

public class PahoImplCallBack implements MqttCallback  {

	MqttClient client;
	Data data;
	
	public PahoImplCallBack(Data data) {
		this.data = data;
	}
	
	@Override
	public void connectionLost(Throwable arg0) {
		System.out.println("The connection with the server is lost. !!!!");
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		System.out.println("The delivery has been complete. The delivery token is " + arg0.toString());
		
	}

	@Override
	public void messageArrived(String arg0, MqttMessage message) throws Exception {
		System.out.println("A new message arrived from the topic: \"" + arg0 + "\". The payload of the message is " + message.toString());	
		Integer i = new Integer(message.toString());
		//Integer i = new Integer(message.toString());
		//System.out.println("Valore di i "+ i);
		//System.out.println("**************" + i.toString());
		data.setVal(i);
	}
	

	public void subscribe() {
		try {
			client = new MqttClient("tcp://localhost:1883", "pahomqttpublish1");
			client.setCallback(this);
			client.connect();
			client.subscribe("home/outsidetemperature");
		} catch (MqttException e) {
		      e.printStackTrace();
		}
	  }
}
