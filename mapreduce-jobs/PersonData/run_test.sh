#!/bin/bash
# Uploads and executes the newest jar to the server

# Add this to your .ssh/config
# Host basecpu1
#     User YOUR_USER_HERE
#     HostName basecpu1.informatik.uni-hamburg.de
#     IdentityFile ~/.ssh/id_rsa

scp target/PersonData-1.jar basecpu1:~/jars/PersonData.jar
ssh basecpu1 'hadoop fs -rm -r test/jars/PersonData.jar'
ssh basecpu1 'hadoop fs -rm -r test/output/PersonData'
ssh basecpu1 'hadoop fs -copyFromLocal jars/PersonData.jar test/jars/PersonData.jar'
ssh basecpu1 'hadoop jar jars/PersonData.jar /user/7mhoffma/test/data/personpages.xml test/output/PersonData loglevel debug'
