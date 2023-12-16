#!/bin/bash
clear
echo

counter=0
while [ $counter -le 0 ]
do
    java -cp /usr/share/java/xmlrpc-client.jar:/usr/share/java/xmlrpc-server.jar:/usr/share/java/xmlrpc-common.jar:/usr/share/java/ws-commons-util.jar:/usr/share/java/commons-logging.jar:. TestClient 107.23.192.217 continuedLookup smart 
    ((counter++))
done
