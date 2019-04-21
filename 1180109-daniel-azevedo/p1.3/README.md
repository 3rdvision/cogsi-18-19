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

* Use docker as a replacement of virtual machines for doing the same as the previous assignments

## Requirements:

* Having a monitor container using Nagios

* Having a monitored container using TODD sample application

* Having a monitored container with tomcat

* Implement passive and active checks (with NRPE and JMX) in a similar way to PL1.2

### Alternative

* Design the same schenario but using linux namespaces instead of docker

* For the implementation, do the containerization of Todd

---

# Solution Design

The host machine was running an Arch-based Linux OS with kernel version 4.19.32-1-MANJARO.

## Containers

## 1- Nagios

### Base image: jasonrivers/nagios:latest

### Dockerfile: No

### Network ip address: 172.18.0.2

### Used network ports:
* 80:80 - Nagios
* 6002 - JMX TODD
* 6003 - JMX Tomcat
* 6668 - NRPE port
* 6667 - NSCA port

### Extra requirements:
* NRPE
* sendEmail
* configuration files
* gradle
* git
* openjdk-8-jdk-headless
* nsca

TODD services in the right folders

## 2- TODD

### Dockerfile: Yes

### Base image: ubuntu:latest

### Network ip address: 172.18.0.3

### Extra requirements:

* gradle
* openjdk-8
* openjdk-8-jdk-headless
* gradle
* wget
* git
* ssh
* nagios-nrpe-server
* vim
* nsca
* sudo
* TODD
* JMX (TODD src repository)

### Ports:

* 6002 - JMX TODD
* 6667 - NSCA port

## 3- Tomcat 8 (172.18.0.4)

### Dockerfile: Yes

### Base image: ubuntu:latest

### Ports:

* 8080:8080 -- tomcat web port
* 6668 - NRPE port
* 6003 - JMX Tomcat
* JMX enabled config file startup (add that file to the etc of tomcat)

### Extra requirements:

* gradle
* openjdk-8
* TODD
* vim
* sudo
* git
* ssh
* nagios-nrpe-server
* vim
* nsca
* sudo
* JMX (TODD src repository)

This way, the 3 containers would start and would be operational with no additional commands!

In the end of the configuration of nagios, the 3 docker image were pushed to docker hub with the following images:

* 3rdvision/cogsi-nagios
* 3rdvision/cogsi-tomcat
* 3rdvision/cogsi-todd

The only commands to reproduce this tutorial in ** any computer in the world ** with docker and internet access is:
```bash
docker network create --subnet=172.18.0.0/16 mynet
docker run --net mynet --net mynet --ip 172.18.0.2 -d --name nagios -v /path/to/nagios/etc/:/opt/nagios/etc/ -p 0.0.0.0:8080:80 3rdvision/cogsi-nagios
docker run --net mynet --net mynet --ip 172.18.0.3 -d --name todd 3rdvision/cogsi-todd
docker run --net mynet --net mynet --ip 172.18.0.4 -d --name tomcat 3rdvision/cogsi-tomcat
```
In the end, the repositories can be started with an elegant oneliner:

`docker start nagios todd tomcat`

This solution also allows for the host machine to access any of the JMX services (TODD and Tomcat).

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

5- Enter inside the container `docker exec -it nagios bash`

update repository list
`apt update`

install needed packages
`apt install vim gradle git openjdk-8-jdk-headless nsca ssh`

start the SSH service
`etc/init.d/ssh start`

6- Inside the container run `vim /opt/nagios/nagios/etc/nagios.cfg`

7- Afterwards add the line to add vclone services as a new object that nagios will scan

```bash
cfg_file=/usr/local/nagios/etc/objects/vclones.cfg
```

8- Copy the vclones.cfg file located in configuration_files of this repository to /usr/local/nagios/etc/objects/vclones.cfg

9- Copy the templates.cfg file located in configuration_files of this repository to  /usr/local/nagios/etc/objects/templates.cfg

10- Copy the commands.cfg file located in configuration_files of this repository to  /usr/local/nagios/etc/objects/commands.cfg

11- Copy the contacts.cfg file located in configuration_files of this repository to  /usr/local/nagios/etc/objects/contacts.cfg

12- Give execution and run permissions to every single created file ith `cd /opt/nagios/libexec && chmod +rx check_todd_load check_todd_load check_todd_load check_todd_load`

13- Uncomment and edit nsca config file to define nagios IP address

Uncomment and edit the ip address to match nagios's
```bash
server_address=172.18.0.2
```
Edit the command file to match nagios output file
```bash
command_file=/opt/nagios/var/rw/nagios.cmd
```

14- Restart nsca service `service nsca restart`

15- Restart with the command /etc/init.d/nagios restart

16- Verify nagios configuration for errors by executing `/opt/nagios/bin/nagios -v /opt/nagios/etc/nagios.cfg`

17- Copy sendEmail pearl executable file into /usr/local/bin and give execution and read permissions with `chmod +rx /usr/local/bin/sendEmail`

18- Restart nagios by going to the web interface by navigating to 172.18.0.2 and cliking "Process Info" > "Restart Nagios process"

