package it.univaq.disim.se4as.paho;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Executor {

	public static void main(String[] args) throws InterruptedException{

 
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		List<Callable<Boolean>> sensors = Arrays.asList(
		  new Sensor("london", "central", "ozone", "/airquality/ozone"),
		  new Sensor("london", "central", "co", "/airquality/co"),
		  new Sensor("london", "central", "so2", "/airquality/so2"),
		  new Sensor("london", "central", "no2", "/airquality/no2"),
		  new Sensor("london", "central", "aerosols", "/airquality/aerosols"));
		
		while (true) {
			List<Future<Boolean>> futures = executorService.invokeAll(sensors);
			Thread.sleep(60000);
		}
	}
}
