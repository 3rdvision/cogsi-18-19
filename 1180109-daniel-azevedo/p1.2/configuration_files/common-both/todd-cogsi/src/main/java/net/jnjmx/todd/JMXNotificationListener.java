package net.jnjmx.todd;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;

public class JMXNotificationListener implements NotificationListener {
	@Override
	public void handleNotification(Notification notification, Object handback) {
		System.out.println("Received Notification");
		System.out.println("======================================");
		System.out.println("Timestamp: " + notification.getTimeStamp());
		System.out.println("Type: " + notification.getType());
		System.out.println("Sequence Number: " + notification.getSequenceNumber());
		System.out.println("Message: " + notification.getMessage());
		System.out.println("User Data: " + notification.getUserData());
		System.out.println("Source: " + notification.getSource());
		System.out.println("======================================");

        // Server info variables
        String s;
        Process p;
        String rHostIp = "192.168.99.101";
        String hostName = "vclone1";
        String hostServiceName = "todd-passive-up";
        String codeErr = "2";
        String codeOk = "0";
        String code;
        String messageOk = "Passive Check OK.";
        String messageErr = "Passive Check OVERLOAD RESOURCES.";
        String message;
        String tab = "\t";
        String pathFile = "/tmp/test";
        String commandToExec = "send_nsca -H " + rHostIp + " < " + pathFile;

        String target = "todd:id=PercentageOfResource";
        String isOnStress = "jmx.monitor.gauge.low";

        // If the notification it's of type PercentageOfResource
        if ( target.equals(""+notification.getSource()) ) {
            // Create a text file with a line\n containing the passive information

            if ( isOnStress.equals(""+notification.getType())){
                // There is a shortage of resources.
                code=codeErr;
                message=messageErr;
                System.out.println("Sending OVERLOAD notification.");
            } else {
                code=codeOk;
                message=messageOk;
                System.out.println("Sending OK notification.");
            }

            try {
                File file = new File (pathFile);
                file.getParentFile().mkdirs();
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.print(hostName+tab+hostServiceName+tab+code+tab+message+"\n\n");
                writer.close();
            } catch (Exception e) {
                System.err.println("Exception when printing to the file.");
                System.exit(1);
            }

            // Execute the command send_nsca -H <hostname> < textfile
            try {
                System.out.println("sending command: \n"+commandToExec);
                p = Runtime.getRuntime().exec(new String[] { "/bin/sh"
                    , "-c", commandToExec });            
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
                while ((s = br.readLine()) != null)
                    System.out.println("line: " + s);
                p.waitFor();
                System.out.println ("exit code: " + p.exitValue());
                p.destroy();
            } catch (Exception e) {
            	System.err.println("Exception at executing command:\n" + commandToExec);
            	System.exit(1);
            }
        }
	}
}

