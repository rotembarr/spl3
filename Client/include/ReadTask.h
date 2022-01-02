#ifndef READ_TASK
#define READ_TASK

#include <stdlib.h>
#include <ctime>
#include "ConnectionHandler.h"

class ReadTask {
private: 
    ConnectionHandler& connectionHandler;

public:   
    ReadTask(ConnectionHandler& _connectionHandler);
    virtual ~ReadTask();
    void operator()();
};

#endif