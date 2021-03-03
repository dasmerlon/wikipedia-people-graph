#!/bin/bash

scp target/PersonData-1.0-SNAPSHOT.jar basecpu1:~/test.jar
ssh basecpu1 '~/run.sh'
