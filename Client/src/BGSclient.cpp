#include "ReadTask.h"
#include <stdlib.h>
#include <thread>
#include <mutex>
#include <ConnectionHandler.h>


/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/


std::string shortToString2(short num) {
    std::string s = "";
    s = s + (char)(num & 0xFF);
    s = (char)((num >> 8) & 0xFF);
}

int main (int argc, char *argv[]) {
    // std::cout << shortToString2((short)49) << std::endl;
    // return 0;

    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    ReadTask readTask(connectionHandler);
    std::thread readThread(&ReadTask::operator(), &readTask);
    readThread.join();

	//From here we will see the rest of the ehco client implementation:
    
 
        // // We can use one of three options to read data from the server:
        // // 1. Read a fixed number of characters
        // // 2. Read a line (up to the newline character using the getline() buffered reader
        // // 3. Read up to the null character
        // std::string answer;
        // // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
        // // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
        // if (!connectionHandler.getLine(answer)) {
        //     std::cout << "Disconnected. Exiting...\n" << std::endl;
        //     break;
        // }
        
		// len=answer.length();
		// // A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
		// // we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
        // answer.resize(len-1);
        // std::cout << "Reply: " << answer << " " << len << " bytes " << std::endl << std::endl;
        // if (answer == "bye") {
        //     std::cout << "Exiting...\n" << std::endl;
        //     break;
        // }
    return 0;
}
