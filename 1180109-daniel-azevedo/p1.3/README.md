STUDENT **Daniel Azevedo** (1180109) - P1.3
===============================
[//]: # (EDIT HERE!! /\)

[//]: # (NAGIOS TOOLS)
[//]: # (-------------)
[//]: # (Nagios root path)
[//]: # (/opt/nagios/etc/objects)
[//]: # (Check Config)
[//]: # (sudo /opt/nagios/bin/nagios -v /opt/nagios/etc/nagios.cfg)
[//]: # (Restart Nagios)
[//]: # (sudo systemctl restart nagios)
[//]: # (-------------)

---

![](https://d1jnx9ba8s6j9r.cloudfront.net/blog/wp-content/uploads/2017/11/DevOps-Life-Cyce-Nagios-Tutorial-Edureka.png)

---

[//]: # (-------------)
[//]: # (DOCKER NAGIOS)
[//]: # (https://hub.docker.com/r/ethnchao/nagios/)
[//]: # (https://hub.docker.com/r/quantumobject/docker-nagios/   -- has sendmail)
[//]: # (https://hub.docker.com/r/jasonrivers/nagios/)
[//]: # (DOCKER TOMCAT: )
[//]: # (https://hub.docker.com/r/bitnami/tomcat/)
[//]: # (DOCKER UBUNTU TODD: )
[//]: # (https://hub.docker.com/_/ubuntu  -- 18.04 LTS its the latest)
[//]: # (-------------)

[//]: # (NOTES)
[//]: # (-How to create a new image from a container I modified)
[//]: # (docker commit <container_id> my_image_name_here)
[//]: # (-Get container IP)
[//]: # (docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' container_name_or_id)




# Problem Analysis

## Objective

* 

## Requirements:



---

# Solution Design

Containers

1- Image: Nagios (172.18.0.2)

Opened ports:
* 80:80 - Nagios
* 6002 - JMX TODD
* 6003 - JMX Tomcat
//* - NSCA port

Extra requirements:
* NRPE
* sendmail
* all configurations
* openjdk-8
* gradle


TODD services in the right folders

2- Image: ubuntu (172.18.0.3)

Extra requirements:
* gradle
* openjdk-8
* Todd

Ports:

* 6002 - JMX TODD
//* - NSCA port

3- Tomcat 8 (172.18.0.4)
* 8080:8080 -- tomcat web port
* 6003 - JMX Tomcat
* JMX enabled config file startup (add that file to the etc of tomcat)



---

# Steps to reproduce

## Install and Configure Docker

1- Install docker packege `sudo pacman -Sy docker`

2- Start the docker deamon `sudo systemctl start docker`

3- Test docker hello world `docker run hello-world`

### Configure Docker

#### Configure a docker subnet to have static ip addresses

1- Create a docker subnet with ip 172.18.0.0 and subnet 255.255.0.0 with `docker network create --subnet=172.18.0.0/16 mynet`


## Create docker nagios container

1- Create the container and assign it to the created subnet with the ip 172.18.0.2 `docker run --net mynet --net mynet --ip 172.18.0.2 -d --name nagios jasonrivers/nagios:latest`

2- Open a browser in your host and navigate to 172.18.0.2

3- Login with username nagiosadmin and password nagios

4- Verify nagios is running

5- Enter inside the container

Check container id with:
`docker ps`

`docker exec -it nagios bash`

update repository list
`apt update`

install vim
`apt install vim`

6- Inside the container run `nano /opt/nagios/nagios/etc/nagios.cfg`

7- Afterwards add the line to add vclone services as a new object nagios will scan

```bash

```

8- Create the previous vlones.cfg file and add the following content

```bash
###############################################################################
# LOCALHOST.CFG - SAMPLE OBJECT CONFIG FILE FOR MONITORING THIS MACHINE
#
#
# NOTE: This config file is intended to serve as an *extremely* simple
#       example of how you can create configuration entries to monitor
#       the local (Linux) machine.
#
###############################################################################



###############################################################################
#
# HOST DEFINITION
#
###############################################################################

# Define a host for the local machine

define host {

    use                     linux-server            ; Name of host template to use
                                                    ; This host definition will inherit all variables that are defined
                                                    ; in (or inherited by) the linux-server host template definition.
    host_name               vclone1
    alias                   vclone1
    address                 192.168.99.102  
    contact_groups          CogsiGroup1     
}



###############################################################################
#
# HOST GROUP DEFINITION
#
###############################################################################

# Define an optional hostgroup for Linux machines

## deleted


###############################################################################
#
# SERVICE DEFINITIONS
#
###############################################################################

# Define a service to "ping" the local machine

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    service_description     PING
    check_command           check_ping!100.0,20%!500.0,60%
}



# Define a service to check the disk space of the root partition
# on the local machine.  Warning if < 20% free, critical if
# < 10% free space on partition.

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    check_interval          1    
    service_description     Root Partition Guest
    check_command           check_nrpe!check_disk -a 20% 10% /
}



# Define a service to check the number of currently logged in
# users on the local machine.  Warning if > 20 users, critical
# if > 50 users.

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    service_description     Current Users Guest
    check_interval          1
    check_command           check_nrpe!check_users -a 15 20
}

## Check swap

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    check_interval          1    
    service_description     Check Swap Guest
    check_command           check_nrpe!check_swap
}


# Define a service to check the number of currently running procs
# on the local machine.  Warning if > 250 processes, critical if
# > 400 processes.

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    check_interval          1    
    service_description     Total Processes Guest
    check_command           check_nrpe!check_procs -a 250 400 RSZDT
}



# Define a service to check the load on the local machine.

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    check_interval          1
    service_description     Current Load Guest
    check_command           check_nrpe!check_load -a 5.0,4.0,3.0 10.0,6.0,4.0
}

# Define a service to check SSH on the local machine.
# Disable notifications for this service by default, as not all users may have SSH enabled.

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    service_description     SSH
    check_command           check_ssh
    notifications_enabled   0
}



# Define a service to check HTTP on the local machine.
# Disable notifications for this service by default, as not all users may have HTTP enabled.

define service {

    use                     local-service           ; Name of service template to use
    check_interval          1
    host_name               vclone1
    service_description     HTTP-80
    check_command           check_http
    notifications_enabled   0
}

# My tomcat HTTP service on port 8080

define service {

        use                             local-service
        host_name                       vclone1
        service_description             HTTP-Tomcat-8080
        check_command                   check_http_port
        check_interval                  1
        _port_number                    8080
        event_handler_enabled           1
        event_handler                   restart-tomcat
        max_check_attempts              4
        contacts                        daniel1         
}

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
9- Do the same for templates.cfg file

10- Do same for commands.cfg

11- Do the same for contacts.cfg

12- Repeat the steps for each file of the libexec and give execution and run permissions to every single one of them

x- Uncomment and edit nsca config file to define nagios IP address

Uncomment and edit the ip address to match nagios's
```bash
server_address=172.18.0.2

```
Edit the command file to match nagios output file
```bash
command_file=/opt/nagios/var/rw/nagios.cmd
```

xx- Restart nsca service `service nsca restart`

xxx- Restart with the command /etc/init.d/nagios restart

xxx- Verify nagios configuration for errors by executing `/opt/nagios/bin/nagios -v /opt/nagios/etc/nagios.cfg`

xxxx- Restart nagios by going to the web interface by nagivating to 172.18.0.2 and cliking "Process Info" > "Restart Nagios process"

##  Build and start the docker container for Tomcat 8.5

1- Build a docker image using a Dockerfile for tomcat 8.5 `vim ~/Dockerfile`

```bash
FROM ubuntu:latest
RUN apt-get -y update && apt-get -y upgrade
RUN apt-get -y install openjdk-8-jdk wget ssh nagios-nrpe-server mcollective-plugins-nrpe vim sudo
RUN mkdir /usr/local/tomcat
RUN wget http://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.39/bin/apache-tomcat-8.5.39.tar.gz -O /tmp/tomcat.tar.gz
RUN cd /tmp && tar xvfz tomcat.tar.gz
RUN cp -Rv /tmp/apache-tomcat-8.5.39/* /usr/local/tomcat/
CMD ( /usr/local/tomcat/bin/catalina.sh run & ) && service ssh start && service nagios-nrpe-server start && tail -f /dev/null
```

2- Build the image using docker build command

```bash
cd ~
docker build -t my-tomcat:v1 .
```
3- `docker run --net mynet --ip 172.18.0.4 -d --name tomcat -td my-tomcat:v1`

4- Check if a tomcat server is running by nativating in your host computer to 172.18.0.4:8080

5- Configure and restart NRPE `vim /etc/nagios/nrpe.cfg`

Change the allowed host line to accept connections from nagios server:
```bash
allowed_hosts=172.18.0.2,127.0.0.1,::1
```

Append the following line to allow NRPE to accept restart_tomcat command
```bash
command[restart_tomcat]=sudo /usr/local/tomcat/bin/shutdown.sh && sleep 5 && sudo usr/local/tomcat/bin/startup.sh
```

6- Restart NRPE server
`service nagios-nrpe-server restart`

7- Add the user nagios to the sudoers file to give permissions to restart nagios `visudo`

Append the following lines:
```bash
nagios ALL=(ALL) NOPASSWD: /usr/local/tomcat/bin/startup.sh
nagios ALL=(ALL) NOPASSWD: /usr/local/tomcat/bin/catalina.sh
nagios ALL=(ALL) NOPASSWD: /usr/local/tomcat/bin/shutdown.sh
```
6- Activate JMX monitoring through remote by adding the necessary parameters in tomcat.

Create a setenv.sh in CATALINA_HOME/bin tomcat location, in this case was `sudo vim /usr/local/tomcat/bin/setenv.sh`

```bash
CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=6003 -Dcom.sun.management.jmxremote.rmi.port=6003 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=172.18.0.4"
```

7- Give user permissions and run the created setenv.sh file `chmod +rx /usr/local/tomcat/bin/setenv.sh && /usr/local/tomcat/bin/setenv.sh`

8- Restart tomcat `/usr/local/tomcat/bin/shutdown.sh && sleep 5 && /usr/local/tomcat/bin/startup.sh`

9- Test the feature by shutting down tomcat and waiting for it to restart via the event handler and NRPE `/usr/local/tomcat/bin/shutdown.sh`

10- Connect to Tomcat JMX by `jconsole` at the host server using the server address 172.18.0.4:6003

##  Build and start the docker container for TODD


1- Build a docker image using a Dockerfile for TODD `vim ~/Dockerfile`

```bash
FROM ubuntu:latest
RUN apt-get -y update && apt-get -y upgrade
RUN apt-get -y install openjdk-8-jdk wget ssh nagios-nrpe-server mcollective-plugins-nrpe vim sudo
RUN mkdir /usr/local/tomcat
RUN wget http://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.39/bin/apache-tomcat-8.5.39.tar.gz -O /tmp/tomcat.tar.gz
RUN cd /tmp && tar xvfz tomcat.tar.gz
RUN cp -Rv /tmp/apache-tomcat-8.5.39/* /usr/local/tomcat/
CMD ( /usr/local/tomcat/bin/catalina.sh run & ) && service ssh start && service nagios-nrpe-server start && tail -f /dev/null
```

2- Build the image using docker build command

```bash
cd ~
docker build -t my-todd:v1 .
```

3- `docker run --net mynet --ip 172.18.0.3 -d --name todd -td my-todd:v1`

# References

* 