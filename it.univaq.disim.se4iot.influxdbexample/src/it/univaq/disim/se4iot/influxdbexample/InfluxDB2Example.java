package it.univaq.disim.se4iot.influxdbexample;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

public class InfluxDB2Example {
	
	
  public static void main(final String[] args) {

	    // You can generate a Token from the "Tokens Tab" in the UI
	    String token = "qwgNARIwfSw8BfhLvXjwtH4p010b9uB4tD6SesDp0UD5rYJ7RMKQwjAsWxVpJkCotYQ3KShsksTXbV7aC4A5ZA==";
	    String bucket = "SE4IOT-demo-bucket";
	    String org = "local-org";

	    InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());

	  Point point = Point
			  .measurement("powerconsumption")
			  .addTag("device", "device1")
			  .addTag("client", "Java")
			  .addField("power", 600)
			  .time(Instant.now(), WritePrecision.NS);
	
			try (WriteApi writeApi = client.getWriteApi()) {
			  writeApi.writePoint(bucket, org, point);
			}
 
  }
}