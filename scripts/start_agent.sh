#!/bin/bash
current_dir=$(dirname $0)
app_jar=/mnt/d/code/e4/mac-agent/build/libs/mac-agent-1.0.jar
cd $current_dir

JAVA_HOME=/home/wei/jdk1.8.0_212
LIB="$JAVA_HOME/lib/tools.jar:$JAVA_HOME/jre/lib/rt.jar"
OPTIONS="-Djava.library.path=$JAVA_HOME/jre/bin -Dbundles.list=/mnt/d/code/e4/mac-agent/sample/bundles"

echo $LIB > /tmp/a.log
if [ -n "$1" ]; then
    # ${JAVA_HOME}/bin/java ${OPTIONS} -cp $LIB -jar ${app_jar} $1 enable
    java ${OPTIONS} -jar ${app_jar} $1 enable
else
    echo "please provide PID as inputs"
fi