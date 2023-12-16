#!/bin/bash
clear
echo "---------Cloing machines---------" 

ipAddress=$1
cd ../
cd ~/.ssh
scp eokyere-keypair* $ipAddress:~/.ssh
scp id_rsa* $ipAddress:~/.ssh
scp known_hosts* $ipAddress:~/.ssh
ssh $ipAddress "git clone git@github.com:bowdoin-dsys/p4-final-abhi-eman.git && cd p4-final-abhi-eman && make"

echo "$ipAddress"
