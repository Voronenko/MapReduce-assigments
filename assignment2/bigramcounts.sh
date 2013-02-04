#!/bin/sh
DIR="$( cd "$( dirname "$0" )" && pwd )"
echo Running in $DIR
echo Build assigment
#ant -buildfile $DIR/src/build.xml
#echo Removing /user/$USER/wc
#hadoop fs -rmr /user/$USER/wc
#echo Removing local wc
#rm -r $DIR/wc
#mkdir wc

echo Run task
echo $DIR/src/etc/hadoop-local.sh BigramCount -input bible+shakes.nopunc.gz -output bigram 

#echo copy result to local filesystem
#hadoop fs -get /user/$USER/wc/* $DIR/wc



