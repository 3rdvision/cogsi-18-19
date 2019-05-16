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

![](https://i.imgur.com/wbA04GK.jpg)

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

# Analysis of the alternative (Puppet)

## The Puppet aproach:

Puppet gives you an automatic way to inspect, deliver, operate and future-proof all of your infrastructure and deliver all of your applications faster. Puppet helps you:

Start automating easy with no prerequisites or Puppet knowledge using an agentless, task based approach.
Discover what you have in your cloud, containers and on premises and what software runs on them.
Quickly move from one-off management of the state of resources to ongoing state management to enforce consistency and changes across the data center and cloud platforms.
Get the visualization and reporting you need to make key decisions or support compliance initiatives.
Deploy applications faster with greater control and visibility for the entire development team.

The result: A new standard for automating infrastructure and software management and delivery all in one platform, at scale.

Puppet is supported int Windows, Linux and macOS.

It's dependencies are as of version 6.4 (the latest as of the date of the writing of this document):

* Ruby 2.5.x
* CFPropertyList 2.2 or later
* Facter 2.0 or later
* The msgpack gem, if you're using msgpack serialization

For the hardware requirements it's recomended to have at least a multi-core processor (2-4 cores) and at least 4 GB of RAM.



# Solution Design

Ansible and Vagrant were used as software solutions to complete the requirements.

To setup TODD hosts and the Tomcat host VMs vagrant was used to create these boxes and then the provisioning of these was pointed to be done by ansible and the respective playbook files for the provisioning of the TODD hosts and the Tomcat.

An ansible playbook was also created for nagios which will be used when runing `ansible-playbook` after issuing the `vagrant up` of the other machines and it's respective provisioning using vagrant+ansible playbooks.

Both ansible and vagrant were run in the bare-metal host machine running Linux 4.19.34-1-MANJARO.

In the end there will be 4 machines running:
* 2 x Todd
* 1 x Nagios
* 1 x Tomcat

The virtualbox looks like the image below:

![](https://i.imgur.com/Y3jgQzB.png)

In the end, after the steps to reproduce are compelted:

![](https://i.imgur.com/TOlJu45.png)

## About Vagrant

Vagrant is a tool for building and managing virtual machine environments in a single workflow. With an easy-to-use workflow and focus on automation, Vagrant lowers development environment setup time, increases production parity, and makes the "works on my machine" excuse a relic of the past.

If you are already familiar with the basics of Vagrant, the documentation provides a better reference build for all available features and internals.

### Why Vagrant?

Vagrant provides easy to configure, reproducible, and portable work environments built on top of industry-standard technology and controlled by a single consistent workflow to help maximize the productivity and flexibility of you and your team.

To achieve its magic, Vagrant stands on the shoulders of giants. Machines are provisioned on top of VirtualBox, VMware, AWS, or any other provider. Then, industry-standard provisioning tools such as shell scripts, Chef, or Puppet, can automatically install and configure software on the virtual machine.

## About Ansible

Ansible is an IT automation tool. It can configure systems, deploy software, and orchestrate more advanced IT tasks such as continuous deployments or zero downtime rolling updates.

Ansible’s main goals are simplicity and ease-of-use. It also has a strong focus on security and reliability, featuring a minimum of moving parts, usage of OpenSSH for transport (with other transports and pull modes as alternatives), and a language that is designed around auditability by humans–even those not familiar with the program.

We believe simplicity is relevant to all sizes of environments, so we design for busy users of all types: developers, sysadmins, release engineers, IT managers, and everyone in between. Ansible is appropriate for managing all environments, from small setups with a handful of instances to enterprise environments with many thousands of instances.

Ansible manages machines in an agent-less manner. There is never a question of how to upgrade remote daemons or the problem of not being able to manage systems because daemons are uninstalled. Because OpenSSH is one of the most peer-reviewed open source components, security exposure is greatly reduced. Ansible is decentralized–it relies on your existing OS credentials to control access to remote machines. If needed, Ansible can easily connect with Kerberos, LDAP, and other centralized authentication management systems.

This documentation covers the version of Ansible noted in the upper left corner of this page. We maintain multiple versions of Ansible and of the documentation, so please be sure you are using the version of the documentation that covers the version of Ansible you’re using. For recent features, we note the version of Ansible where the feature was added.

Ansible releases a new major release of Ansible approximately three to four times per year. The core application evolves somewhat conservatively, valuing simplicity in language design and setup. However, the community around new modules and plugins being developed and contributed moves very quickly, adding many new modules in each release.

All the configuration details and explanations are better detailed in the next section below (steps to reproduce).

---

# Steps to reproduce

## Installation of tooling

For this solution, vagrant and ansible should be installed in the host host machine, which in this case it's Manjaro.

Manjaro is a distro based on Arch Linux and uses pacman as the PACkage MANager.

Using pacman we can install vagrant and ansible with a beautiful oneliner.
`sudo pacman -Syu vagrant ansible`

## Configure vagrant and ansible for the hosts machine

1- Make a new folder and change directory to it `mkdir playbook && cd playbook`

2- Create a file named Vagrantfile which will be used to configure our VMs and insert the following content
```bash
# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.
  
  # Inline script to update to german fast servers
  config.vm.provision :shell, :inline => "sed -i 's/us.archive/de.archive/g' /etc/apt/sources.list"

  # Ubuntu 18.04 LTS for all boxes
  config.vm.box = "bento/ubuntu-18.04"

  # The tomcat1 box using ansible provision and the ip 192.168.99.102
  config.vm.define "tomcat1" do |tomcat1|
    tomcat1.vm.box = "bento/ubuntu-18.04"
    tomcat1.vm.network "private_network", ip: "192.168.99.102"
    tomcat1.vm.provision :ansible do |ansible|
      ansible.playbook = "playbook_tomcats.yml"
      ansible.inventory_path = "hosts"
    end
  end

  # The tomcat1 box using ansible provision and the ip 192.168.99.103
  config.vm.define "todd1" do |todd1|
    todd1.vm.box = "bento/ubuntu-18.04"
    todd1.vm.network "private_network", ip: "192.168.99.103"
  end

  # The tomcat1 box using ansible provision and the ip 192.168.99.104
  config.vm.define "todd2" do |todd2|
    todd2.vm.box = "bento/ubuntu-18.04"
    todd2.vm.network "private_network", ip: "192.168.99.104"
  end  

  # Ansible Provision for the Todds (common for todd1 and todd2 boxes)
  config.vm.provision :ansible do |ansible|
    ansible.playbook = "playbook_todds.yml"
    ansible.inventory_path = "hosts"
  end

  # A provision script in vagrantfile which can be used to run scripts in our machines
  #config.vm.provision :shell, path: "bootstrap.sh"

end
```

If we read carefully through the comments of our Vagrantfile configurations we can get the idea of what each block of code is meant to do.

Some itneresting facts about Vagrant is:

* Vagrant is based on ruby which is a very elegant and clean programming language.
* The Vagrantfile is configured using ruby which means power to the user!

This Vagrantfile does this:
* Creates 3 boxes based on Ubuntu 18.04 LTS
* Provisions all those boxes using Vagrant to update the apt list servers to german ones.
* Provisions all those boxes with the respective Ansible playbook files. For example, for the todd1 and todd2 boxes, it gets provisioned in ansible using the `playbook_todds.yml` file which contains the provisioning done by ansible for the machines in the Todds group. This file will be configured next.

3- Create a file named playbook_todds.yml and insert the following content
```bash
---
- hosts: todds

  become: yes
  become_method: sudo
  remote_user: vagrant

  tasks:
  - name: Update apt packages
    apt:
      update_cache: yes

  - name: Install todd packages
    apt: 
      name: ['openjdk-8-jdk','gradle','wget','git','ssh','nagios-nrpe-server','vim','nsca','sudo']
      state: present

  - name: create /home/dan directory
    file:
      path: /home/dan
      state: directory
      mode: 0755

  - name: git clone 
    git:
      repo: 'https://dvazevedo@bitbucket.org/dvazevedo/todd-cogsi.git'
      dest: /home/dan/todd-cogsi
      update: no

  - name: Copy todd buid.gradle config
    copy:
      src: config_files/todd/1/build.gradle
      dest: /home/dan/todd-cogsi/build.gradle
      mode: 744


  - name: Build todd
    shell: 'cd /home/dan/todd-cogsi && sudo gradle build'

  - name: Copy NRPE config
    copy:
       src: config_files/nrpe/nrpe.cfg
       dest: /etc/nagios/nrpe.cfg
       mode: 0755

  - name: Copy bash script
    copy:
      src: config_files/todd/start_script/start_todd
      dest: /usr/local/bin/start_todd
      mode: 0755

  - name: Add nagios user to sudoers
    become: yes
    become_method: sudo
    shell: adduser nagios sudo      

  - name: Make todd autostart crontab
    become: yes
    become_method: sudo
    copy:
      src: config_files/cron/vagrant
      dest: /var/spool/cron/crontabs/vagrant
      mode: 0600

  - name: Enable & start nagios-nrpe
    service:
      name: nagios-nrpe-server
      enabled: yes
      state: restarted

  - name: Enable & start ssh
    service:
      name: ssh
      enabled: yes
      state: started      

  - name: Start todd (takes about ~15sec)
    shell: 'bash start_todd'
    async: 2592000               # 60*60*24*30 – 1 month    
    poll: 0
```

This ansible file will configure all Todds hosts (tood1 and todd2 boxes).

If we look upon the names of each task, we get the idea of the effect they will create on the box.

This ansible playbook will play the following actions in this order:
* Update apt packages
* Ensure all the necessary packages are installed
* Creates a new directory for todd and git clones the application pulic repository
* Copies todd customm build.gradle config which contains configuration to extract the IP Address of the machine and uses it to advertise the JMX application
* Builds the application
* Copies NRPE configuration file
* Copies a bash script to start the Todd application (which will be executed at the startup of the machine)
* Adds a nagios user to the sudoers
* Makes todd autostartable using crontab
* Enables nagios-nrpe service
* Enables ssh service
* Starts todd and makes sure it stays open, even after the vagrant script and ssh session is closed

After the run of this playbook, the machine is packed with NRPE enabled nagios and TODD running application with JMX enabled and notifications to the nagios host at the IP 192.168.99.101.

4- Create a file named playbook_tomcats.yml and insert the following content
```bash
---
- hosts: tomcats

  become: yes
  become_method: sudo
  remote_user: vagrant

  tasks:

  - name: Install Tomcat NRPE and NSCA
    apt:
      name: ['tomcat8','nagios-nrpe-server','nsca']
      state: present
      update_cache: yes

  - name: Copy JMX setnenv for tomcat
    copy:
       src: config_files/tomcat-env/setenv.sh
       dest: /usr/share/tomcat8/bin/setenv.sh
       mode: 0775

  - name: Sets env
    shell: bash /usr/share/tomcat8/bin/setenv.sh  

  - name: Add nagios user to sudoers
    become: yes
    become_method: sudo
    shell: adduser nagios sudo

  - name: Copy NRPE config
    copy:
       src: config_files/nrpe/nrpe.cfg
       dest: /etc/nagios/nrpe.cfg
       mode: 0755  

  - name: Start and enable Tomcat service
    systemd:
      name: tomcat8
      state: restarted
      enabled: true

  - name: Enable & restart nagios-nrpe-server
    service:
      name: nagios-nrpe-server
      enabled: yes
      state: restarted

  - name: Enable & start ssh
    service:
      name: ssh
      enabled: yes
      state: started
```

This ansible file will configure the Tomcat host  which is just one (tomcat1).

If we look upon the names of each task, we get the idea of the effect they will create on the box.

This ansible playbook will play the following actions in this order:
* Update apt packages
* Ensure all the necessary packages are installed
* Copies JMX setenv for tomcat which will be the script used to set the JMX properties to enable it in tomcat8
* Runs that setnev.sh script
* Adds a nagios user to the sudoers
* Copies the NRPE configuration file
* Enables tomcat8 service
* Enables nagios-nrpe service
* Enables ssh service

After ansible is done playing this playbook, we can visit the address 192.168.99.102:8080 to check Tomcat is running on that machine and even try to use jconsole to check the application status.

5- Create a file named playbook_tomcats.yml and insert the following content

```bash
---
- hosts: nagios

  become: yes
  become_method: sudo
  remote_user: vagrant

  tasks:
  - name: Update apt packages
    apt:
      update_cache: yes
    become: true

  - nagios:
      action: servicegroup_service_downtime
      minutes: 30
      servicegroup: CogsiHostGroup2
      host: '{{ inventory_hostname }}'

  - name: Copy Nagios config
    copy:
       src: config_files/nagios/nagios-etc/objects/vclones.cfg
       dest: /usr/local/nagios/etc/objects/vclones.cfg
       mode: 0755

  - name: Restart nagios using new config
    service:
      name: nagios
      enabled: yes
      state: restarted       

  # - name: Install nagios and dependencies
  #   apt: 
  #     name: ['apache2','php','php-cgi','libapache2-mod-php','php-common','php-pear','php-mbstring','nagios3','nagios-plugins','nagios-nrpe-plugin','python-pexpect','python-setuptools','python-pip','openjdk-8-jdk','gradle','wget','git','ssh','nagios-nrpe-server','vim','nsca','sudo']
  #     state: present
 
  # - name: install pexpect
  #   pip:
  #     name: pexpect
  #   become: yes

  # - name: Define nagios password
  #   expect:
  #     command: /bin/bash -c "sudo htpasswd -c /etc/nagios3/htpasswd.users nagiosadmin"
  #     responses:
  #       (?i)password: "nagios"      

  # - name: Copy NSCA config
  #   copy:
  #      src: config_files/nagios/nsca/nsca.cfg
  #      dest: /etc/nsca.cfg
  #      mode: 0755

```

This ansible file will configure the Nagios host which was created in the previous interation 1.2.

If we look upon the names of each task, we get the idea of the effect they will create on the box.

This ansible playbook will play the following actions in this order:
* Update apt packages
* Copies nagios configuration file vhosts.cfg to replace the old one
* Restarts nagios to use the new configurations
* Issues a service dowtime of 30 minutes to the group with the name "CogsiHostGroup2" which is the group for todd1 and todd2

After ansible is done playing this playbook, we can visit the address 192.168.99.102:8080 to check Tomcat is running on that machine and even try to use jconsole to check the application status.


6- Create a file named hosts and insert the following content
```bash
---
nagios ansible_ssh_host=192.168.99.101 ansible_ssh_port=22 ansible_ssh_user=dan ansible_ssh_pass=***
[tomcats]
tomcat1 ansible_ssh_host=127.0.0.1 ansible_ssh_port=2222 ansible_ssh_private_key_file=".vagrant/machines/tomcat1/virtualbox/private_key"
[todds]
todd1 ansible_ssh_host=127.0.0.1 ansible_ssh_port=2200 ansible_ssh_private_key_file=".vagrant/machines/todd1/virtualbox/private_key"
todd2 ansible_ssh_host=127.0.0.1 ansible_ssh_port=2201 ansible_ssh_private_key_file=".vagrant/machines/todd2/virtualbox/private_key"
```

This file will have the configuration variables for our hosts in order for ansible to log into those using SSH.

We will be using the provided vagrant ssh private key file to log into the vagrant created boxes.

As for nagios, we can log into it using the username and password we normally use when we SSH into it or log into it through the Virtualbox. Those properties will be `ansible_ssh_user` for the username and `ansible_ssh_pass` for the password.

7- Create a file named ansible.cfg and insert the following content
```bash
[defaults]
inventory = hosts
remote_user = vagrant
private_key_file = /home/qwe/playbooks/.vagrant/machines/default/virtualbox/private_key

```

This file will contain the ansible default configurations that can be omited when using a playbook

8- Use or import from another machine/OS the nagios virtual machine used in previous assignments 1.2

9- Execute the command `vagrant up` in that directory and vagrant will create the virtual machines and provision them using ansible

![](https://i.imgur.com/X4FvDLx.png)

Here's a sample of an expected output from running that command:

```bash
qwe@qwe-pc ~/D/playbooks> vagrant up
Bringing machine 'tomcat1' up with 'virtualbox' provider...
Bringing machine 'todd1' up with 'virtualbox' provider...
Bringing machine 'todd2' up with 'virtualbox' provider...
==> tomcat1: Importing base box 'bento/ubuntu-18.04'...
==> tomcat1: Matching MAC address for NAT networking...
==> tomcat1: Checking if box 'bento/ubuntu-18.04' version '201812.27.0' is up to date...
==> tomcat1: Setting the name of the VM: playbooks_tomcat1_1558040086019_18606
==> tomcat1: Vagrant has detected a configuration issue which exposes a
==> tomcat1: vulnerability with the installed version of VirtualBox. The
==> tomcat1: current guest is configured to use an E1000 NIC type for a
==> tomcat1: network adapter which is vulnerable in this version of VirtualBox.
==> tomcat1: Ensure the guest is trusted to use this configuration or update
==> tomcat1: the NIC type using one of the methods below:
==> tomcat1: 
==> tomcat1:   https://www.vagrantup.com/docs/virtualbox/configuration.html#default-nic-type
==> tomcat1:   https://www.vagrantup.com/docs/virtualbox/networking.html#virtualbox-nic-type
==> tomcat1: Clearing any previously set network interfaces...
==> tomcat1: Preparing network interfaces based on configuration...
    tomcat1: Adapter 1: nat
    tomcat1: Adapter 2: hostonly
==> tomcat1: Forwarding ports...
    tomcat1: 22 (guest) => 2222 (host) (adapter 1)
==> tomcat1: Booting VM...
==> tomcat1: Waiting for machine to boot. This may take a few minutes...
    tomcat1: SSH address: 127.0.0.1:2222
    tomcat1: SSH username: vagrant
    tomcat1: SSH auth method: private key
    tomcat1: 
    tomcat1: Vagrant insecure key detected. Vagrant will automatically replace
    tomcat1: this with a newly generated keypair for better security.
    tomcat1: 
    tomcat1: Inserting generated public key within guest...
    tomcat1: Removing insecure key from the guest if it's present...
    tomcat1: Key inserted! Disconnecting and reconnecting using new SSH key...
==> tomcat1: Machine booted and ready!
==> tomcat1: Checking for guest additions in VM...
==> tomcat1: Configuring and enabling network interfaces...
==> tomcat1: Mounting shared folders...
    tomcat1: /vagrant => /home/qwe/Documents/playbooks
==> tomcat1: Running provisioner: shell...
    tomcat1: Running: inline script
==> tomcat1: Running provisioner: ansible...
Vagrant has automatically selected the compatibility mode '2.0'
according to the Ansible version installed (2.7.10).                                                                                                                   

Alternatively, the compatibility mode can be specified in your Vagrantfile:                                                                                            
https://www.vagrantup.com/docs/provisioning/ansible_common.html#compatibility_mode                                                                                     

    tomcat1: Running ansible-playbook...

PLAY [todds] *******************************************************************
skipping: no hosts matched

PLAY RECAP *********************************************************************

==> tomcat1: Running provisioner: ansible...
Vagrant has automatically selected the compatibility mode '2.0'
according to the Ansible version installed (2.7.10).                                                                                                                   

Alternatively, the compatibility mode can be specified in your Vagrantfile:                                                                                            
https://www.vagrantup.com/docs/provisioning/ansible_common.html#compatibility_mode                                                                                     

    tomcat1: Running ansible-playbook...

PLAY [tomcats] *****************************************************************

TASK [Gathering Facts] *********************************************************
ok: [tomcat1]

TASK [Install Tomcat NRPE and NSCA] ********************************************
changed: [tomcat1]

TASK [Copy JMX setnenv for tomcat] *********************************************
changed: [tomcat1]

TASK [Sets env] ****************************************************************
changed: [tomcat1]

TASK [Add nagios user to sudoers] **********************************************
changed: [tomcat1]

TASK [Copy NRPE config] ********************************************************
changed: [tomcat1]

TASK [Start and enable Tomcat service] *****************************************
changed: [tomcat1]

TASK [Enable & restart nagios-nrpe-server] *************************************
changed: [tomcat1]

TASK [Enable & start ssh] ******************************************************
ok: [tomcat1]

PLAY RECAP *********************************************************************
tomcat1                    : ok=9    changed=7    unreachable=0    failed=0   

==> todd1: Importing base box 'bento/ubuntu-18.04'...
==> todd1: Matching MAC address for NAT networking...
==> todd1: Checking if box 'bento/ubuntu-18.04' version '201812.27.0' is up to date...
==> todd1: Setting the name of the VM: playbooks_todd1_1558040328111_92265
==> todd1: Fixed port collision for 22 => 2222. Now on port 2200.
==> todd1: Vagrant has detected a configuration issue which exposes a
==> todd1: vulnerability with the installed version of VirtualBox. The
==> todd1: current guest is configured to use an E1000 NIC type for a
==> todd1: network adapter which is vulnerable in this version of VirtualBox.
==> todd1: Ensure the guest is trusted to use this configuration or update
==> todd1: the NIC type using one of the methods below:
==> todd1: 
==> todd1:   https://www.vagrantup.com/docs/virtualbox/configuration.html#default-nic-type
==> todd1:   https://www.vagrantup.com/docs/virtualbox/networking.html#virtualbox-nic-type
==> todd1: Clearing any previously set network interfaces...
==> todd1: Preparing network interfaces based on configuration...
    todd1: Adapter 1: nat
    todd1: Adapter 2: hostonly
==> todd1: Forwarding ports...
    todd1: 22 (guest) => 2200 (host) (adapter 1)
==> todd1: Booting VM...
==> todd1: Waiting for machine to boot. This may take a few minutes...
    todd1: SSH address: 127.0.0.1:2200
    todd1: SSH username: vagrant
    todd1: SSH auth method: private key
    todd1: 
    todd1: Vagrant insecure key detected. Vagrant will automatically replace
    todd1: this with a newly generated keypair for better security.
    todd1: 
    todd1: Inserting generated public key within guest...
    todd1: Removing insecure key from the guest if it's present...
    todd1: Key inserted! Disconnecting and reconnecting using new SSH key...
==> todd1: Machine booted and ready!
==> todd1: Checking for guest additions in VM...
==> todd1: Configuring and enabling network interfaces...
==> todd1: Mounting shared folders...
    todd1: /vagrant => /home/qwe/Documents/playbooks
==> todd1: Running provisioner: shell...
    todd1: Running: inline script
==> todd1: Running provisioner: ansible...
Vagrant has automatically selected the compatibility mode '2.0'
according to the Ansible version installed (2.7.10).                                                                                                                   

Alternatively, the compatibility mode can be specified in your Vagrantfile:                                                                                            
https://www.vagrantup.com/docs/provisioning/ansible_common.html#compatibility_mode                                                                                     

    todd1: Running ansible-playbook...

PLAY [todds] *******************************************************************

TASK [Gathering Facts] *********************************************************
ok: [todd1]

TASK [Update apt packages] *****************************************************
changed: [todd1]

TASK [Install todd packages] ***************************************************
changed: [todd1]

TASK [create /home/dan directory] **********************************************
changed: [todd1]

TASK [git clone] ***************************************************************
changed: [todd1]

TASK [Copy todd buid.gradle config] ********************************************
changed: [todd1]

TASK [Build todd] **************************************************************
changed: [todd1]

TASK [Copy NRPE config] ********************************************************
changed: [todd1]

TASK [Copy bash script] ********************************************************
changed: [todd1]

TASK [Add nagios user to sudoers] **********************************************
changed: [todd1]

TASK [Make todd autostart crontab] *********************************************
changed: [todd1]

TASK [Enable & start nagios-nrpe] **********************************************
changed: [todd1]

TASK [Enable & start ssh] ******************************************************
ok: [todd1]

TASK [Start todd (takes about ~15sec)] *****************************************
changed: [todd1]

PLAY RECAP *********************************************************************
todd1                      : ok=14   changed=12   unreachable=0    failed=0   

==> todd2: Importing base box 'bento/ubuntu-18.04'...
==> todd2: Matching MAC address for NAT networking...
==> todd2: Checking if box 'bento/ubuntu-18.04' version '201812.27.0' is up to date...
==> todd2: Setting the name of the VM: playbooks_todd2_1558040812597_44175
==> todd2: Fixed port collision for 22 => 2222. Now on port 2201.
==> todd2: Vagrant has detected a configuration issue which exposes a
==> todd2: vulnerability with the installed version of VirtualBox. The
==> todd2: current guest is configured to use an E1000 NIC type for a
==> todd2: network adapter which is vulnerable in this version of VirtualBox.
==> todd2: Ensure the guest is trusted to use this configuration or update
==> todd2: the NIC type using one of the methods below:
==> todd2: 
==> todd2:   https://www.vagrantup.com/docs/virtualbox/configuration.html#default-nic-type
==> todd2:   https://www.vagrantup.com/docs/virtualbox/networking.html#virtualbox-nic-type
==> todd2: Clearing any previously set network interfaces...
==> todd2: Preparing network interfaces based on configuration...
    todd2: Adapter 1: nat
    todd2: Adapter 2: hostonly
==> todd2: Forwarding ports...
    todd2: 22 (guest) => 2201 (host) (adapter 1)
==> todd2: Booting VM...
==> todd2: Waiting for machine to boot. This may take a few minutes...
    todd2: SSH address: 127.0.0.1:2201
    todd2: SSH username: vagrant
    todd2: SSH auth method: private key
    todd2: Warning: Connection reset. Retrying...
    todd2: Warning: Remote connection disconnect. Retrying...
    todd2: 
    todd2: Vagrant insecure key detected. Vagrant will automatically replace
    todd2: this with a newly generated keypair for better security.
    todd2: 
    todd2: Inserting generated public key within guest...
    todd2: Removing insecure key from the guest if it's present...
    todd2: Key inserted! Disconnecting and reconnecting using new SSH key...
==> todd2: Machine booted and ready!
==> todd2: Checking for guest additions in VM...
==> todd2: Configuring and enabling network interfaces...
==> todd2: Mounting shared folders...
    todd2: /vagrant => /home/qwe/Documents/playbooks
==> todd2: Running provisioner: shell...
    todd2: Running: inline script
==> todd2: Running provisioner: ansible...
Vagrant has automatically selected the compatibility mode '2.0'
according to the Ansible version installed (2.7.10).                                                                                                                   

Alternatively, the compatibility mode can be specified in your Vagrantfile:                                                                                            
https://www.vagrantup.com/docs/provisioning/ansible_common.html#compatibility_mode                                                                                     

    todd2: Running ansible-playbook...

PLAY [todds] *******************************************************************

TASK [Gathering Facts] *********************************************************
ok: [todd2]

TASK [Update apt packages] *****************************************************
changed: [todd2]

TASK [Install todd packages] ***************************************************
changed: [todd2]

TASK [create /home/dan directory] **********************************************
changed: [todd2]

TASK [git clone] ***************************************************************
changed: [todd2]

TASK [Copy todd buid.gradle config] ********************************************
changed: [todd2]

TASK [Build todd] **************************************************************
changed: [todd2]

TASK [Copy NRPE config] ********************************************************
changed: [todd2]

TASK [Copy bash script] ********************************************************
changed: [todd2]

TASK [Add nagios user to sudoers] **********************************************
changed: [todd2]

TASK [Make todd autostart crontab] *********************************************
changed: [todd2]

TASK [Enable & start nagios-nrpe] **********************************************
changed: [todd2]

TASK [Enable & start ssh] ******************************************************
ok: [todd2]

TASK [Start todd (takes about ~15sec)] *****************************************
changed: [todd2]

PLAY RECAP *********************************************************************
todd2                      : ok=14   changed=12   unreachable=0    failed=0   

```

First make sure you have a host only network configured as the image shown below:

![](https://i.imgur.com/Nsobcdc.png)

Go to File>Import Apliance

![](https://i.imgur.com/ZMHNEP4.png)

Then Click "Expert Mode", select the virtual appliance file exported from your previous machine/OS and click "Import"

![](https://i.imgur.com/yl0nvDl.png)

After it is imported. The machine will apear in virtualbox as if you created it.

![](https://i.imgur.com/b0PwQHA.png)

In the end, visit in your browser http://192.168.99.101/nagios

Input your username and password for nagios and everything should be good as shown in the image below

![](https://i.imgur.com/TOlJu45.png)

In order to execute our nagios playbook we should use ansible directly with `ansible-playbook playbook_nagios.yml` and wait for the provisioning to end.

In order to shutdown our host machines create by vagrant we should navigate to the Vagrantfile directory and type `vagrant halt`.

If we want to destroy our Vagrant machines (which can be recreated again with a simple `vagrant up`) we should type `vagrant destroy` and respond with "y" to all subsequent promts.


# References


* https://www.vagrantup.com/intro/index.html
* https://docs.ansible.com/ansible/latest/index.html
* https://puppet.com/products/how-puppet-works