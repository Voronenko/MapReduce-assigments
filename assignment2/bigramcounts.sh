#!/bin/sh
DIR="$( cd "$( dirname "$0" )" && pwd )"
echo Running in $DIR
echo This is bigram count demo found at http://lintool.github.com/Cloud9/docs/exercises/bigrams.html

echo Removing local bigram
rm -r $DIR/bigram
mkdir bigram

echo Run task
$DIR/src/etc/hadoop-local.sh BigramCount $DIR/src/data/bible+shakes.nopunc.gz bigram 1
$DIR/src/etc/run.sh AnalyzeBigramCount  bigram > $DIR/result1.txt
cat $DIR/result1.txt




