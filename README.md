# spl3

## Description
This is a repo for Assignment 3 in SPL course at BGU.
This is a client server repo that simulates twitter.
The client written in cpp and the server written in java.
For more details visit: https://moodle2.bgu.ac.il/moodle/mod/assign/view.php?id=2028488

## Compiling & Running
### Client
cpp build tools used is makefile.
In order to Compile the client:
1. cd Client
2. make comp
In order to run the client (after compiling):
3. ./bin/BGSclient <Server ip address> <Port>

Example of stage 3:
    ./bin/BGSclient 10.0.2.15 9999

### Server
java build too use is maven.
In order to Compile the server:
1. cd Server
2. mvn compile
In order to run the server (after compiling) You have two options:
3. mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args="'<Port>'"  
or
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="'<Port>' '<Max number of threads>'"  

Example of stage 3:
    mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args="'9999'"
or
    mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="'9999' '4'"

#### Filter
Filter with all the words needes to be filtered located in: ./Server/src/main/java/bgu/spl/net/impl/BGSServer/Filter.java 

### Attention
1. Server nust start running before client
2. The ip address givven to the client has to be the ip address the server running on.
3. The port givven to the client has to be the same port givven to the server.