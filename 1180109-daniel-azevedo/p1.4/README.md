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


## Requirements:


### Alternative


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