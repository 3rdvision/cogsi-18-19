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