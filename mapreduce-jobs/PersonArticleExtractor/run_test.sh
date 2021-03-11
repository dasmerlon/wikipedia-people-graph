#!/bin/bash
# Uploads and executes the newest jar to the server

# Add this to your .ssh/config
# Host basecpu1
#     User YOUR_USER_HERE
#     HostName basecpu1.informatik.uni-hamburg.de
#     IdentityFile ~/.ssh/id_rsa

scp target/PersonArticle-1.jar basecpu1:~/jars/PersonArticle.jar
ssh basecpu1 'hadoop fs -rm -r test/jars/PersonArticle.jar'
ssh basecpu1 'hadoop fs -rm -r test/output/PersonArticle'
ssh basecpu1 'hadoop fs -copyFromLocal jars/PersonArticle.jar test/jars/PersonArticle.jar'
ssh basecpu1 'hadoop jar jars/PersonArticle.jar /user/7mhoffma/test/data/wikipages.xml test/output/PersonArticle loglevel debug'
