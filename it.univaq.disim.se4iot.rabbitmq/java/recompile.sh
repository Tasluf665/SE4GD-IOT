#!/bin/sh

BASE="/mnt/c/Users/david/git/se4iot/rabbitmq-tutorials/java";
javac -cp .;$BASE/amqp-client-5.6.0.jar;$BASE/slf4j-api-1.7.26.jar;$BASE/slf4j-simple-1.7.26.jar EmitLogDirect.java  ReceiveLogsDirect.java
