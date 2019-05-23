STUDENT **Daniel Azevedo** (1180109) - 2.1
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

[//]: # (optional image here)

---

# Problem Analysis

## Notes


## Objectives

* To simulate an integrated "devOps" scenario

## Requirements

### Continous Integration

* Use Jenkins to implement a pipeline to build the TODD application

### Continous Delivery

* Successful builds of the application should result in the application being archived in the Nexus artifact repository
* Deploy the application to, at least, two different hosts inthe local network (using Ansible)
* Include a stage in the pipeline that, upon user confirmation, executes the Ansible deployment of the application

### Network

* All the hosts should be connected
* The network should include two hosts: one with TODD and another with TODD and Nagios

---

# Analysis of the alternative (different aproaches to the pipeline)


# Solution Design

---

# Steps to reproduce


## Installation of tooling




# References


* 