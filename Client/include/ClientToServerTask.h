#ifndef CLIENT_TO_SERVER_TASK
#define CLIENT_TO_SERVER_TASK

#include <stdlib.h>
#include <ctime>
#include "ConnectionHandler.h"

class ClientToServerTask {
private: 
    ConnectionHandler& connectionHandler;

public:   
    ClientToServerTask(ConnectionHandler& _connectionHandler);
    void operator()();
};

#endif