FROM ubuntu:latest
RUN apt-get -y update && apt-get -y upgrade
RUN apt-get -y install openjdk-8-jdk gradle wget git ssh nagios-nrpe-server vim nsca sudo
RUN mkdir /home/dan/
RUN cd /home/dan && git clone https://dvazevedo@bitbucket.org/dvazevedo/todd-cogsi.git
RUN cd /home/dan/todd-cogsi && gradle build
CMD cd /home/dan/todd-cogsi && service nsca start && service ssh start && service nagios-nrpe-server start && ( gradle runserverremote & ) && sleep 3 && ( gradle runclient3 & ) && tail -f /dev/null 