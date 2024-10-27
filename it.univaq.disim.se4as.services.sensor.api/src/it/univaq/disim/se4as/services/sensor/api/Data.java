package it.univaq.disim.se4as.services.sensor.api;


public class Data {
	String unit;
	int val;
	String brand;
	int accuracy;
	
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public int getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public int getVal() {
		return val;
	}
	public void setVal(int i) {
		this.val = i;
	}

}