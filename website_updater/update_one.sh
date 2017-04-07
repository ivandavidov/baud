#!/bin/sh

JAVA_HOME=/home/ivan/jdk
PATH=$JAVA_HOME/bin:$PATH

cd ..

if [ "$1" = "" ] ; then
  echo "Parameter 'fond' is missing."
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

cp weekly.js website/$2

echo "The file '$2' has been processed."

cd website_updater

