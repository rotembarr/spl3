CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_thread -lpthread

all: comp

comp: BGSclient
	g++ -o bin/BGSclient bin/ConnectionHandler.o bin/ClientToServerTask.o bin/ServerToClientTask.o bin/BGSclient.o $(LDFLAGS)

BGSclient: bin/ConnectionHandler.o bin/ClientToServerTask.o bin/ServerToClientTask.o bin/BGSclient.o
	
bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/BGSclient.o: src/BGSclient.cpp
	g++ $(CFLAGS) -o bin/BGSclient.o src/BGSclient.cpp

bin/ClientToServerTask.o: src/ClientToServerTask.cpp
	g++ $(CFLAGS) -o bin/ClientToServerTask.o src/ClientToServerTask.cpp

bin/ServerToClientTask.o: src/ServerToClientTask.cpp
	g++ $(CFLAGS) -o bin/ServerToClientTask.o src/ServerToClientTask.cpp


run: comp
	./bin/BGSclient 10.0.2.15 9999

.PHONY: clean
clean:
	rm -f bin/*
