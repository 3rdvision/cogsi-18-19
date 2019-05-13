#!/bin/bash
ip=$(ip addr show eth1 | grep "inet\b" | awk '{print $2}' | cut -d/ -f1)
CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=6003 -Dcom.sun.management.jmxremote.rmi.port=6003 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=$ip"