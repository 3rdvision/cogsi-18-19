STUDENT **Daniel Azevedo** (1180109) - P1.5
===============================
[//]: # (EDIT HERE!! /\)

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

## Notes

* It is mandatory to use Ansible;
* The system monitoring tool should be nagios;
* In case of VMs (recommended), use Vagrant to manage them.

## Objectives

* Simulate the provision of several hosts connected in an small local network.
* Deploy software to several hosts (should be simulated VMs)
* Configure hosts

## Requirements

### Network

* All hosts should be connected in a small local network;
* The hosts should also have access to the internet.

### Ansible

* Provision a set (at least 2) of hosts with a software package you developed (for instance the Todd server application);
* Provision one of the hosts with tomcat;
* Provision all hosts (except nagios) with remove monitoring agents (eg. NRPE);
* Configure the NRPE agent in the hosts;
* Configure, partially, some aspect of Nagios (Nagios should monitor all the elements of the network);
* Use Nagios Ansible Module to configure one example of downtime for one of the hosts (See https://docs.ansible.com/ansible/latest/modules/nagios_module.html)

## Nagios

* Monitor all the hosts in the network except the host running Ansible (use a monitoring configuration similar to the previous assignments)

---

# Solution Design



---

# Steps to reproduce



# References



* 