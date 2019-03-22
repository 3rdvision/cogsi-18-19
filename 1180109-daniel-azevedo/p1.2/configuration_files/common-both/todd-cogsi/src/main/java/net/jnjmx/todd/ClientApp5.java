package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientApp5 {

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
			System.out.println("Creating 6 dummies for 5 seconds");
			
			Client c = new Client(server);
			Client c1 = new Client(server);
			Client c2 = new Client(server);
			Client c3 = new Client(server);
			Client c4 = new Client(server);
			Client c5 = new Client(server);
			Client c6 = new Client(server);


			String timeOfDay = c.timeOfDay();

			System.out.println("The current time of day on todd server is: "+timeOfDay);
			
//			System.out.println("Waiting 60 secs to receive notifications");
			Thread.sleep(5000);
			
			c.close();
			c1.close();
			c2.close();
			c3.close();
			c4.close();
			c5.close();
			c6.close();
			
			System.out.println("Dummies disconnected!");
			System.out.println("Exiting...");

			System.exit(0);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

}
