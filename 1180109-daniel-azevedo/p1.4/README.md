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

### Configure Router 1

x- Configure quagga accordingly using vtysh `$ vtysh`
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

# References

* DEI-ISEP slides for Configuração e Gestão de Sistemas 2018/2019