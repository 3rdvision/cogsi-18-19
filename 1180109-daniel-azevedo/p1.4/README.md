STUDENT **Daniel Azevedo** (1180109) - P1.4
===============================
[//]: # (EDIT HERE!! /\)

[//]: # (NAGIOS)
[//]: # (-------------)
[//]: # (Check Config)
[//]: # (/opt/nagios/bin/nagios -v /opt/nagios/etc/nagios.cfg)
[//]: # (-------------)

---

![](https://static1.squarespace.com/static/58939c00bebafb2e19889577/58aeaf4e5016e19bc9091cf5/58aeaf6b46c3c4b688bce67b/1487931589078/GNS3_Logo_Web.jpg?format=1500w)

---

# Problem Analysis

## Objective

* Simulate a local network with components from the previous exercises

## Requirements:

### Simulation

* The simulation should include 3 routers, as depicted in the previous figure.
* The routers should be simulated using a docker image based on quagga (follow the tutorial in https://www.gns3.com/news/article/running-quagga-router-as-a-conta)
* Include at least one switch in your simulation (see previous figure)

Include in the simulation:

* A monitoring system based on Nagios
* A system running the Todd Java application
* A system running Tomcat

* They should be directly connected (or via a switch) to different routers.
* The simulation should aim at consume the lowest resources of the host system, i.e., using Docker containers.

### Monitoring

* Model the topology of the network in the monitoring system (i.e., use the "parents" attribute in the Nagios configuration of hosts)
* The monitoring system should include a solution to automatically identify new hosts in the monitored networks.
* Replicate the monitored solutions developed in the previous exercises (i.e., active and passive monitoring with NRPE, JMX and NSCA).
* Monitor all the elements in the network (i.e., include routers and switches)

### Alternative

As an alternative for this exercise, use a different monitoring application
(i.e., Zabbix vs Nagios)

The scenario should be similar but use an alternative monitoring application.

Use the GNS3/Wireshark integration (i.e., in a link use the "Start Capture" right menu option) to explore differences in the monitoring protocols
between the Active and Passive monitoring approaches in both monitoring applications.

* How network traffic compares between active and passive?
* And between Nagios and the Alternative?

---

# Solution Design

---

# Steps to reproduce

## Install and Configure GNS3

1- Install the packages and dependencies by following the steps on https://wiki.archlinux.org/index.php/GNS3#Installation if you have arch-based distro, otherwise follow GNS3 documenation (https://docs.gns3.com/1QXVIihk7dsOL7Xr7Bmz4zRzTsJ02wklfImGuHwTlaA4/index.html)

2- Enable libvirt and docker services `sudo systemctl enable docker libvirt`

3- Create a file at /tmp/default.xml `vim /tmp/default.xml`

```xml
<network>
  <name>default</name>
  <bridge name="virbr0"/>
  <forward mode="nat"/>
  <ip address="192.168.122.1" netmask="255.255.255.0">
    <dhcp>
      <range start="192.168.122.2" end="192.168.122.254"/>
    </dhcp>
  </ip>
</network>
```

4- Run the following commands to configure your NAT network using libvirt `virsh net-define /tmp/default.xml && sudo virsh net-start default && sudo virsh net-autostart default`

5- Open GNS3 for the first time and configure it to run on the local server (less resource hungry) so it uses the computer's docker instead of the alternatives which are using a remote computer as a VM or create a VM inside the computer and run docker inside of it.

6- Create a new project a call it cogsi

7- After this GNS3 is ready to be used and to follow the rest of the steps below.

## Create the topology

We are going to create the below topology step by step

![](https://i.imgur.com/pDSgrWa.png)

### Install the appliances

1- First we are going to add the components needed for this topology and for this we need to go to the sidebar and clock browse all devices button then "New appliance template" button below the list.

2- Click the option "Add a Docker container"

3- Select new image if your machine doesn't possess the following image: 3rdvision/cogsi-nagios-gns3:latest

4- Click next and name it nagios

5- Define 2 adapters

6- Leave everything else default and click next and finish.

7- Repeat the steps 2-6 for the other two images:  3rdvision/cogsi-tidd-gns3:latest and  3rdvision/cogsi-tomcat-gns3:latest

8- Add a quagga router by repeating steps 2-6 but instead of defining 2 adapters, define 4 instead and use the image ajnouri/quagga_alpine:latest

9- Now that we have installed all needed appliances, we are ready to proceed to the creation of the architecture of the topology

### Creation of the topology web

1- Drag every component as in the image:

* 3 Quagga
* 1 Nagios
* 1 Todd
* 1 Tomcat
* 1 NAT
* 1 Ethernet Switch

2- Connect each component according to the below image eth interface

3- Select all routers with Ctrl + click in each router and click change symbol

4- Select router symbol from dialog and click ok

![](https://i.imgur.com/puzAcjC.png)

### Configuration of the quagga routers

1- Select all routers with Ctrl + click in each router and click change symbol

2- Right click and select "Start"

3- Click a blank area to disselect any previously select element

4- Right click router named quagga-1 and select "Console" option

5- Paste the following command and press enter to configure the quagga configuration using the vtysh terminal

```bash
configure terminal
router ospf
 network 192.168.1.0/24 area 0
 network 192.168.100.0/24 area 0 
 network 192.168.101.0/24 area 0 
 passive-interface eth0    
 exit
interface eth0
 ip address 192.168.1.254/24
 exit
interface eth1
 ip address 192.168.100.1/24
 exit
interface eth2
 ip address 192.168.101.2/24
 exit
exit
ip forward
write
exit 
```

6- Right click router named quagga-2 and select "Console" option

7- Paste the following command and press enter to configure the quagga configuration using the vtysh terminal

```bash
configure terminal
router ospf
 network 192.168.2.0/24 area 0.0.0.0
 network 192.168.100.0/24 area 0.0.0.0
 network 192.168.102.0/24 area 0.0.0.0
 passive-interface eth0    
 exit
interface eth0
 ip address 192.168.2.254/24
 exit
interface eth1
 ip address 192.168.100.2/24
 exit
interface eth2
 ip address 192.168.102.2/24
 exit
exit
ip forward
write
exit 
```

8- Right click router named quagga-2 and select "Console" option

9- Paste the following command and press enter to configure the quagga configuration using the vtysh terminal

```bash
configure terminal
router ospf
 network 192.168.3.0/24 area 0.0.0.0
 network 192.168.101.0/24 area 0.0.0.0
 network 192.168.102.0/24 area 0.0.0.0
 passive-interface eth0    
 exit
interface eth0
 ip address 192.168.3.254/24
 exit
interface eth1
 ip address 192.168.101.1/24
 exit
interface eth2
 ip address 192.168.102.1/24
 exit
exit
ip forward
write
exit 
```

### Configuration of the end hosts netowrk interfaces (nagios,tomcat,todd)

1- Right click nagios docker machine and click "edit config"

2- Delete anything that's inside the textbox and paste the following:

```bash
auto eth0
iface eth0 inet static
   address 192.168.1.1
   netmask 255.255.255.0
up route add -net 192.168.0.0/16 gw 192.168.1.254 dev eth0

auto eth1
iface eth1 inet static
   address 192.168.122.10
   netmask 255.255.255.0
```

3- Right click todd docker machine and click "edit config"

4- Delete anything that's inside the textbox and paste the following:

```bash
auto eth0
iface eth0 inet static
   address 192.168.2.1
   netmask 255.255.255.0
up route add -net 192.168.0.0/16 gw 192.168.2.254 dev eth0

auto eth1
iface eth1 inet static
   address 192.168.122.20
   netmask 255.255.255.0
```

5- Right click tomcat docker machine and click "edit config"

6- Delete anything that's inside the textbox and paste the following:

```bash
auto eth0
iface eth0 inet static
   address 192.168.3.1
   netmask 255.255.255.0
up route add -net 192.168.0.0/16 gw 192.168.3.254 dev eth0

auto eth1
iface eth1 inet static
   address 192.168.122.30
   netmask 255.255.255.0
```

### Configuration check for the ip addresses inside the machines

1- Repeat these steps accordingly to each one of the end hosts machines: -nagios; -todd; -tomcat.

2- Right click the end host and select "Auxiliary console" option

3- Make the below checks in each one of different hosts

#### Nagios

- Right click and select "Show in file manager"
- Navigate to /opt/nagios/etc/
- Copy all files from the provided nagios-etc/ which are also in /home/dan/nagios-copy path of the container machine and paste them into this folder and select the option to overrite every occurence
- Check if server_address property of file /etc/nsca.cfg is set to 192.168.1.1 to bind nsca for the  passive checks to the right address
- If the address is different, change it and restart nsca service with service nsca restart
- If nagios pluggin for auto discovery is enabled, if not, install it from this link https://exchange.nagios.org/directory/Plugins/Network-and-Systems-Management/Nagios/check_find_new_hosts/details
- If the /opt/nagios/etc/vclones.cfg has all the following needed hosts and services:

```bash
###############################################################################
# LOCALHOST.CFG - SAMPLE OBJECT CONFIG FILE FOR MONITORING THIS MACHINE
#
## NOTE: This config file is intended to serve as an *extremely* simple
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

    parents                 quagga3
    host_name               tomcat
    alias                   tomcat
    address                 192.168.3.1  
    use                     linux-server       
    contact_groups          CogsiGroup1     
}

define host {

    parents                 quagga2
    host_name               todd
    alias                   todd
    address                 192.168.2.1  
    use                     linux-server    
    contact_groups          CogsiGroup1     
}

define host {

    parents                 localhost
    host_name               quagga1
    alias                   quagga1
    address                 192.168.1.254
    use                     linux-server 
    contact_groups          CogsiGroup1     
}


define host {

    parents                 quagga1
    host_name               quagga2
    alias                   quagga2
    address                 192.168.2.254
    use                     linux-server 
    contact_groups          CogsiGroup1     
}

define host {

    parents                 quagga1
    host_name               quagga3
    alias                   quagga3
    address                 192.168.3.254
    use                     linux-server 
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
    host_name               tomcat
    service_description     PING
    check_command           check_ping!100.0,20%!500.0,60%
}


define service {

    use                     local-service           ; Name of service template to use
    host_name               quagga1
    service_description     PING
    check_command           check_ping!100.0,20%!500.0,60%
}


define service {

    use                     local-service           ; Name of service template to use
    host_name               quagga2
    service_description     PING
    check_command           check_ping!100.0,20%!500.0,60%
}


define service {

    use                     local-service           ; Name of service template to use
    host_name               quagga3
    service_description     PING
    check_command           check_ping!100.0,20%!500.0,60%
}



# Define a service to check the number of currently logged in
# users on the local machine.  Warning if > 20 users, critical
# if > 50 users.

define service {

    use                     local-service           ; Name of service template to use
    host_name               tomcat
    service_description     Current Users Guest
    check_interval          1
    check_command           check_nrpe!check_users -a 15 20
}

 

# Define a service to check the number of currently running procs
# on the local machine.  Warning if > 250 processes, critical if
# > 400 processes.

define service {

    use                     local-service           ; Name of service template to use
    host_name               tomcat
    check_interval          1    
    service_description     Total Processes Guest
    check_command           check_nrpe!check_procs -a 250 400 RSZDT
}



# Define a service to check the load on the local machine.

define service {

    use                     local-service           ; Name of service template to use
    host_name               tomcat
    check_interval          1
    service_description     Current Load Guest
    check_command           check_nrpe!check_load -a 5.0,4.0,3.0 10.0,6.0,4.0
}

# Define a service to check SSH on the local machine.
# Disable notifications for this service by default, as not all users may have SSH enabled.

define service {

    use                     local-service           ; Name of service template to use
    host_name               tomcat
    service_description     SSH
    check_command           check_ssh
    notifications_enabled   0
}

# My tomcat HTTP service on port 8080

define service {

        use                             local-service
        host_name                       tomcat
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
        host_name                       todd
        service_description             Todd-Up-Service
        check_command                   check_todd_up
        check_interval                  1
        max_check_attempts              4
        contacts                        daniel1         
}

# My todd load service

define service {

        use                             local-service
        host_name                       todd
        service_description             Todd-Load-Service
        check_command                   check_todd_load
        check_interval                  1
        max_check_attempts              4
        contacts                        daniel1         
}

define service{
        use                             passive-service
        host_name                       todd        
        service_description             todd-passive-up
        max_check_attempts              1
        event_handler_enabled           1
        event_handler                   grow_todd        
        _grow_number                    12        
}

```

#### Tomcat

- Check if /etc/nagios/nrpe.cfg file has the right IP address set to 192.168.1.1 in allowed hosts property
- If the address is different, change it and restart nsca service with service nagios-nrpe-server restart
- Check if the script /usr/local/tomcat/bin/setenv.sh has the right address to bind JMX to
- If the address is different, change it and restart the machine

#### Todd

- Check if /etc/nagios/nrpe.cfg file has the right IP address set to 192.168.1.1 in allowed hosts property
- If the address is different, change it and restart nsca service with service nagios-nrpe-server restart
- Check if the build gradle groovy script has the right ip addresses to bind in all the tasks at /home/dan/todd-cogsi/build.gradle
- If the address is different, change it and restart the machine
- Check if the JMX application of todd has the right addresses and hostname at /home/dan/todd-cogsi/src/main/java/net/jnjmx/todd/JMXNotificationListener.java 
- If the address or hostname is different, change them and restart the machine to apply the changes

4- If everything checks, go to Control>Stop all nodes followed by Control>Start all nodes to restart every end host and router in the network

5- Navigate to 192.168.122.10 in your host computer browser and login with nagiosadmin/nagios and the network should be setted up correctly.

# References

* DEI-ISEP slides for Configuração e Gestão de Sistemas 2018/2019
* https://www.brianlinkletter.com/how-to-build-a-network-of-linux-routers-using-quagga/
* https://www.brianlinkletter.com/using-open-source-routers-in-gns3/