#!/bin/bash
clear
echo "---------Starting Test 3---------" 

./killServers.sh
# ireland
# ./cloneOnMachine.sh 34.245.77.164
./runFrontEnd.sh 34.245.77.164

#ireland #2
# ./cloneOnMachine.sh 54.216.88.215
./runDatabase.sh 54.216.88.215 34.245.77.164

#tokyo #1
#./cloneOnMachine.sh 13.114.101.52
./runOtherFrontEnds.sh 13.114.101.52 34.245.77.164

# ./cloneOnMachine.sh 3.112.237.22
#tokyo #2
./runDatabase.sh 3.112.237.22 13.114.101.52

