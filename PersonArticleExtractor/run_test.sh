#!/bin/bash
# Uploads and executes the newest jar to the server

# Add this to your .ssh/config
# Host basecpu1
#     User YOUR_USER_HERE
#     HostName basecpu1.informatik.uni-hamburg.de
#     IdentityFile ~/.ssh/id_rsa

scp target/PersonData-1.0-SNAPSHOT.jar basecpu1:~/jars/test.jar
ssh basecpu1 'hadoop fs -rm -r testicle'
ssh basecpu1 'hadoop jar jars/test.jar /user/7mhoffma/Test_osteron/test.xml testicle loglevel debug'
