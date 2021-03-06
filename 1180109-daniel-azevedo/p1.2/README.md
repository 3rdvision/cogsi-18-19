STUDENT **Daniel Azevedo** (1180109) - P1.2
===============================

[//]: # (NAGIOS TOOLS)
[//]: # (-------------)
[//]: # (Nagios root path)
[//]: # (/usr/local/nagios/etc/objects)
[//]: # (Check Config)
[//]: # (sudo /usr/local/nagios/bin/nagios -v /usr/local/nagios/etc/nagios.cfg)
[//]: # (Restart Nagios)
[//]: # (sudo systemctl restart nagios)
[//]: # (-------------)

---

![](https://d1jnx9ba8s6j9r.cloudfront.net/blog/wp-content/uploads/2017/11/DevOps-Life-Cyce-Nagios-Tutorial-Edureka.png)

---

# Problem Analysis

## Objective

* Extend the scenario developed for P1.1 with application monitoring using JMX.

## Requirements:

### Integration with Nagios:

* Check (polling) if TODD server is running. If TODD server is not running you should
try to automatically start it

* Check (polling) if the "AvailableSessions" attribute is below 20% of the "Size"
attribute. If so you should consider that the service is critical and notify someone.

### JMX Notifications

* You should develop a JMX Agent Application that is able to use JMX notifications
to react to "events" in a JMX monitored resource.

* Using JMX notifications this application should be able to monitor the TODD server
and increase the size of the "SessionPool" by using the method "grow" when the
"AvailableSessions" attribute is below 20% of the "Size" attribute. You should try
to integrate with the system monitoring tools using passive checks.

* You should also use a similar approach to handle possible JVM memory limitations
regarding the Tomcat server. You should define thresholds for this concern and try
to free memory or even restart tomcat if necessary (with different settings for the
memory usage of the JVM).

---

# Solution Design

A customized version of TODD (Time Of Day Deamon):

* Public repository: https://bitbucket.org/dvazevedo/todd-cogsi/src/master/

In this solution, two virtual machines were used:

* Ubuntu 18.04 LTS server with 2GB RAM, 10 GB HDD, Nagios-core installed server with nagios pluggins package, gradle, openjdk-8-headless, nsca and TODD from above repository.

* Ubuntu 18.04 LTS server with 2GB RAM, 10 GB HDD, Nagios agent installed with nagios pluggins package, gradle, openjdk-8-headless, nsca and TODD from above repository.

Overview:

The solution takes advantage of the NSCA passive notification plugin on nagios

![](https://i.imgur.com/G5IHnaI.png)

In this case, the external application will be the Java client that will be monitoring TODD or Tomcat through JMX

The JMX architecture should be simply put in the below image:

![](https://upload.wikimedia.org/wikipedia/en/thumb/d/db/Jmxarchitecture.png/400px-Jmxarchitecture.png)

In the below steps to reproduce there are more details and explanantions about the procedure but in an overall matter it consists of the following key points:

* Define an active nagios check that uses java executable that will return either 0 or 2 code whenether TODD is running on the remote machine

* Define an active nagios check that uses java executable that will return either 0 or 2 code whenether TODD load is > 20%

* An event listener with a gauge for high and low <20%/>20% for the load of Available Sessions / Size in terms of the property SessionsPool. This gauge will trigger and send a notification that will be handled in a behavior explained in the next point.

* A notification receiver server running on remote machine that will get notified when the load is >20% and will react by sending a send_nsca command to the nagios machine with the status of the load of TODD AND >10% for the load of Tomcat process CPU.

* Zabbix needs zabbix-java-gateway module to monitor JMX, after configuring the module and restarting zabbix-server and starting zabbix-java-gateway, zabbix can be configured to monitor the JMX parameters of tomcat. More details in the steps to reproduce laster chapter. 

---

# Steps to reproduce

## Configure TODD active pulling checks for TODD up and TODD load percentage

0 - Install all dependencies:

* gradle
* openjdk-8-jdk-headless

1- Go to nagios machine and clone the modified version of TODD - to accept JMX remote connections and return exit codes 0 or 2 in case TODD is not running or the sessions load is higher than 20%
`cd ~`
`git clone https://bitbucket.org/dvazevedo/todd-cogsi/src/master/`

2- Do step 1 in the monitoring machine

3- In the monitoring machine run TODD server and change the ip address of that same machine to match your own vhost ip machine:

`vim build.gradle`

```java

apply plugin: 'java'
apply plugin: 'eclipse'

// Execute the Server (local JMX only)
task runServer(type:JavaExec, dependsOn: classes) {
   main = 'net.jnjmx.todd.Server'
    if (project.hasProperty("appArgs")) {
        jvmArgs Eval.me(appArgs)
    }
	else {
	   jvmArgs = ['-Dcom.sun.management.jmxremote']
	}
 
   classpath = sourceSets.main.runtimeClasspath
}

// Execute the Server (enable remote JMX)
task runServerRemote(type:JavaExec, dependsOn: classes) {
   main = 'net.jnjmx.todd.Server'
    if (project.hasProperty("appArgs")) {
        jvmArgs Eval.me(appArgs)
    }
	else {
	   jvmArgs = ['-Dcom.sun.management.jmxremote',
              '-Dcom.sun.management.jmxremote.port=6002',
              '-Dcom.sun.management.jmxremote.rmi.port=6002',
              '-Dcom.sun.management.jmxremote.authenticate=false',
              '-Dcom.sun.management.jmxremote.local.only=false',
              '-Dcom.sun.management.jmxremote.ssl=false',
              '-Djava.rmi.server.hostname=192.168.99.102'] // <--- Change this
    }
   classpath = sourceSets.main.runtimeClasspath
}

// Execute the Client
task runClient(type:JavaExec, dependsOn: classes) {
   main = 'net.jnjmx.todd.ClientApp'
    if (project.hasProperty("appArgs")) {
        args Eval.me(appArgs)
    }
	else {
   		args = ['192.168.99.102'] // <--- Change this
   }
   classpath = sourceSets.main.runtimeClasspath
}

// Execute the ClientApp2
task runClient2(type:JavaExec, dependsOn: classes) {
   main = 'net.jnjmx.todd.ClientApp2'
    if (project.hasProperty("appArgs")) {
        args Eval.me(appArgs)
    }
	else {
     args = ['192.168.99.102:6002'] // <--- Change this
     }
   classpath = sourceSets.main.runtimeClasspath
}

// Execute the ClientApp3
task runClient3(type:JavaExec, dependsOn: classes) {
   main = 'net.jnjmx.todd.ClientApp3'
    if (project.hasProperty("appArgs")) {
        args Eval.me(appArgs)
    }
	else {
   		args = ['192.168.99.102:6002'] // <--- Change this
   	}
   classpath = sourceSets.main.runtimeClasspath
}


```

`cd ~/todd-cogsi`

`gradle runServerRemote`

4- Test if nagios machine can connect to the machine running TODD server

`gradle runClient`

`gradle runClient2`

`gradle runClient3`

5- Kill the TODD server in the vhost being monitored and test again if the return code is 2

`gradle runClient`

`gradle runClient2`

`gradle runClient3`

6- Create nagios plugin script to use as the check up command:

`cd /usr/local/nagios/libexec`

`sudo vim vim check_todd_up`


```bash
#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "No arguments supplied"
  else
    echo "Doing check on hostname: $1"
fi


cd /home/dan/todd-cogsi/build/classes/main
java net.jnjmx.todd.ClientApp $1
```

* this will do the same as gradle runClient but it will accept an argument ($1) which will be vhost TODD server ip address.

7-  Create nagios plugin script to use as the check load command:

`sudo vim vim check_todd_load`


```bash
#|/bin/bash


if [ $# -eq 0 ]
  then
    echo "No arguments supplied"
  else
    echo "Doing check on hostname: $1:6002"
fi


cd /home/dan/todd-cogsi/build/classes/main
java net.jnjmx.todd.ClientApp2 $1:6002
```

* this will do the same as gradle runClient2 but it will accept an argument ($1) which will be vhost TODD server ip address and add to it the port 6002 which is the port todd is running on.

8- Add the command to nagios configuration

`sudo vim /usr/local/nagios/etc/objects/commands.cfg`

```bash

### Check todd up

define command{
    command_name check_todd_up
    command_line $USER1$/check_todd_up $HOSTADDRESS$
}

### Check todd load

define command{
    command_name check_todd_load
    command_line $USER1$/check_todd_load $HOSTADDRESS$
}
```

9- Define the services in TODD vhost

`sudo vim /usr/local/nagios/etc/objects/vclones.cfg`


```bash
# My todd up service

define service {

        use                             local-service
        host_name                       vclone1
        service_description             Todd-Up-Service
        check_command                   check_todd_up
        check_interval                  1
        max_check_attempts              4
        contacts                        daniel1         
}

# My todd load service

define service {

        use                             local-service
        host_name                       vclone1
        service_description             Todd-Load-Service
        check_command                   check_todd_load
        check_interval                  1
        max_check_attempts              4
        contacts                        daniel1         
}
```

10- Validate nagios configurations files

`sudo /usr/local/nagios/bin/nagios -v /usr/local/nagios/etc/nagios.cfg`

11- Restart nagios `sudo systemctl restart nagios.service` and go to nagios main page `http://nagios-ip/nagios/`

12- Test if the service is up when TODD server is up and if it becomes critical when it is killed.

## Configure nagios to receive passive NSCA notifications (push notifications)

1- Install nsca for ubuntu 18.04 LTS `apt install nsca` in nagios host (compiling from source using tar package from nagios documentation doesn't work as NSCA is mostly depricated, unmaintained and the make install won't work on Ubuntun 18.04)

2- Configure NSCA config file `sudo vim /etc/nsca.cfg` and change the following atributes:

```bash
server_address=192.168.99.101 # <-- your nagios ip address

debug=1

command_file=/usr/local/nagios/var/rw/nagios.cmd

alternate_dump_file=/usr/local/nagios/var/rw/nsca.dump

```

3- Start NSCA service if it's not already running to apply the new settings `sudo service nsca restart`

4- Also install nsca package `apt install nsca` (in this step we only really need the nsca_client but due to Ubuntu 18.04 client being uncompatible with nsca server version, it will be necessary to make this)

5- Try to send a test command to confirm it is working `send_nsca -H <nagios-ip-address> < test`

6- Check the syslog in nagios machine and confirm it received something `tail -f /var/log/syslog`

## Configure nagios to do event handling by growing size of TODD

1- Append the following command to nagios `sudo vim /usr/local/nagios/etc/objects/commands.cfg`

```bash
### Grow todd size

define command{
    command_name grow_todd
    command_line    /usr/local/nagios/libexec/grow_todd  $SERVICESTATE$ $SERVICESTATETYPE$ $SERVICEATTEMPT$ $HOSTADDRESS$ $_SERVICEGROW_NUMBER$
}
```

2- Create the following script in libexec `sudo vim /usr/local/nagios/libexec/grow_todd`

```bash
#!/bin/sh
#
# Event handler script for restarting the web server on the local machine
#
# Note: This script will only restart the web server if the service is
#       retried 3 times (in a "soft" state) or if the web service somehow
#       manages to fall into a "hard" error state.
#

# What state is the HTTP service in?

echo "Variables todd: 1: $1 2: $2 3: $3 4: $4 5: $5 and... over." >> /home/nagios/log  

case "$1" in
OK)
	# The service just came back up, so don't do anything...
	;;
WARNING)
	# We don't really care about warning states, since the service is probably still running...
	;;
UNKNOWN)
	# We don't know what might be causing an unknown error, so don't do anything...
	;;
CRITICAL)
	# Aha!  The HTTP service appears to have a problem - perhaps we should restart the server...
	# Is this a "soft" or a "hard" state?
	case "$2" in

	# We're in a "soft" state, meaning that Nagios is in the middle of retrying the
	# check before it turns into a "hard" state and contacts get notified...
	SOFT)

		# What check attempt are we on?  We don't want to restart the web server on the first
		# check, because it may just be a fluke!

		# Wait until the check has been tried 3 times before restarting the web server.
		# If the check fails on the 4th time (after we restart the web server), the state
		# type will turn to "hard" and contacts will be notified of the problem.
		# Hopefully this will restart the web server successfully, so the 4th check will
		# result in a "soft" recovery.  If that happens no one gets notified because we
		# fixed the problem!
		echo -n "Growing todd size soft..."
		echo "Now executing grow_todd from SOFT state" >> /home/nagios/log  		
		# Call the init script to restart the HTTPD server
		cd /home/dan/todd-cogsi/build/classes/main
		java net.jnjmx.todd.ClientApp4 ${4}:6002 $5 >> /home/nagios/log  
		;;

	# The HTTP service somehow managed to turn into a hard error without getting fixed.
	# It should have been restarted by the code above, but for some reason it didn't.
	# Let's give it one last try, shall we?  
	# Note: Contacts have already been notified of a problem with the service at this
	# point (unless you disabled notifications for this service)
	HARD)
		echo -n "Growing todd size hard..."
		echo "Now executing grow_todd from HARD state" >> /home/nagios/log  		
		# Call the init script to restart the HTTPD server
		cd /home/dan/todd-cogsi/build/classes/main
		java net.jnjmx.todd.ClientApp4 ${4}:6002 $5 >> /home/nagios/log  
		;;
	esac
	;;
esac
exit 0


```

3- Give run and read permissions to that script `sudo chmod +rx /usr/local/nagios/libexec/grow_todd`

4- Add event handler property to TODD's passive service by appending the following lines:

```bash
define service{
        use                             passive-service
        host_name                       vclone1        
        service_description             todd-passive-up
        max_check_attempts              1
        event_handler_enabled           1
        event_handler                   grow_todd        
        _grow_number                    12        
}
```
5- Check configuration for errors `sudo /usr/local/nagios/bin/nagios -v /usr/local/nagios/etc/nagios.cfg`

6- If everything is good, restart nagios `sudo systemctl restart nagios.service`

Each time there is an overload or recovery, ClientApp3 will automatically trigger everything to notify nagios of the sittuation which may itself do an event handle to grow those resources and get things back to normal by invoking an JMX event to execute the grow() method of Todd.

## Configure the remote host to receive JMX notifications when threshold is under 20% of resources and warn nagios host through send_nsca 

1- Clone the custom repository of TODD `git clone https://dvazevedo@bitbucket.org/mei-isep/todd.git`

2- Start TODD server and TODD notification receiver/handler

`gradle runServerRemote`

`gradle runClient3`

Client 3 will add a notifiction event listener to the remote server that will trigger a notification whenether the free resources are under 20% and afterwards when they return back to normal state >20%.

The notification trigger will also create a file in /tmp/test with contents that will be automatically used in the command `send_nsca -H <nagios-ip> < /tmp/test`

The content of /tmp/test will have the nagios remote host name, the service name, the return code (0 if OK or 2 if OVERLOAD) and a custom message.

Each time there is an overload or recovery, ClientApp3 will automatically trigger everything to notify nagios of the sittuation which may itself do an event handle to grow those resources and get things back to normal by invoking an JMX event to execute the grow() method of Todd.

To test this feature `gradle runClient5` should be used in order to create 6 dummies for 10 seconds that will fill 6 sessions and surpass the 20% for the default amount of AvailableSessions (8 Sessions).

** All the implemented JMX code is commented and documented so please check the src for the custom TODD repository as stated in the beggining of this README. **

## Configure JMX to notify nagios when Tomcat server is using too much memory

1- Activate JMX monitoring through remote by adding the necessary parameters in tomcat.

Create a setenv.sh in CATALINA_HOME/bin tomcat location, in this case was `sudo vim /opt/tomcat/latest/bin/setenv.sh`

```bash
CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=6003 -Dcom.sun.management.jmxremote.rmi.port=6003 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=192.168.99.102"
```

2- Give execute and read permissions `sudo chmod 775 setenv.sh`

3- Restart tomcat and check that te environemnts were introduced by checking the parameters at status

Restart:
`sudo systemctl restart tomcat`

Check if the JMX enable environement parameteres were correctly set
`sudo systemctl status tomcat`

4- Add the JMX notification gauge and listener by running `gradle runClientAppTomcat`

5- Add a passive service to receive the passive tomcat overload notifications `sudo vim /usr/local/nagios/etc/objects/vclones.cfg`

Append the following lines to the document:
```bash

define service{
        use                             passive-service
        host_name                       vclone1        
        service_description             tomcat-passive-load
        max_check_attempts              1
        event_handler_enabled           1
        event_handler                   restart-tomcat
}
```

6- Restart tomcat `sudo systemctl restart tomcat.service`

7- Refresh webpage many times rapidly by continuously pressing F5 without releasing - to overload the CPU - and test the receiving of the notification and the triggering of the event to restart tomcat and send an e-mail notification.

## Configure Zabbix to monitor Tomcat through JMX 

1- Install zabbix-java-gateway `sudo apt install zabbix-java-gateway`

2- Configure zabbix-java-gateway `sudo vim /etc/zabbix/java_server.conf` and add the following lines:

```bash
JavaGateway=127.0.0.1
StartJavaPollers=5
```

3- Restart zabbix and enable and start zabbix-java-gateway

Enable Zabbix-Java-Gateway
`sudo systemctl enable zabbix-java-gateway`

Restart Zabbix
`sudo systemctl restart zabbix-server`

Start Zabbix-Java-Gatewway
`sudo systemctl start zabbix-java-gateway`

4- Go to zabbix web interface at http://<zabbix-ip-address>/zabbix

5- Go to Configuration>Hosts>Add Host

6- Add a new host with name vclone1_tomcat

7- Delete the Agent interface and add a new JMX interface with ip address of the vclone and the port to JMX which was specified in this document as being 6002

8- Go to templates and add "Template App Apache Tomcat JMX"

9- Click the blue Add button to add the new host and wait until the JMX turns green by refreshing the page once every 30 seconds

10- In the Zabbix web interface, go to Monitoring > Graphs and select the host vclone1_Tomcat or the name you configured in step 6 and selec the graph http-8080-worker-threads or any other graph data and the monitoring data will be displayed.

# References



* https://wiki.scn.sap.com/wiki/pages/viewpage.action?pageId=441453905
* https://www.zabbix.com/documentation/4.2/pt/manual/concepts/java
* https://www.youtube.com/watch?v=h6MlF4ztekg -- Zabbix Java Gateway Installation With Tomcat Monitoring

