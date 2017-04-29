#!/bin/sh

JAVA_HOME=/home/ivan/jdk
PATH=$JAVA_HOME/bin:$PATH

cd /home/ivan/projects/baud

python list-funds.py

./run.sh

cd /home/ivan/projects/baud/website_updater

