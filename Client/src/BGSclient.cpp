#include "ClientToServerTask.h"
#include "ServerToClientTask.h"
#include <stdlib.h>
#include <thread>
#include <mutex>
#include <ConnectionHandler.h>


/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/


int main (int argc, char *argv[]) {

    //////// Parse args ////////
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    

    //////// Create Connection with server ////////
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    //////// Threads: ////////
    // Reading from console and send to server.
    ClientToServerTask clientToServerTask(connectionHandler);
    std::thread clientToServerThread(&ClientToServerTask::operator(), &clientToServerTask);

    // Get messages from server and print them to console.
    ServerToClientTask serverToClientTask(connectionHandler);
    std::thread serverToClientThread(&ServerToClientTask::operator(), &serverToClientTask);

    //////// Finishing the program ////////
    // Wait for ack msg from logout.
    serverToClientThread.join();
    return 0;
}
