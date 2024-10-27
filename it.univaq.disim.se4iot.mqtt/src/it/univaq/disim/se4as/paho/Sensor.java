package it.univaq.disim.se4as.paho;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


class Sensor implements Callable<Boolean> {
    String city;
    String station;
    String pollutant;
    String topic;
    private static DecimalFormat decimalFormat = new DecimalFormat(".00");
    		
    Sensor(String city, String station, String pollutant, String topic) {
        this.city = city;
        this.station = station;
        this.pollutant = pollutant;
        this.topic = topic;
    }

    @Override
    public Boolean call() throws Exception {
        MqttClient publisher = new MqttClient(
          "tcp://localhost:1883", UUID.randomUUID().toString());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        publisher.connect(options);
        IntStream.range(0, 10).forEach(i -> {
        	double number = ThreadLocalRandom.current().nextDouble(0, 100);
            double roundOff = (double) Math.round(number * 100) / 100;
            String valueString = decimalFormat.format(roundOff);
            valueString = valueString.replace(",",".");
        	
        	System.out.println(valueString);
            
        	String payload = String.format("%1$s,city=%2$s,station=%3$s value=%4$s",
              pollutant,
              city,
              station,
              valueString);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(0);
            message.setRetained(true);
            try {
                publisher.publish(topic, message);
                Thread.sleep(1000);
            } catch (MqttException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        return true;
    }
}
