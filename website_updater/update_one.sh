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

rm daily.csv
rm weekly.csv
rm weekly.js

./run.sh $1

if [ ! -f daily.csv ] ; then
  echo "The file 'daily.csv' is missing."
  exit 1
fi;

if [ ! -f weekly.csv ] ; then
  echo "The file 'weekly.csv' is missing."
  exit 1
fi;

if [ ! -f weekly.js ] ; then
  echo "The file 'weekly.js' is missing."
  exit 1
fi;

cp daily.csv website/data/$2-dnevni-danni.csv
echo "The file 'website/data/$2-dnevni-danni.csv' has been processed."

cp weekly.csv website/data/$2-sedmichni-danni.csv
echo "The file 'website/data/$2-sedmichni-danni.csv' has been processed."

cp weekly.js website/data/$2.js
echo "The file 'website/data/$2.js' has been processed."

cd website_updater
