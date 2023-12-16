#!/bin/bash
clear
echo "---------Starting Test 3---------" 

ipAddress=$1

ssh $ipAddress "cd p4-final-abhi-eman && git pull && make && java -cp /usr/share/java/xmlrpc-client.jar:/usr/share/java/xmlrpc-server.jar:/usr/share/java/xmlrpc-common.jar:/usr/share/java/ws-commons-util.jar:/usr/share/java/commons-logging.jar:. DatabaseServer $1 $2 &"

echo "$ipAddress"
