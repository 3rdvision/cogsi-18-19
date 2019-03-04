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

# Problem Analysis


---
# Solution design


---
# Steps to reproduce

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

## Configure nagios to monitor the cloned machine Tomcat

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