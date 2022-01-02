CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_thread -lpthread

all: BGSclient
	g++ -o bin/BGSclient bin/ConnectionHandler.o bin/ReadTask.o bin/BGSclient.o $(LDFLAGS)

BGSclient: bin/ConnectionHandler.o bin/ReadTask.o bin/BGSclient.o
	
bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/BGSclient.o: src/BGSclient.cpp
	g++ $(CFLAGS) -o bin/BGSclient.o src/BGSclient.cpp

bin/ReadTask.o: src/ReadTask.cpp
	g++ $(CFLAGS) -o bin/ReadTask.o src/ReadTask.cpp


.PHONY: clean
clean:
	rm -f bin/*
