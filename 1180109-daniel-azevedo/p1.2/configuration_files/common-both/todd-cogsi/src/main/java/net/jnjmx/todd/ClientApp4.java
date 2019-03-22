package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Scanner;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import javax.management.remote.*;

//Mine
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;


public class ClientApp4 {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("Todd ClientApp4... Accessing JMX Beans (and gorwing the size)");

		try {
			String server = "192.168.56.11:10500";
			int growSize = 10;

			if (args.length >= 1) {
				server = args[0];
			}
			if (args.length > 1){
				growSize = Integer.parseInt(args[1]);
			}

			System.out.println("Connecting to JMX Agent (with running TODD server) at "+server+" ...");

			// Connect to a remote MBean Server
			JMXConnector c = javax.management.remote.JMXConnectorFactory
					.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + server + "/jmxrmi"));
			
			MBeanServerConnection mbs = c.getMBeanServerConnection();

			/*
			 * MBeanServerConnection conn=new MBeanServerConnection();
			 * ManagementFactory.newPlatformMXBeanProxy(MBeanServerConnection
			 * connection, String mxbeanName, Class<T> mxbeanInterface)
			 */

			Set<ObjectInstance> mbeans = mbs.queryMBeans(null, null);
			ObjectName son2 = new ObjectName("todd:id=SessionPool");
			ObjectInstance ob2 = mbs.getObjectInstance(son2);			

			// Now invoking operation to grow
			// Go through operation to find the interested one and invoke
			MBeanInfo beanInfo = mbs.getMBeanInfo(son2); //uses Son2
			mbs.invoke(son2, "grow", new Integer[] {new Integer(growSize)}, new String[] {"int"} );
			System.out.println("Grow of "+growSize+" sent to server.");
			c.close();

		} catch (Exception ex) {
			System.out.println("Error: unable to connect to MBean Server");
			System.exit(2);
		}
	}
}
