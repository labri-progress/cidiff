#!/bin/bash

client="MONACO"
parser="GITHUB"
differ="SEED"
rewrite="0.5"

if [ $# -ge 1 ]
then
  if [ $# -ge 2 ]
  then
    java -jar build/libs/cidiff-1.0-SNAPSHOT-all.jar "${1}/pass.log" "${1}/fail.log" -o client ${2} -o parser $parser -o differ $differ -o differ.rewrite_min $rewrite
  else
    java -jar build/libs/cidiff-1.0-SNAPSHOT-all.jar "${1}/pass.log" "${1}/fail.log" -o client $client -o parser $parser -o differ $differ -o differ.rewrite_min $rewrite
  fi
else
  echo "missing arguments"
fi

