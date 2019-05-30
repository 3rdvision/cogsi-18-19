STUDENT **Daniel Azevedo** (1180109) - 2.1
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

![](https://i.imgur.com/Ag7FyZ8.jpg)

---

# Problem Analysis

## Objectives

* To simulate an integrated "devOps" scenario

## Requirements

### Continous Integration

* Use Jenkins to implement a pipeline to build the TODD application

### Continous Delivery

* Successful builds of the application should result in the application being archived in the Nexus artifact repository
* Deploy the application to, at least, two different hosts in the local network (using Ansible)
* Include a stage in the pipeline that, upon user confirmation, executes the Ansible deployment of the application

### Network

* All the hosts should be connected
* The network should include two hosts: one with TODD and another with TODD and Nagios

### Virtualization

* Use docker or vagrant to simulate the hosts

## Configuration management

* Use Ansible to deploy the TODD application
* Ansible makes sure all the necessary packages and requirements are there
* Ansible applies the necessary configuration to nagios so that it will event handle the 

---

# Analysis of the alternative (different aproaches to the pipeline)

In this component the analysis of the alternative is centered specially in different aproaches in the pipeline.

The different aproaches will be presented and described below

Different aproaches for the pipeline design:
1- The build of the pipeline being initiated by a new commit event;
2- The Jenkinsfile would be in the repository instead of the pasted version;
3- Use groovy functions to improve the readability of our pipeline;
4- Use gradle Jenkins add-on instead of directly using the command;
5- Using different aproaches for unix-like systems or windows;
6- Initiating a docker or vagrant box only to compile the code and publish the artifacts;


 1- A new commit would trigger a Webhooks (possible to configure in popular repository management websites like Github, Bitbucket, etc.) and

 2- In order to archive this we would configure our pipeline "Defininition" from "Pipeline script from SCM", choose GIT, the credentials and the script path from the repository to the Jenkinsfile.

 3- We could define a groovy function for, for instance building todd and instead of directly having all the commands in the build todd we would just have buildTodd() and the definition of all functions would be at the end of the Jenkinsfile

 ```r
def buildTodd() {
    echo 'Going for CMS project directory and building...'    
    sh '''cd $WORKSPACE
    gradle build'''
}
 ``` 

 4- We could use the Jenkins gradle and artifactory to build and publish the gradle artifacts like the example below:

 ```

node {
    // Get Artifactory server instance, defined in the Artifactory Plugin administration page.
    def server = Artifactory.server "SERVER_ID"
    // Create an Artifactory Gradle instance.
    def rtGradle = Artifactory.newGradleBuild()
    def buildInfo

    stage('Clone sources') {
        git url: 'https://github.com/jfrogdev/project-examples.git'
    }

    stage('Artifactory configuration') {
        // Tool name from Jenkins configuration
        rtGradle.tool = "Gradle-2.4"
        // Set Artifactory repositories for dependencies resolution and artifacts deployment.
        rtGradle.deployer repo:'ext-release-local', server: server
        rtGradle.resolver repo:'remote-repos', server: server
    }

    stage('Gradle build') {
        buildInfo = rtGradle.run rootDir: "gradle-examples/4/gradle-example-ci-server/", buildFile: 'build.gradle', tasks: 'clean artifactoryPublish'
    }

    stage('Publish build info') {
        server.publishBuildInfo buildInfo
    }
}
 ```

 5- By using the isUnix which returns true if enclosing node is running on a Unix-like system (such as Linux or Mac OS X), false if Windows. We could execute different functions in case of it being a unix system or windows. This would allows our pipeline to be run in a more independent-os fashion. An example is below for building TODD:

 ```jenkinsfile
node {
	stage('Build') {
      	if (isUnix()) {
         	sh "cd $WORKSPACE
    gradle build"
      	} else {
         	bat '''cd %WORKSPACE%
            gradle build
             '''
    }
}
 ```

6- We could docker run a Ubuntu 18.04 LTS image with gradle on docker and mount a folder to do a gradle build of the src then after the job was done, to publish the JAR to our nexus repository. The benefit on doing this would be to offload the Jenkins server from the cpu-intensive build of TODD and also provide a better security through containerism and separation of responsibility. This mean our Jenkins server would run smoother and would be safe from a malicious remote code execution if that somehow got into TODD source code.

# Solution Design

The objective of this assignment was to simulate a real devOps scenario and in this academia example we are going to simulate that we have 2 machines we want to be deploying and one of them has nagios and is monitoring the other through JMX.

To simulate a real devOps scenario we will be using Jenkings for the continuous integration and continuous delivery which will be done using ansible. In this scenario, after a succesful build a new code of the application, this will be automatically deployed to our hosts and restart the services of them.

In this academia example is just a virtualbox but in the real world would be a machine or VM in the company's server. These will be controlled using ansible which was explored in the previous assignemnt and allows for the configuration of any machine and doesn't require an agent as it can work only through SSH.

![](https://image.slidesharecdn.com/opscon-ansible-170423173302/95/ansible-new-paradigms-for-orchestration-19-638.jpg?cb=1492968801)

As is show in the image above, our control machine will have the necessary inventory and playbooks to connect through SSH to the hosts which in our case is 2 and not 3 like the image shows.

The control machine will also have Jenkins and Nexus although, again, in a real world scenario, Jenkins and Nexus would probably be their own separate server.

```bash
---
nagios ansible_ssh_host=192.168.99.101 ansible_ssh_port=22 ansible_ssh_user=dan ansible_ssh_pass=********
[todds]
todd1 ansible_ssh_host=127.0.0.1 ansible_ssh_port=2222 ansible_ssh_private_key_file=".vagrant/machines/todd1/virtualbox/private_key"
```

Our inventory file above (hosts file) shows our 2 hosts, their respective IP address and SSH accessible way. In case of todd1 which is created using a Vagrant file, it will be accessed through an automatically created SSH private key. On the other hand, nagios, our already existing VM, will be accessed through simple SSH username and SSH password.

Our Vagrantfile is presented below:

```ruby
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

  # The tomcat1 box using ansible provision and the ip 192.168.99.103
  config.vm.define "todd1" do |todd1|
    todd1.vm.box = "bento/ubuntu-18.04"
    todd1.vm.network "private_network", ip: "192.168.99.103"
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

As we can easily read, we have two hosts, both with "bento/ubuntu-18.04" box image. The "todd1" host has a provision associated with ansible pointed to the playbook playbook_todds.yml which contains the configuration from a standart ubuntu 18.04 server installation to a todd host configured to be monitored through NRPE, NSCA and JMX.

```yaml
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

  - name: Copy bash start todd script
    copy:
      src: config_files/todd/start_script/start_todd
      dest: /usr/local/bin/start_todd
      mode: 0755

  - name: Copy restart todd bash script
    copy:
      src: config_files/todd/restart_script/restart_todd
      dest: /usr/local/bin/restart_todd
      mode: 0755      

  - name: Copy stop todd bash script
    copy:
      src: config_files/todd/stop_script/stop_todd
      dest: /usr/local/bin/stop_todd
      mode: 0755          

  - name: Copy todd unit systemd service
    become: yes
    become_method: sudo  
    copy:
      src: config_files/todd/unit/
      dest: /etc/systemd/system/
      mode: 0644              

  - name: Add nagios user to sudoers
    become: yes
    become_method: sudo
    shell: adduser nagios sudo      

  - name: restart systemd deamon
    become: yes
    become_method: sudo  
    shell: 'systemctl daemon-reload'

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

  - name: Enable & start todd
    service:
      name: todd
      enabled: yes
      state: restarted      
```

In a summary, it installs all required packages, pulls todd from it's public repository, configures it, builds it, copies todds scripts to start, stop, restart and a systemd unit to control it, configures NRPE and then it enables and starts all the services that will be needed in our todd1 host.

As for our nagios 

```yaml
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

  - name: Copy Nagios config
    copy:
       src: config_files/nagios/nagios-etc/
       dest: /usr/local/nagios/etc/
       mode: 0755

  - name: Copy NRPE config
    copy:
       src: config_files/nrpe/nrpe.cfg
       dest: /etc/nagios/nrpe.cfg
       mode: 0755      

  - name: git clone 
    git:
      repo: 'https://dvazevedo@bitbucket.org/dvazevedo/todd-cogsi.git'
      dest: /home/dan/todd-cogsi
      update: no

  - name: Build todd
    shell: 'cd /home/dan/todd-cogsi && sudo gradle build'      

  - name: Copy todd buid.gradle config
    copy:
      src: config_files/todd/nagios_build_gradle/build.gradle
      dest: /home/dan/todd-cogsi/build.gradle
      mode: 744

  - name: Copy bash start todd script
    copy:
      src: config_files/todd/start_script/start_todd
      dest: /usr/local/bin/start_todd
      mode: 0755

  - name: Copy restart todd bash script
    copy:
      src: config_files/todd/restart_script/restart_todd
      dest: /usr/local/bin/restart_todd
      mode: 0755      

  - name: Copy stop todd bash script
    copy:
      src: config_files/todd/stop_script/stop_todd
      dest: /usr/local/bin/stop_todd
      mode: 0755          

  - name: Copy todd unit systemd service
    become: yes
    become_method: sudo  
    copy:
      src: config_files/todd/unit/
      dest: /etc/systemd/system/
      mode: 0644              
      
  - name: restart systemd deamon
    become: yes
    become_method: sudo  
    shell: 'systemctl daemon-reload'

  - name: Enable & start nagios-nrpe
    service:
      name: nagios-nrpe-server
      enabled: yes
      state: restarted

  - name: Restart nagios using new config
    service:
      name: nagios
      enabled: yes
      state: restarted             

  - name: Enable & start todd
    service:
      name: todd
      enabled: yes
      state: restarted      
```

In a summary what we are doing is updating apt package list, reconfiguring nagios with the configurations specified in the config_files folder, installing todd and all it's unit components and then restarting the services.

In order to run our `ansible-playbooks playbook-nagios.yml` command without the need to specify anything else, like the location of the hosts file, we can have, **in the same folder**, the ansible.cfg file which will tell ansible how it should run it's files when running from that folder.

```r
[defaults]
inventory = hosts
remote_user = vagrant
private_key_file = /home/qwe/playbooks/.vagrant/machines/default/virtualbox/private_key
```

This way, our Jenkins pipeline script will just need to use `ansible-playbooks [playbook of nagios or todd1]` command in order to deploy todd package.

![](https://i.imgur.com/wIE2ccC.png)

As seen in the image below, our Jenkins pipeline is composed of 6 stages.

1- SMC Checkout - clones todd-cogsi repository
2- Build TODD project - builds TODD as name suggests
3- Ask user question to deploy - queries an input before the deploy, as requested in the requirements
4- SMC Checkout VM machines - clones the repository with the ansible-playbooks, Vagrantfile and associated VM configurations (treat infrascructure as code)
5- Deployment - deploys TODD to todd1 and nagios hosts using Ansible and Vagrant to create todd1.

---

# Steps to reproduce

This takes into account the steps and tooling from the previous assignments (1.1 to 1.5) are all installed.

## Install tooling

1- Install jenkins from arch official repositories `sudo pacman -Syu jenkins`

2- Install nexus-oss from AUR `yay nexus-oss`

3- Start the nexus service repository with `sudo systemctl start nexus-oss` and wait about 5 minutes (test by entering localhost:8081)

## Configuration of Nexus

1- Click the toolbox icon and login with `username: admin` and `password admin123`

2- Create a nexus maven2 repository by going to: Configuration > Repositories > Create repository and selec the mavem2 (hosted)

3- Then configurate like the below images:
![](https://i.imgur.com/40btYxc.png)
![](https://i.imgur.com/sPRkcfh.png)

## Configuration of Jenkins

1- First, let's make sure to start the service using `sudo systemctl start jenkins`

2- Nagivate localhost:8090 to follow in initial setup installing the recomended pluggins in the promt

3- After the setup is done, navigate to http://localhost:8090/credentials/store/system/ and click the arrow in front of "Global credentials (unrestricted)" and click > Add credentials

4- Configure the credentials according to your nexus OSS like shown below:

![](https://i.imgur.com/FlVQwHs.png)

5- Add jenkins to the sudoers file so we can change to the user with permissions to use the VMs (this is optional but allows for a better automation).

add to the sudoers file `sudo visudo` the following line:
```bash
jenkins ALL=(ALL) NOPASSWD: ALL
```

6- Finally, create a new piipeline project and name it "cogsi-pl21" and paste the following in the textbox called "Pipeline definition" when selecting Pipeline script of the last dropdown menu:

Note: You should change the path to the "configuration_files" to your own path. The files are all in the provided current folder (same as this README.md file is) in this repository.

```groovy
pipeline {

    agent any

    environment{
        good = 'Aprove and deploy to production.'
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "localhost:8081"
        NEXUS_REPOSITORY = "repository-cogsi"
        NEXUS_CREDENTIAL_ID = "nexus"   
        VAGRANT_PATH = "/home/qwe/Documents/cogsi-18-19-thursday-1180109/1180109-daniel-azevedo/p2.1/configuration_files/ansible-playbook/playbooks/"   
    }

    stages {

        stage('SMC Checkout') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'dvazevedo', url: 'https://dvazevedo@bitbucket.org/dvazevedo/todd-cogsi.git']]])
            }
        }

        stage('Build TODD project') {
            steps {
                echo 'Going for CMS project directory and building...'    
                sh '''cd $WORKSPACE
            gradle build'''}
        }

        stage("Archive to nexus") {

            steps {

                echo 'Stage to archive artifacts to nexus'

                script {

                    // Find built artifact under build/lib folder

                    echo 'Workspace variable is is: ${WORKSPACE}'
                    echo 'doing ls to workspace...'
                    sh 'ls -la ${WORKSPACE}/build/libs'

                    filesByGlob = findFiles(glob: "**/*.jar");

                    // Print some info from the artifact found

                    echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"

                    // Extract the path from the File found

                    artifactPath = filesByGlob[0].path;

                    // Assign to a boolean response verifying If the artifact name exists

                    artifactExists = fileExists artifactPath;

                    if(artifactExists) {

                        echo "*** File: ${artifactPath}";

                        nexusArtifactUploader(

                            nexusVersion: NEXUS_VERSION,

                            protocol: NEXUS_PROTOCOL,

                            nexusUrl: NEXUS_URL,

                            groupId: 'pt.ipp.isep.dei',

                            version: '0.1',

                            repository: NEXUS_REPOSITORY,

                            credentialsId: NEXUS_CREDENTIAL_ID,

                            artifacts: [

                                // Artifact generated such as .jar, .ear and .war files.

                                [artifactId: 'dazevedo.todd',

                                classifier: '',

                                file: artifactPath,

                                type: 'jar']

                            ]

                        );

                    } else {

                        error "*** File: ${artifactPath}, could not be found";

                    }

                }

            }

        }        

        stage('Ask user question to deploy'){
            steps{
                //insert input for the UI acceptance test
                script {    
                    env.UIACCEPTANCE = input message: 'User input required. Deploy?',
                    ok: 'Deploy',
                    parameters: [choice(name: 'Acceptance result', choices: "${good}", description: 'Do you approve the deployment')]
                }
            }
        }

        stage('SMC Checkout VM Machines') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'dvazevedo', url: 'https://dvazevedo@bitbucket.org/mei-isep/cogsi-18-19-thursday-1180109.git']]])
            }
        }        

        stage('Deployment'){
            steps{
                echo 'Starting nagios VM...'
                sh '''
                sudo -H -u qwe bash -c "nohup VBoxHeadless -startvm "ubuntu-cogsi-nagios" & "
                '''
                
                echo 'Nagios VM started.'
                echo 'Now deploying to todd1 through vagrant...'
                script{
                    sh '''
                    sudo -H -u qwe bash -c "cd /home/qwe/Documents/cogsi-18-19-thursday-1180109/1180109-daniel-azevedo/p2.1/configuration_files/ansible-playbook/playbooks/ && vagrant up"
                    sudo -H -u qwe bash -c "cd /home/qwe/Documents/cogsi-18-19-thursday-1180109/1180109-daniel-azevedo/p2.1/configuration_files/ansible-playbook/playbooks/ && vagrant provision"
                    '''
                }
                echo 'Deployed to todd1'
                echo 'Now deploying to nagios'
                script{
                    sh '''
                    sudo -H -u qwe bash -c 'cd /home/qwe/Documents/cogsi-18-19-thursday-1180109/1180109-daniel-azevedo/p2.1/configuration_files/ansible-playbook/playbooks/ && ansible-playbook playbook_nagios.yml'
                    '''
                }

                script{
                    echo 'SUCCESS!!!\n Remember you can control the machines state using ansible and vagrant from now on! :)'
                }

                script{
                   if (env.UIACCEPTANCE=='Aprove and deploy to production.'){
                        echo 'User accepted the deployment..'
                        //Deployment to production conditional
                        echo 'deploy to production with ansible'
                    } else {
                        echo 'User didn\'t accept the deployment. Deployment stopped.'
                    }
                }
            }
        }
    }
}
```
7- Click Save and then "Build now" button in the left navigation panel.

8- To see the output log of the operations of the steps click the arrow in the build job and select console output

After a successfull build, the VM will be running and it should look like this:

![](https://i.imgur.com/TTRNrrg.png)

As for our virtualbox, of we open Oracle VM Virtualbox, it should have 2 virtual machines on: Nagios and our host named todd1

![](https://i.imgur.com/j894xP6.png)


# References


* https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#isunix-checks-if-running-on-a-unix-like-node
* https://docs.ansible.com/
* https://www.vagrantup.com/docs/index.html
* https://help.sonatype.com/repomanager3