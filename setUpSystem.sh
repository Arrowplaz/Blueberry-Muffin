#!/bin/bash
clear
echo "---------Starting Test 3---------" 

./killServers.sh

#Region 1: North America
#Virginia(Database)
./cloneOnMachine.sh 3.89.231.167
./runFrontEnd.sh 3.89.231.167
./cloneOnMachine.sh 54.204.131.69
./runFrontEnd.sh 54.204.131.69
#Oregon (CLIENT)
./cloneOnMachine.sh 34.217.13.50
./runFrontEnd.sh 34.217.13.50
./cloneOnMachine.sh 52.25.77.13
./runFrontEnd.sh 52.25.77.13
#Ohio(FE)
./cloneOnMachine.sh 3.128.204.116 
./runFrontEnd.sh 3.128.204.116 
./cloneOnMachine.sh 3.135.204.68
./runFrontEnd.sh 3.135.204.68

#Region 2: East Asia
#Tokyo(Database)
./cloneOnMachine.sh 13.114.101.52
./runFrontEnd.sh 13.114.101.52
./cloneOnMachine.sh 3.112.237.22
./runFrontEnd.sh 3.112.237.22
#Seoul (CLIENT)
./cloneOnMachine.sh 54.180.122.185
./runFrontEnd.sh 54.180.122.185
./cloneOnMachine.sh 54.180.82.204
./runFrontEnd.sh 54.180.82.204
#Osaka(FE)
./cloneOnMachine.sh 15.152.54.178
./runFrontEnd.sh 15.152.54.178
./cloneOnMachine.sh 13.208.251.78
./runFrontEnd.sh 13.208.251.78

#Region 3: South East Asia
#Mumbai (CLIENT)
./cloneOnMachine.sh 3.110.118.143
./runFrontEnd.sh 3.110.118.143
./cloneOnMachine.sh 3.111.39.82
./runFrontEnd.sh 3.111.39.82
#Jakarta(Database)
./cloneOnMachine.sh 108.137.2.253
./runFrontEnd.sh 108.137.2.253
./cloneOnMachine.sh 108.136.135.181
./runFrontEnd.sh 108.136.135.181
#Singapore(FE)
./cloneOnMachine.sh 108.137.2.253
./runFrontEnd.sh 108.137.2.253
./cloneOnMachine.sh 54.169.70.132
./runFrontEnd.sh 54.169.70.132

#Region 4: Middle East
#UAE(Database)
./cloneOnMachine.sh 3.28.44.128
./runFrontEnd.sh 3.28.44.128
./cloneOnMachine.sh 3.29.126.32
./runFrontEnd.sh 3.29.126.32
#Tel Aviv (CLIENT)
./cloneOnMachine.sh 51.17.24.10
./runFrontEnd.sh 51.17.24.10
./cloneOnMachine.sh 51.17.168.219
./runFrontEnd.sh 51.17.168.219
#Bahrain(FE)
./cloneOnMachine.sh 15.185.38.199
./runFrontEnd.sh 15.185.38.199
./cloneOnMachine.sh 157.175.85.120
./runFrontEnd.sh 157.175.85.120

#Region 5: Europe
#Paris(Database)
./cloneOnMachine.sh 52.47.75.71
./runFrontEnd.sh 52.47.75.71
./cloneOnMachine.sh 13.39.18.95
./runFrontEnd.sh 13.39.18.95
#Milan(FE)
./cloneOnMachine.sh 15.161.56.73
./runFrontEnd.sh 15.161.56.73
./cloneOnMachine.sh 15.160.247.27
./runFrontEnd.sh 15.160.247.27
#London (CLIENT)
./cloneOnMachine.sh 13.40.228.246
./runFrontEnd.sh 13.40.228.246
./cloneOnMachine.sh 3.10.51.103
./runFrontEnd.sh 3.10.51.103

#Region 6: Europe 2
#Frankfurt (Database)
./cloneOnMachine.sh 18.192.57.65
./runFrontEnd.sh 18.192.57.65
./cloneOnMachine.sh 3.67.193.200
./runFrontEnd.sh 3.67.193.200
#Stockholm(CLIENT)
./cloneOnMachine.sh 51.20.98.34
./runFrontEnd.sh 51.20.98.34
./cloneOnMachine.sh 13.48.48.239
./runFrontEnd.sh 13.48.48.239
#Ireland(FE)
./cloneOnMachine.sh 51.20.98.34
./runFrontEnd.sh 51.20.98.34
./cloneOnMachine.sh 34.245.77.164
./runFrontEnd.sh 34.245.77.164





















