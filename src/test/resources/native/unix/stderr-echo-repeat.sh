#!/bin/sh -e

value=$1
repeat=$2

x=1
while [ $x -le $repeat ]
do
   echo "$value" 1>&2
   x=$(( $x + 1 ))
done