#!/bin/sh

JAVA_HOME=/home/ivan/jdk
PATH=$JAVA_HOME/bin:$PATH

cd ..

if [ "$1" = "" ] ; then
  echo "Parameter 'fundId' is missing."
  exit 1
fi;

if [ "$2" = "" ] ; then
  echo "Parameter for JS file is missing."
  exit 1
fi;

rm weekly.js

./run.sh $1

if [ ! -f "weekly.js" ] ; then
  echo "The file 'weekly.js' is missing."
  exit 1
fi;

cp weekly.js website/data/$2.js

echo "The file 'website/data/$2.js' has been processed."

cd website_updater
