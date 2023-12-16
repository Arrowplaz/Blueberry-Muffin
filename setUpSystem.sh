#!/bin/bash
clear
echo "---------Starting Test 3---------" 

#./killServers.sh
# ireland
# ./cloneOnMachine.sh 3.128.204.116
# ./runFrontEnd.sh 3.128.204.116
# # --- REGION 1 North
# #Virginia
# ./cloneOnMachine.sh 107.23.192.217
# ./runDatabase.sh 107.23.192.217 3.128.204.116

# #tokyo #1
# ./cloneOnMachine.sh 3.135.204.68
# ./runOtherFrontEnds.sh 3.135.204.68 3.128.204.116

# cloneOnMachine.sh 54.204.131.69
# #tokyo #2
# ./runDatabase.sh 54.204.131.69 3.135.204.68

#Start first FE
#./cloneOnMachine.sh 3.128.204.116 #Ohio
./runFrontEnd.sh 3.128.204.116

#Connect all Front end
#./cloneOnMachine.sh 15.152.54.178 #Osaka
echo "adding front end"
./runOtherFrontEnds.sh 15.152.54.178 3.128.204.116

./cloneOnMachine.sh 54.169.70.132 #Singapore
./runOtherFrontEnds.sh 54.169.70.132 3.128.204.116

./cloneOnMachine.sh 15.185.38.199 #Bahrain(FE)
./runOtherFrontEnds.sh 15.185.38.199 3.128.204.116

./cloneOnMachine.sh 15.161.56.73 #London
./runOtherFrontEnds.sh 15.161.56.73 3.128.204.116

#./cloneOnMachine.sh 34.245.77.164 #Ireland(FE)
./runOtherFrontEnds.sh 34.245.77.164 3.128.204.116

##Connect DB to FE respectivley
#./cloneOnMachine.sh 107.23.192.217 #Virginia
#./cloneOnMachine.sh 54.204.131.69
#