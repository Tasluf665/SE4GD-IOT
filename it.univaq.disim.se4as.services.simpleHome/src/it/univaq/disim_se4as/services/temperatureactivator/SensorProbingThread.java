package it.univaq.disim_se4as.services.temperatureactivator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import it.univaq.disim.se4as.services.sensor.api.ISensor;

public class SensorProbingThread extends Thread {
    private volatile boolean active = true;
    private BundleContext context;


	public void run() {
        while (active) {
				try {
					printSensorValues();
	    			Thread.sleep(5000);

				} catch (InvalidSyntaxException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
	}
	
    public void setContext(BundleContext context){
    	this.context=context;
    }
	
	private void printSensorValues() throws InvalidSyntaxException {
		ServiceReference<?>[] refs = context.getAllServiceReferences(ISensor.class.getName(), null);        
		 if (refs != null) {
			 	for (int i = 0; i < refs.length; i++) {
					 ISensor sensor = (ISensor) context.getService(refs[i]);
					 if (refs[i] != null) {
						 if (sensor != null) {
							 System.out.println("Found one sensor:");
							 System.out.println("Unit: " + sensor.getData().getUnit());
							 System.out.println("Sensed value: " + sensor.getData().getVal());
							 System.out.println("Accuracy: " + sensor.getData().getAccuracy());

						 }

					 }
				 }
				 				 
            }
	}
}
