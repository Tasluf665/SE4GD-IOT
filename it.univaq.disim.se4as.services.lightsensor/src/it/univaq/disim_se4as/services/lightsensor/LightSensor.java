package it.univaq.disim_se4as.services.lightsensor;

import java.util.Random;

import it.univaq.disim.se4as.services.sensor.api.ISensor;
import it.univaq.disim.se4as.services.sensor.api.Data;

public class LightSensor implements ISensor {
	Data data;
	
	public LightSensor() {
		Random random = new Random();
		int i = random.nextInt(100);
		int accuracy = random.nextInt(100);
	
		data = new Data();
		data.setUnit("Lux");
		data.setVal(i);
		data.setAccuracy(accuracy);
		
	}
	
	@Override
	public Data getData() {
		return this.data;
	}

	@Override
	public void setData(Data data) {
		this.data = data;
	}

}
