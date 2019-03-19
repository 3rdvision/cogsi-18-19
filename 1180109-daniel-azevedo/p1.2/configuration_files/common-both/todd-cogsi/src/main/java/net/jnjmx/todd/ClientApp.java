package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientApp {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		System.out.println("Todd ClientApp... The 'regular' TODD client application.");
		
		try {
			String server = "192.168.56.11";
			
			if (args.length >= 1) {
				server = args[0];
			}			

			System.out.println("Connecting to TODD server at "+server+" ...");
			
			Client c = new Client(server);

			String timeOfDay = c.timeOfDay();

			System.out.println("The current time of day on todd server is: "+timeOfDay);
			
//			System.out.println("Waiting 60 secs to receive notifications");
//			Thread.sleep(60000);
			
			c.close();
			
			System.out.println("Exiting...");

			System.exit(0);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

}
