# Welcome to ZIO Kafka 

This folder contains the simplest possible Scala implementation of Kafka Сonsumer. 
This assumes you're running a ZooKeeper, Kafka Server and Producer from shell. 

Installation and setup insructions can be found [here](https://tecadmin.net/install-apache-kafka-ubuntu)

This program simply connects to your localhost  broker and prints string messages from the console. 

## Usage
```bash
sbt "runMain hello.SimpleConsumer"
```
Sample output:
```bash
[info]  Received message: (null, Hi Boris how are you?) at offset 10
[info]  Received message: (null, ) at offset 11
[info]  Received message: (null, Hi Boris, how are you?) at offset 12
```
