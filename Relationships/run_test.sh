#!/bin/bash
# Uploads and executes the newest jar to the server

# Add this to your .ssh/config
# Host basecpu1
#     User YOUR_USER_HERE
#     HostName basecpu1.informatik.uni-hamburg.de
#     IdentityFile ~/.ssh/id_rsa

scp target/Relationships-1.jar basecpu1:~/jars/Relationships.jar
ssh basecpu1 'hadoop fs -rm -r test/jars/Relationships.jar'
ssh basecpu1 'hadoop fs -rm -r test/output/Relationships'
ssh basecpu1 'hadoop fs -copyFromLocal jars/Relationships.jar test/jars/Relationships.jar'
ssh basecpu1 'hadoop jar jars/Relationships.jar /user/7mhoffma/test/data/personpages.xml test/output/Relationships loglevel debug'
