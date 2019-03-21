STUDENT **Daniel Azevedo** (1180109) - P1.1
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


# Problem Analysis

## Objective

* Extend the scenario developed for P1.1 with application monitoring using JMX.

## Requirements:



# Solution Design




# Steps to reproduce

## Configure todd active pulling checks for Todd up and Todd load percentage

0 - Install all dependencies:

* gradle
* openjdk-8-jdk-headless

1- Go to nagios machine and clone the modified version of Todd - to accept JMX remote connections and return exit codes 0 or 2 in case Todd is not running or the sessions load is higher than 20%
`cd ~`
`git clone https://bitbucket.org/dvazevedo/todd-cogsi/src/master/`

2- Do step 1 in the monitoring machine

3- In the monitoring machine run Todd server and change the ip address of that same machine to match your own vhost ip machine:

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

4- Test if nagios machine can connect to the machine running Todd server

`gradle runClient`

`gradle runClient2`

`gradle runClient3`

5- Kill the Todd server in the vhost being monitored and test again if the return code is 2

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

* this will do the same as gradle runClient but it will accept an argument ($1) which will be vhost todd server ip address.

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

* this will do the same as gradle runClient2 but it will accept an argument ($1) which will be vhost todd server ip address and add to it the port 6002 which is the port todd is running on.

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

9- Define the services in Todd vhost

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

12- Test if the service is up when todd server is up and if it becomes critical when it is killed.

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

## Configure nagios to do event handling by growing size of todd

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

4- Add event handler property to todd's passive service by appending the following lines:

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