#!/bin/sh 
 if [ "$RCLIENTHOME" = "" ]; then 
        RCLIENTHOME=`pwd` 
fi 

export CLASSPATH="$RCLIENTHOME:$RCLIENTHOME/commons-logging-api-1.0.4.jar:$RCLIENTHOME/httpclient-4.5.2.jar:$RCLIENTHOME/httpcore-4.4.4.jar:$RCLIENTHOME/json-20160810.jar" 

/usr/bin/javac $RCLIENTHOME/RestClient.java 
/usr/bin/java RestClient $* 
