#!/bin/bash

client="SWING"
parser="GITHUB"
differ="SEED"
rewrite="0.5"

if [ $# -ge 1 ]
then
  if [ $# -ge 2 ]
  then
    java -jar build/libs/cidiff-next-1.0-SNAPSHOT.jar "${1}/pass.log" "${1}/fail.log" -o client $client -o parser $parser -o differ ${2} -o differ.seed.window 1 -o differ.rewrite.min $rewrite
  else
    java -jar build/libs/cidiff-next-1.0-SNAPSHOT.jar "${1}/pass.log" "${1}/fail.log" -o client $client -o parser $parser -o differ $differ -o differ.seed.window 1 -o differ.rewrite.min $rewrite
  fi
else
  echo "missing arguments"
fi

