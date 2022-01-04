#ifndef SERVER_TO_CLIEANT_TASK
#define SERVER_TO_CLIEANT_TASK

#include <stdlib.h>
#include <ctime>
#include "ConnectionHandler.h"


class ServerToClientTask {
private:
    ConnectionHandler& connectionHandler;

public:   
    ServerToClientTask(ConnectionHandler& _connectionHandler);
    void operator()();

}; 

#endif