19- Create the TODD executable check commands by creating the executable java files

```bash
mkdir /home/dan/
cd /home/dan && git clone https://dvazevedo@bitbucket.org/dvazevedo/todd-cogsi.git && cd todd-cogsi && gradle build
```

20- You can now exit the bash by typing `exit` and if you wish stop the nagios container using `docker stop nagios` for configuring the other containers. Later we can start this same container only by typing docker start nagios

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
RUN apt-get -y install openjdk-8-jdk gradle wget git ssh nagios-nrpe-server vim nsca sudo
RUN mkdir /home/dan/
RUN cd /home/dan && git clone https://dvazevedo@bitbucket.org/dvazevedo/todd-cogsi.git
RUN cd /home/dan/todd-cogsi && gradle build
CMD cd /home/dan/todd-cogsi && service nsca start && service ssh start && service nagios-nrpe-server start && ( gradle runserverremote & ) && sleep 3 && ( gradle runclient3 & ) && tail -f /dev/null 
```

2- Build the image using docker build command

```bash
cd ~
docker build -t my-todd:v1 .
```

3- `docker run --net mynet --ip 172.18.0.3 -d --name todd -td my-todd:v1`

# Alternative: Linux namespaces

Starting from kernel 2.6.24, Linux supports 6 different types of namespaces. Namespaces are useful in creating processes that are more isolated from the rest of the system, without needing to use full low level virtualization technology. The feature works by having the same name space for these resources in the various sets of processes, but those names referring to distinct resources. Examples of resource names that can exist in multiple spaces, so that the named resources are partitioned, are process IDs, hostnames, user IDs, file names, and some names associated with network access, and interprocess communication.

Namespaces are a fundamental aspect of containers on Linux.

The term "namespace" is often used for a type of namespace (e.g. process ID) as well for a particular space of names.

A Linux system starts out with a single namespace of each type, used by all processes. Processes can create additional namespaces and join different namespaces. 

Every time a computer with Linux boots up, it starts with just one process, with process identifier (PID) 1. This process is the root of the process tree, and it initiates the rest of the system by performing the appropriate maintenance work and starting the correct daemons/services. All the other processes start below this process in the tree. The PID namespace allows one to spin off a new tree, with its own PID 1 process. The process that does this remains in the parent namespace, in the original tree, but makes the child the root of its own process tree.

With PID namespace isolation, processes in the child namespace have no way of knowing of the parent process’s existence. However, processes in the parent namespace have a complete view of processes in the child namespace, as if they were any other process in the parent namespace.


![](https://uploads.toptal.io/blog/image/674/toptal-blog-image-1416487554032.png)

It is possible to create a nested set of child namespaces: one process starts a child process in a new PID namespace, and that child process spawns yet another process in a new PID namespace, and so on.

With the introduction of PID namespaces, a single process can now have multiple PIDs associated with it, one for each namespace it falls under. In the Linux source code, we can see that a struct named pid, which used to keep track of just a single PID, now tracks multiple PIDs through the use of a struct named `upid`:

```c
struct upid {
  int nr;                     // the PID value
  struct pid_namespace *ns;   // namespace where this PID is relevant
  // ...
};

struct pid {
  // ...
  int level;                  // number of upids
  struct upid numbers[0];     // array of upids
};
```

To create a new PID namespace, one must call the clone() system call with a special flag CLONE_NEWPID. (C provides a wrapper to expose this system call, and so do many other popular languages.) Whereas the other namespaces discussed below can also be created using the unshare() system call, a PID namespace can only be created at the time a new process is spawned using clone(). Once clone() is called with this flag, the new process immediately starts in a new PID namespace, under a new process tree. This can be demonstrated with a simple C program

```c
#define _GNU_SOURCE
#include <sched.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <unistd.h>

static char child_stack[1048576];

static int child_fn() {
  printf("PID: %ld\n", (long)getpid());
  return 0;
}

int main() {
  pid_t child_pid = clone(child_fn, child_stack+1048576, CLONE_NEWPID | SIGCHLD, NULL);
  printf("clone() = %ld\n", (long)child_pid);

  waitpid(child_pid, NULL, 0);
  return 0;
}
```
Compile and run this program with root privileges and you will notice an output that resembles this:
```bash

clone() = 5304
PID: 1
```

The PID, as printed from within the child_fn, will be 1.

Even though this namespace tutorial code above is not much longer than “Hello, world” in some languages, a lot has happened behind the scenes. The clone() function, as you would expect, has created a new process by cloning the current one and started execution at the beginning of the child_fn() function. However, while doing so, it detached the new process from the original process tree and created a separate process tree for the new process.

![](https://uploads.toptal.io/blog/image/677/toptal-blog-image-1416545619045.png)



# References

* DEI-ISEP slides for Configuração e Gestão de Sistemas 2018/2019
* https://www.toptal.com/linux/separation-anxiety-isolating-your-system-with-linux-namespaces
* https://docs.docker.com/v17.12/docker-cloud/builds/push-images/ -- how to push images
* https://stackoverflow.com/questions/17157721/how-to-get-a-docker-containers-ip-address-from-the-host -- get IP of a container