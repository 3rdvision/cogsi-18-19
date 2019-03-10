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

![](https://i.imgur.com/7PBJ2np.png)

# Problem Analysis

## Objective

* To simulate a simple monitoring

### 1- Remote Monitoring 

* The monitoring of some properties (e.g., disk free space) may require the installation
of software in the monitored machine

* With Nagios one of the solutions for this issue is to use NRPE

* The monitoring can be done by "pulling" or by "pushing". These may also be
known as passive or active checks

### 2- Automatic Recovery

* You should install Tomcat in the monitored machine
* The Monitoring Server should try to automatically recover the Tomcat server when it is down. In Nagios, please refer to "event handlers" to support your solution
* Contact(s) should be notified by email when the service changes states (e.g., up, down, etc,). In Nagios you may use the sendemail.

### 3- Customization

* How the monitoring tool can be customized (e.g., using a different database, adding new features/plugins, setting different compiling options for optimization or security purposes, for instance, regarding NRPE)

---

# Solution design (Nagios)

## Overview of the design

The solution was implemented and tested in a virtual environment of two remotelly connected machines using VirtualBox software.

The machines in this virtual environment were configured with 2.5GB of RAM 10 GB of HDD and set to run Ubuntu 18.04 LTS.

A virtualbox host-only ethernet adapter was set 192.168.99.1/8 and both machines had a NAT adapter and a host-only adapter attached to this virtual network.

* Machine1 (nagios) - Monitoring machine running nagios
* Machine2 (vclone1) - To be monitored with nagios agent (NRPE)

In Machine1, nagios core was installed with pluggins along with all it's dependencies.

Other scripts and nagios pluggins were also added in order to satisfy the requirements as detailed in the section "Steps to reproduce".

In Machine2, a Tomcat 9 service was installed to run on port 8080.

Machine1 nagios was configured to monitor the following Machine2's services:

* Check Swap Guest
* Current Load Guest
* Current Users Guest
* HTTP-80
* HTTP-Tomcat-8080
* PING
* Root Partition Guest
* SSH

Notifications was set for all of the previous services and an e-mail was sent to the defined contacts 24hx7.

Auto recovery for the HTTP-8080-Tomcat service was implemented in a way that every time the service changed state to CRITICAL and it would be the 2nd time, an intent to restart the tomcat server was sent through NRPE tunnel.
Machine2 in receiving that intent, would run "sudo service  tomcat restart".

## Technical nagios deign aspects

### Monitoring remote server properties through NRPE

Monitoring of remote properties such as disk free space, cpu load, etc. will be done using NPRE.

The NRPE addon consists of two pieces:

* The check_nrpe plugin, which resides on the local monitoring machine
* The NRPE daemon, which runs on the remote Linux/Unix machine

![](https://i.imgur.com/Ldtplu5.png)

When Nagios needs to monitor a resource of service from a remote Linux/Unix machine:

* Nagios will execute the check_nrpe plugin and tell it what service needs to be checked
* The check_nrpe plugin contacts the NRPE daemon on the remote host over an (optionally) SSL protected connection
* The NRPE daemon runs the appropriate Nagios plugin to check the service or resource
* The results from the service check are passed from the NRPE daemon back to the check_nrpe plugin, which then returns the check results to the Nagios process.

## Customization of Nagios & Other important notes

### Apearance and custom interfaces

There is a possibility to change nagio's frontends web appearance, the look and feel of the Nagios Core CGIs by installing the different themes, web interfaces and even enabling responsive designs for mobile web browsing using different online theme solutions.

Among other different sources of themes:

* The official nagios website: https://exchange.nagios.org/directory/Addons/Frontends-(GUIs-and-CLIs)

Theme example:
![](https://exchange.nagios.org/components/com_mtree/img/listings/m/605.png)

Each Addon should be installed in a particular way and most of them have instructions on nagios webpage, a link to an external documentation or the installation steps in the README.

### Security and NRPE

#### Good security pracices:

* When passing arguments through NRPE ($ARG1$) try avoiding the use of extensive arguments, in a case the monitoring machine gets compromised.

* Don't Run Nagios As Root

* Lock Down The Check Result Directory. Make sure that only the nagios user is able to read/write in the check result path.

* Require Authentication In The CGIs

* Secure Access to Remote Agents. Make sure you lock down access to agents (NRPE, NSClient, SNMP, etc.) on remote systems using firewalls, access lists, etc. You don't want everyone to be able to query your systems for status information.

* Secure Communication Channels. Make sure you encrypt communication channels between different Nagios installations and between your Nagios servers and your monitoring agents whenever possible.

```bash
command_line $USER1$/check_nrpe -H $HOSTADDRESS$ -c $ARG1$
```

### Different database

Nagios Core doesn't run a database. On the other hand, nagios XL, the premium paid version of nagios, allows the customization of the database and even offloading mysql to a remote server.

The configuration of the type of database should be in a file `/usr/local/nagiosxi/html/config.inc.php` where it's stated in a line the dbtype: `"dbtype" => 'mysql'`.

To adjust Nagios XL server settings to use an external database we must edit 3 confioiguration files: `ndo2db.cfg` `config.inc.php` and `settings.php`. Also it's important to modify the backup script, located by default in `/root/scripts/automysqlbackup`and changing `DBHOST=localhost` to `DBHOST=<IP_OF_MYSQL_OR MARIADB_SERVER>`.

### Performance Tweaks

* Large installation tweak options

This option determines whether or not the Nagios daemon will take several shortcuts to improve performance. These shortcuts result in the loss of a few features, but larger installations will likely see a lot of benefit from doing so.

To enable it, edit nagios config file and enable the option 
`use_large_installation_tweaks=1`

There are 3 major effects by enabling this tweak:

1- No Summary Macros In Environment Variables - The summary macros will not be available to you as environment variables. Calculating the values of these macros can be quite time-intensive in large configurations, so they are not available as environment variables when use this option. Summary macros will still be available as regular macros if you pass them to to your scripts as arguments.

2- Different Memory Cleanup - Normally Nagios will free all allocated memory in child processes before they exit. This is probably best practice, but is likely unnecessary in most installations, as most OSes will take care of freeing allocated memory when processes exit. The OS tends to free allocated memory faster than can be done within Nagios itself, so Nagios won't attempt to free memory in child processes if you enable this option.

3- Checks fork() Less - Normally Nagios will fork() twice when it executes host and service checks. This is done to (1) ensure a high level of resistance against plugins that go awry and segfault and (2) make the OS deal with cleaning up the grandchild process once it exits. The extra fork() is not really necessary, so it is skipped when you enable this option. As a result, Nagios will itself clean up child processes that exit (instead of leaving that job to the OS). This feature should result in significant load savings on your Nagios installation.

* Using passive checks when possible.

* Avoid using interpreted plugins (compiled C/C++, etc) or interpreted (like Pearl or Python) instead of shell scripts.

* Disabling the option to use agressive host checking in the host checking options `use_aggressive_host_checking=0`

* Using nagiostats utility to allow to graph various Nagios performance statistics over time using MRTG

Graphs like the one below will provide important statistics information for what can be tunned for a better performance.
![](https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/3/en/images/mrtg-activehostchecks.png)

---

# Alternative comparison (Zabbix)

## Zabbix Software Overview

Zabbix is also an open-source monitoring software for diverse componentes such as networks, servers, virtual machines (VMs) and cloud services. 

Zabbix provides monitoring metrics, among others network utilization, CPU load and disk space consumption. Zabbix monitoring configuration can be done using XML based templates which contains elements to monitor.

Zabbix can use MySQL, MariaDB, PostgreSQL, SQLite, Oracle or IBM DB2 to store data. Its backend is written in C and the web frontend is written in PHP.

## Key Features

* High performance, high capacity (able to monitor hundreds of thousands of devices).
* Auto-discovery of servers and network devices and interfaces.
* Low-level discovery, allows to automatically start monitoring new items, file systems or network interfaces among others.
* Distributed monitoring with centralized web administration.
* Native high performance agents (client software for Linux, Solaris, HP-UX, AIX, FreeBSD, OpenBSD, OS X, Tru64/OSF1, Windows 2000, Windows Server 2003, Windows XP, Windows Vista, Windows Server 2008, Windows 7)
* SLA, and ITIL KPI metrics on reporting.
* High-level (business) view of monitored resources through user-defined visual console screens and dashboards.
* Agent or Agent-less monitoring capabilities.
* Web-based interface.
* Support for both polling and trapping mechanisms.
* JMX monitoring
* Web monitoring
* Flexible e-mail notification on predefined events.
* Near-real-time notification mechanisms, for example using including XMPP protocol

## Software architecture 

Zabbix archicture and different communication mechanisms
![](https://i.imgur.com/q3W0XAw.png)

## Nagios Core vs Zabbix comparison

According to alternativeto.net, Nagios and Zabbix are the two most popular monitoring systems softwares so it's important to compare them.
Depending on the sysadmin priorities and personal taste, one software can be more intersting than the other.
Below is a side-by-side comparison tabble that compares the key componentes of both monitoring solutions.

![](https://i.imgur.com/TN8m1l3.png)
![](https://i.imgur.com/g3uUf7m.png)

source: https://www.comparitech.com/net-admin/nagios-vs-zabbix/#Dashboard_and_User_Interface

---

# Steps to reproduce (Nagios)

## Installation and Execution Environments

1- Inside a virtual machine guest with 2 GB RAM and 10 GB disk Install Ubuntu Server 18.04 LTS

2- Clone the machine created at step 1 to later use as hosts of nagios.

3- Install nagios core 4 on machine created on step 1 and all it's dependencies (refer to nagios 4.0 documentation)

4- Install nagios pluggins 2.2.1 (refer to doc) and it's package requirements: `sudo apt install autoconf gcc libc6 libmcrypt-dev makelibssl-dev wget bc gawk dc build-essential snmp libnet-snmp-perlgettext`

## Start nagios and open nagios web interface

1- Start nagios service using systemd init system manager `sudo systemctl start nagios`

2- (Optional) Enable the service to startup at boot with  `sudo systemctl enable nagios`

3- Check if nagios service is up and running `sudo systemctl status nagios`

4- Find out guests ip address `ip addr show`

5- In the host machine nagivate to http://nagios-vguest-ip/nagios

6- Login using user: nagiosadmin and the password defined in the installation of nagios core.

## Install Tomcat 9 on cloned machine

1- Install OpenJDK on the cloned virtual machine `sudo apt update && sudo apt install default-jdk`

2- Create Tomcat user `sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat`

3- Install Tomcat9:

`wget http://www-eu.apache.org/dist/tomcat/tomcat-9/v9.0.16/bin/apache-tomcat-9.0.16.tar.gz -P /tmp`
`sudo tar xf /tmp/apache-tomcat-9*.tar.gz -C /opt/tomcat`
`sudo ln -s /opt/tomcat/apache-tomcat-9.0.14 /opt/tomcat/latest`
`sudo chown -RH tomcat: /opt/tomcat/latest`
`sudo sh -c 'chmod +x /opt/tomcat/latest/bin/*.sh'`

4- Create systemd unit file `sudo nano /etc/systemd/system/tomcat.service`

Paste the following configuration (change JAVA_HOME path if necessary):

```bash
[Unit]
Description=Tomcat 9 servlet container
After=network.target

[Service]
Type=forking

User=tomcat
Group=tomcat

Environment="JAVA_HOME=/usr/lib/jvm/default-java"
Environment="JAVA_OPTS=-Djava.security.egd=file:///dev/urandom -Djava.awt.headless=true"

Environment="CATALINA_BASE=/opt/tomcat/latest"
Environment="CATALINA_HOME=/opt/tomcat/latest"
Environment="CATALINA_PID=/opt/tomcat/latest/temp/tomcat.pid"
Environment="CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC"

ExecStart=/opt/tomcat/latest/bin/startup.sh
ExecStop=/opt/tomcat/latest/bin/shutdown.sh

[Install]
WantedBy=multi-user.target
```

5- Reload the systemd deamon `sudo systemctl daemon-reload`

6- Start Tomcat 9 service `sudo systemctl start tomcat`

7- In the host machine nagivate to http://nagios-vguest-ip:8080 and check if Tomcat9 welcome page comes up

## Configure nagios to do remote monitoring of the cloned machine on Tomcat

1- Create a new object file called vclones.cfg at `/usr/local/nagios/etc/objects/tutorial.cfg` with the following configuration (pay atention to CHANGE THIS):

```bash
## Hosts
define host {

    use                     linux-server    
    host_name               vclone1
    alias                   vclone1
    address                 172.18.132.239 # CHANGE THIS
}

## Services

# Define a service to "ping" the local machine

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    service_description     Current Users
    check_command           check_local_users!20!50
}



# Define a service to check the number of currently running procs
# on the local machine.  Warning if > 250 processes, critical if
# > 400 processes.

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    service_description     Total Processes
    check_command           check_local_procs!250!400!RSZDT
}



# Define a service to check the load on the local machine.

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    service_description     Current Load
    check_command           check_local_load!5.0,4.0,3.0!10.0,6.0,4.0
}



# Define a service to check the swap usage the local machine.
# Critical if less than 10% of swap is free, warning if less than 20% is free

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    service_description     Swap Usage
    check_command           check_local_swap!20%!10%
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
}


```
2- Modify nagios.cfg to include the created vclones.cfg object by appending the line `cfg_file=/usr/local/nagios/etc/objects/vclones.cfg`

3- Restart nagios service.

## Create contacts in nagios and notify services by e-mail (using Gmail)

1- Create a gmail account and in that account, enable the account access for less secure apps in security options of gmail.

2- Install pre-requisites `apt-get install libio-socket-ssl-perl libnet-ssleay-perl perl`

3- Download sendEmail `apt-get install libio-socket-ssl-perl libnet-ssleay-perl perl` 

4- Install sendEmail

Extract
`tar -zxvf sendEmail-v1.56.tar.gz`

Copy the script to user bin
`sudo cp -a sendEmail-v1.XX/sendEmail /usr/local/bin`

Give execution permissions for that script
`sudo chmod +x /usr/local/bin/sendEmail`

Test it
`sendEmail`

5- Add to the Nagios Core resources the e-mail details `vi /usr/local/nagios/etc/resource.cfg`

Append the following configuration:
```bash
# gmail account
$USER5$=your_email@gmail.com
$USER7$=smtp.gmail.com:587
$USER9$=your_email@gmail.com
$USER10$=your_password
```

6- Comment previous `notify-host-by-email` and `notify-service-by-email` commands and append the following ones:

```bash
define command{
	command_name	notify-host-by-email
	command_line	/usr/bin/printf "%b" "***** Nagios *****\n\nNotification Type: $NOTIFICATIONTYPE$\nHost: $HOSTNAME$\nState: $HOSTSTATE$\nAddress: $HOSTADDRESS$\nInfo: $HOSTOUTPUT$\n\nDate/Time: $LONGDATETIME$\n" | /usr/local/bin/sendEmail -s $USER7$ -xu $USER9$ -xp $USER10$ -t $USER5$ -f $USER5$ -o tls=yes -l /var/log/sendEmail -u "** $NOTIFICATIONTYPE$ Host Alert: $HOSTNAME$ is $HOSTSTATE$ **" -m "***** Nagios *****\n\nNotification Type: $NOTIFICATIONTYPE$\nHost: $HOSTNAME$\nState: $HOSTSTATE$\nAddress: $HOSTADDRESS$\nInfo: $HOSTOUTPUT$\n\nDate/Time: $LONGDATETIME$\n"
}
 
define command{
	command_name	notify-service-by-email
	command_line	/usr/bin/printf "%b" "***** Nagios *****\n\nNotification Type: $NOTIFICATIONTYPE$\n\nService: $SERVICEDESC$\nHost: $HOSTALIAS$\nAddress: $HOSTADDRESS$\nState: $SERVICESTATE$\n\nDate/Time: $LONGDATETIME$\n\nAdditional Info:\n\n$SERVICEOUTPUT$" | /usr/local/bin/sendEmail -s $USER7$ -xu $USER9$ -xp $USER10$ -t $CONTACTEMAIL$ -f $USER5$ -o tls=yes -l /var/log/sendEmail -u "** $NOTIFICATIONTYPE$ Service Alert: $HOSTALIAS$/$SERVICEDESC$ is $SERVICESTATE$ **" -m "***** Nagios *****\n\nNotification Type: $NOTIFICATIONTYPE$\n\nService: $SERVICEDESC$\nHost: $HOSTALIAS$\nAddress: $HOSTADDRESS$\nState: $SERVICESTATE$\n\nDate/Time: $LONGDATETIME$\n\nAdditional Info:\n\n$SERVICEOUTPUT$"
}
```

7- Create a contact by appending this to contacts.cfg

```bash
define contact {
        contact_name                            contact1
        alias                                   YourName
        email                                   your_email@gmail.com
        service_notification_period             24x7
        service_notification_options            w,u,c,r,f,s
        service_notification_commands           notify-service-by-email
        host_notification_period                24x7
        host_notification_options               d,u,r,f,s
        host_notification_commands              notify-host-by-email
}
```

8- Append to the hosts and services block in the `vclones.cfg` file that you want to get notified to that contact
```bash
define service {
        use                             local-service
        host_name                       vclone1
        service_description             HTTP-Tomcat-8080
        check_command                   check_http_port
        check_interval                  1
        _port_number                    8080
        contacts                        contact1         
}
```

9- Edit `templates.cfg` to delete line 81 `contact_groups                  admins             ; Notifications get sent to the admins by default` so that admins stop being a default group of notifiations when a host becomes CRITICAL.

10- Repeat the previous step but for the service template and delete the line  `contact_groups                  admins             ; Notifications get sent to the admins by default` so that admins stop being a default group of notifiations when a service becomes CRITICAL.

10- Restart nagios service.

11- Try to put down tomcat service or manually issue a custom notification targeted to the HTTP-Tomcat service from the web nagios configuration panel.

## Configure cloned machine to be monitored by properties (e.g. disk free space, current logged users, cpu load, etc.)

### Configure NRPE agent on the guest (cloned) machine

1- Defined a static ip address for the virtual machine by adding another network interface "host-only adapter" and define a static network adapter in virtualbox global tools. The first network interface to connect to the internet should be set as NAT.

2- Configure netplan configuration yaml to be set as:

/etc/netplan/01-netcfg.yaml
```yaml
network:
    renderer: networkd
    version: 2
    ethernets:
        enp0s3:
            dhcp4: yes
        enp0s8:
            addresses: [192.168.99.101/24]
            dhcp4: no
            dhcp6: no
            nameservers:
                    addresses: [8.8.8.8,8.8.4.4]
```

and remove any other unwanted configuration files.

4- Resconfigure netplan with `sudo netplan --debug apply`

5- Install NRPE in the cloned virtual machine

Run all below commands as root
`sudo su`

Download NRPE
`wget http://assets.nagios.com/downloads/nagiosxi/agents/linux-nrpe-agent.tar.gz`

Unpack and run the install script
```bash
tar xzf linux-nrpe-agent.tar.gz
cd linux-nrpe-agent
./fullinstall
```

6- When promt, insert the static ip address of the nagios manager machine.

7- Uncomment and edit the sample nrpe commands `sudo vim /usr/local/nagios/etc/nrpe.cfg`


Append the following command definitions:
```bash
command[check_users]=/usr/local/nagios/libexec/check_users -w $ARG1$ -c $ARG2$
command[check_load]=/usr/local/nagios/libexec/check_load -w $ARG1$ -c $ARG2$
command[check_disk]=/usr/local/nagios/libexec/check_disk -w $ARG1$ -c $ARG2$ -p $ARG3$
command[check_procs]=/usr/local/nagios/libexec/check_procs -w $ARG1$ -c $ARG2$ -s $ARG3$
command[check_swap]=/usr/local/nagios/libexec/check_swap -w 20% -c 10%
```

### Install the NRPE pluggin on the host monitoring machine

1- Download nrpe pluggin `wget https://github.com/NagiosEnterprises/nrpe/releases/download/nrpe-3.2.1/nrpe-3.2.1.tar.gz`

2- Navigate to folder `cd nrpe-*/`

3- Install the plugin

Configure 
`./configure`

Compile check_nrpe
`make check_nrpe`

Install NRPE plugin
`make install-plugin`

4- Test the communication between monitoring machine -> virtual clone guest machine
`/usr/local/nagios/libexec/check_nrpe -H 192.168.99.102`

The output should be the NRPE version installed in the guest machine
`NRPE v3.2.1`

5- Add the command to check_nrpe to `commands.cfg` file

```bash
define command{
	command_name 	check_nrpe
	command_line 	$USER1$/check_nrpe -H $HOSTADDRESS$ -c $ARG1$
}
```

6- Edit the monitoring property services to monitor the remote server using NRPE agent and with a check interval of 1 minute

```bash
define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    check_interval          1
    service_description     Current Load
    check_command           check_nrpe!check_load -a 5.0,4.0,3.0 10.0,6.0,4.0
}

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    check_interval          1    
    service_description     Total Processes
    check_command           check_local_procs!250!400!RSZDT
}

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    check_interval          1    
    service_description     Check Swap Guest
    check_command           check_nrpe!check_swap
}

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    check_interval          1
    service_description     Root Partition
    check_command           check_nrpe!check_disk -a 20% 10% /
}

define service {

    use                     local-service           ; Name of service template to use
    host_name               vclone1
    check_interval          1
    service_description     Total Processes Guest
    check_command           check_nrpe!check_procs -a 250 400 RSZDT
}
```

7- Restart nagios.

## Configure NAGIOS for automatic recovery of tomcat service

1- In the cloned machine edit sudoers config file `sudo visudo`

Append the following line
```bash
nagios ALL=(ALL) NOPASSWD: /usr/local/bin/restart_tomcat
```

2- Create the following scripts:

/usr/local/bin/restart_tomcat
```bash
service restart tomcat
```

/usr/local/bin/restart_tomcat_now
```bash
sudo /usr/local/bin/restart_tomcat
```

3- Try to restart tomcat using nagios user `sudo su nagios` and then `restart_tomcat_now`

4- Edit nrpe.cfg file and append the following line `command[restart_tomcat]=/usr/local/bin/restart_tomcat_now`

5- Make sure global nagios configuration property `enable_event_handlers=1` is set to 1 in `nagios.cfg`

6- Add the event_handler property to the target monitoring service 

```bash
event_handler_enabled           1
event_handler                   restart-tomcat
```

7- Add the event_handler command `restart-tomcat` to magios:

```bash
define command{
    command_name restart-tomcat
    command_line    /usr/local/nagios/libexec/restart-tomcat  $SERVICESTATE$ $SERVICESTATETYPE$ $SERVICEATTEMPT$ $HOSTADDRESS$
}
```

8- Create a restart-tomcat script in `/usr/local/nagios/libexec/` directory

/usr/local/nagios/libexec/restart-tomcat
```bash
#!/bin/sh
#
# Event handler script for restarting the web server on the local machine
#
# Note: This script will only restart the web server if the service is
#       retried 3 times (in a "soft" state) or if the web service somehow
#       manages to fall into a "hard" error state.
#

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
		case "$3" in

		# Wait until the check has been tried 3 times before restarting the web server.
		# If the check fails on the 4th time (after we restart the web server), the state
		# type will turn to "hard" and contacts will be notified of the problem.
		# Hopefully this will restart the web server successfully, so the 4th check will
		# result in a "soft" recovery.  If that happens no one gets notified because we
		# fixed the problem!
		2)
			echo -n "Restarting Tomcat service (3rd soft critical state)..."
			# Call the init script to restart the HTTPD server
			/usr/local/nagios/libexec/check_nrpe -H $4 -c restart_tomcat
			;;
			esac
		;;

	# The HTTP service somehow managed to turn into a hard error without getting fixed.
	# It should have been restarted by the code above, but for some reason it didn't.
	# Let's give it one last try, shall we?  
	# Note: Contacts have already been notified of a problem with the service at this
	# point (unless you disabled notifications for this service)
	HARD)
		echo -n "Restarting Tomcat service..."
		# Call the init script to restart the HTTPD server
		/usr/local/nagios/libexec/check_nrpe -H $4 -c restart_tomcat
		;;
	esac
	;;
esac
exit 0

```

9- Give read and execution permissions `sudo chmod 755 /usr/local/nagios/libexec/restart-tomcat`

10- Restart nagios and test the feature by stopping tomcat service in the cloned machine `sudo service tomcat stop` and wait 2 minutes until event handler sends a restart_tomcat command through NRPE and preventing the mail notification by successfully recovering from a SOFT state failure.

---


# Steps to reproduce (Zabbix)

## Installation and Execution Environments

1- Inside a virtual machine guest with 2 GB RAM and 10 GB disk Install Ubuntu Server 18.04 LTS

2- Clone the machine created at step 1 to later use as hosts of nagios.

3- Install nagios core 4 on machine created on step 1 and all it's dependencies (refer to nagios 4.0 documentation)

4- Install nagios pluggins 2.2.1 (refer to doc) and it's package requirements: `sudo apt install autoconf gcc libc6 libmcrypt-dev makelibssl-dev wget bc gawk dc build-essential snmp libnet-snmp-perlgettext`

## Zabbix Server installation

1- Install apache2

```bash
sudo apt update
sudo apt install apache2
```

2- Test if apache is operational at http://vhost-ip-address

3- Install MariaDB Database Server `sudo apt-get install mariadb-server mariadb-client`

4- Verify mariadb server is running `sudo systemctl status mariadb.service`

5- Secure MariaDB server by creating a root password and disallowing remote root access `sudo mysql_secure_installation`

6- Open MariaDB config file `sudo vim /etc/mysql/mariadb.conf.d/50-server.cnf` and append the lines below
```bash
innodb_file_format = Barracuda
innodb_large_prefix = 1
innodb_default_row_format = dynamic
```

7- Restart MariaDB `sudo systemctl restart mariadb.service`

8- Test if you can login to MariaDB with `sudo mysql -u root -p`

9- Install PHP 7.2 and Related Modules

Add the following third party repositories:
```bash
sudo apt-get install software-properties-common
sudo add-apt-repository ppa:ondrej/php
```

Update the repository list
`sudo apt update`

Install PHP7.2 packages and it's dependencies and related modules
`sudo apt install php7.2 libapache2-mod-php7.2 php7.2-common php7.2-mysql php7.2-gmp php7.2-curl php7.2-intl php7.2-mbstring php7.2-xmlrpc php7.2-mysql php7.2-gd php7.2-xml php7.2-cli php7.2-zip`

Change the PHP7.2 apache config file `sudo vim /etc/php/7.2/apache2/php.ini` and make the below changes:

```bash
file_uploads = On
allow_url_fopen = On
short_open_tag = On
memory_limit = 256M
upload_max_filesize = 100M
max_execution_time = 360
date.timezone = Europe/Lisbon
``` 

Restart apache2 service `sudo systemctl restart apache2.service`

Test php by creating a file `sudo vim /var/www/html/phpinfo.php` and add to this file content:

```php
<?php phpinfo( ); ?>
```

Navigate to http://vhost-ip-address/phpinfo.php

10- Create Zabbix database `sudo mysql -u root -p` then type in mariadb console:

```SQL
CREATE DATABASE zabbix character set utf8 collate utf8_bin;
CREATE USER 'zabbixuser'@'localhost' IDENTIFIED BY 'new_password_here';
GRANT ALL ON zabbix.* TO 'zabbixuser'@'localhost' IDENTIFIED BY 'user_password_here' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EXIT;
```

11- Install Zabbix 

Add Zabbix 4.0 repositories
```bash
cd /tmp
wget https://repo.zabbix.com/zabbix/4.0/ubuntu/pool/main/z/zabbix-release/zabbix-release_4.0-2+bionic_all.deb
wget https://repo.zabbix.com/zabbix/4.0/ubuntu/pool/main/z/zabbix-release/zabbix-release_4.0-2+xenial_all.deb
wget https://repo.zabbix.com/zabbix/4.0/ubuntu/pool/main/z/zabbix-release/zabbix-release_4.0-2+bionic_all.deb
sudo dpkg -i zabbix-release_4.0-2+bionic_all.deb
sudo dpkg -i zabbix-release_4.0-2+bionic*.deb
sudo dpkg -i zabbix-release_4.0-2+xenial*.deb
```

Install the other Zabbix dependencies:
`sudo apt install zabbix-server-mysql zabbix-agent zabbix-frontend-php php7.2-bcmath php7.2-ldap`


12- Configure Zabbix

Open Zabbix config file
`sudo vim /etc/zabbix/zabbix_server.conf`

Make the following changes:
```bash
DBName=zabbix
DBUser=zabbixuser
DBPassword=zabbixuser_password_here
```

Change the hostname of zabbix server
`sudo nano /etc/zabbix/zabbix_agentd.conf`
and change the line:
`Hostname=zabbix.example.com`

Import the initial schema and data for the server with MySQL:
`zcat /usr/share/doc/zabbix-server-mysql/create.sql.gz | mysql -u zabbixuser -p zabbix`

Restart and enable the Zabbix service:
```bash
sudo systemctl restart zabbix-server
sudo systemctl enable zabbix-server
sudo systemctl restart apache2.service
```

Navigate to http://vhost-ip-address/zabbix and follow the instructions inserting the previous MySQL credentials.

Set the Admin credentials with ``sudo mysql -u root -p`` followed by:
```SQL
USE zabbix;
UPDATE users SET passwd=md5('your_password_here') WHERE alias='Admin';
```

Finally login using the credentials: Admin/your_password_here.

### Zabbix Agent installation

1- Install Zabbix agent

Instal zabbix agent
```bash
sudo apt-get update
sudo apt-get install zabbix-agent
```

2- Configure Zabbix agent `sudo vim /etc/zabbix/zabbix_agentd.conf` and change the lines:

```bash
Server=192.168.99.103
ServerActive=http://192.168.99.103/zabbix/
Hostname=zabbix-serv1
```

3- Enable and restart Zabbix agent 

```bash
sudo systemctl enable zabbix-agent
sudo systemctl restart zabbix-agent
```

---

### Add a Zabbix host

1- Login to zabbix web interface at http://zabbix-serv-ip/zabbix/

2- Go to Configuration > Hosts > Create Host

3- Fill the form with host name, your vclone ip address in Agent Interfaces and add it to Linux Servers group

4- Go to templates tab, click select Template OS Linux, click select and underlined Add button then the square blue Add button.

### Monitor Graphs 

1- Go to Monitoring > Graphs

2- In the Graph dropdown select "CPU load", in the Host dropdown select "vclone1" or the name that you choose for your monitored host.

3- Select the desired interval and the graph will instantly adapt to your preferences

# References:

* https://pplware.sapo.pt/tutoriais/networking/tutorial-como-enviar-alertas-do-nagios-via-gmail/
* https://medium.com/@exesse/how-to-build-ubuntu-server-in-virtualbox-on-host-only-network-adapter-with-internet-access-from-81cd7253e3b1
* https://askubuntu.com/questions/984445/netplan-configuration-on-ubuntu-17-04-virtual-machine
* https://www.comparitech.com/net-admin/nagios-vs-zabbix/#Dashboard_and_User_Interface
* https://www.nagios.org/documentation/
* https://www.zabbix.com/documentation/4.0/manual/introduction/overviewbb
* https://websiteforstudents.com/how-to-install-zabbix-4-0-monitoring-system-with-apache2-mariadb-and-php-7-2-on-ubuntu-16-04-18-04-18-10/
* https://tecadmin.net/install-zabbix-agent-on-ubuntu-and-debian/
* https://tecadmin.net/add-host-zabbix-server-monitor/