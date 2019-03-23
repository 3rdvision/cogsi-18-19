package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Scanner;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import javax.management.remote.*;


public class ClientAppTomcat {

	/**
	 * The first two lines here create and register a GaugeMonitor MBean named
	 * todd:id=SessionPoolMonitor. The next seven lines set attributes that tell
	 * GaugeMonitor which attribute of which MBean should be monitored
	 * (ObservedAttribute or ObservedObject), how often (GranularityPeriod, in
	 * milliseconds), and whether or not to send a notification on high-threshold
	 * and low-threshold violations. Then we invoke the setThresholds() method, via
	 * the MBeanServer, to set the actual high and low threshold values. Finally, we
	 * make the server listen for session pool monitor notifications and start the
	 * gauge monitor.
	 */
	public static void configureTomcatMonitor(MBeanServerConnection mbs) throws Exception {
		ObjectName spmon = new ObjectName("java.lang:type=OperatingSystemMonitoringTomcat");

		Set<ObjectInstance> mbeans = mbs.queryMBeans(spmon, null);

		if (mbeans.isEmpty()) {
			mbs.createMBean("javax.management.monitor.GaugeMonitor", spmon);
		} else {
			// noting to do...
		}

		System.out.println("going for the attribute list");

		AttributeList spmal = new AttributeList();
		spmal.add(new Attribute("ObservedObject", new ObjectName("java.lang:type=OperatingSystem")));
		spmal.add(new Attribute("ObservedAttribute", "ProcessCpuLoad"));
		spmal.add(new Attribute("GranularityPeriod", new Long(1000)));
		spmal.add(new Attribute("NotifyHigh", new Boolean(true)));
		spmal.add(new Attribute("NotifyLow", new Boolean(true)));
		mbs.setAttributes(spmon, spmal);

		System.out.println("invoking notification");


		// CPU process should be < 5%
		mbs.invoke(spmon, "setThresholds", new Object[] { new Double(0.10), new Double(0.10) },
				new String[] { "java.lang.Number", "java.lang.Number" });

		System.out.println("invoked notification");


		mbs.addNotificationListener(spmon, new JMXNotificationListener(), null, null);

		mbs.invoke(spmon, "start", new Object[] {}, new String[] {});
	}


	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("Todd ClientApp2... Accessing JMX Beans (and the 'Uptime' property of TODD MBean 'Server')");

		try {
			String server = "192.168.99.102:6003";

			if (args.length >= 1) {
				server = args[0];
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

//			for (ObjectInstance mbean : mbeans) {
//				System.out.println(mbean.getClassName());
//				System.out.println(mbean.getObjectName());
//			}
			
			// Lets try to access the MBean net.jnjmx.todd.Server:
			//ObjectName son = new ObjectName("todd:id=Server");
			// I want-> com.sun.management.internal.OperatingSystemImpl
			// Object name-> java.lang:type=OperatingSystem
			ObjectName son = new ObjectName("java.lang:type=OperatingSystem");
			//ObjectName son = new ObjectName("com.sun.management.internal:id=OperatingSystemImpl");
			//ObjectInstance ob = mbs.getObjectInstance(son);
			
			Double processCpuload=(Double)mbs.getAttribute(son, "ProcessCpuLoad");

			System.out.println("ProcessCpuload right now: "+processCpuload);

			System.out.println("Now setting up notification handlers...");
			configureTomcatMonitor(mbs);
			System.out.println("Waiting 292 billion years and processing notifications");
			Thread.sleep(Long.MAX_VALUE);			

			c.close();

		} catch (Exception ex) {
			System.out.println("Error: unable to connect to MBean Server");
			System.exit(2);
		}
	}
}
