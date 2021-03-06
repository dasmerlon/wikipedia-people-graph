#!/bin/bash
# Uploads and executes the newest jar to the server

# Add this to your .ssh/config
# Host basecpu1
#     User YOUR_USER_HERE
#     HostName basecpu1.informatik.uni-hamburg.de
#     IdentityFile ~/.ssh/id_rsa

scp target/Titles-1.jar basecpu1:~/jars/Titles.jar
ssh basecpu1 'hadoop fs -rm -r test/jars/Titles.jar'
ssh basecpu1 'hadoop fs -rm -r test/output/PersonTitles'
ssh basecpu1 'hadoop fs -copyFromLocal jars/Titles.jar test/jars/Titles.jar'
ssh basecpu1 'hadoop jar jars/Titles.jar /user/7mhoffma/test/data/personpages.xml test/output/PersonTitles loglevel debug'
