#!/bin/bash
clear
echo "---------Starting Test 3---------" 
echo

counter=0
while [ $counter -le 20 ]
do
    java -cp /usr/share/java/xmlrpc-client.jar:/usr/share/java/xmlrpc-server.jar:/usr/share/java/xmlrpc-common.jar:/usr/share/java/ws-commons-util.jar:/usr/share/java/commons-logging.jar:. TestClient 34.228.197.147 add dumb &
    ((counter++))
done

echo "---------Test 3 Ended------------" 