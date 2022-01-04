#include <ServerToClientTask.h>

short stringToShort(std::string s) {
    short num;
    num = (short)(s.at(0) << 8);
    num += (short)(s.at(1));
    return num;
}

ServerToClientTask::ServerToClientTask(ConnectionHandler& _connectionHandler) :
    connectionHandler(_connectionHandler) {
}

void ServerToClientTask::operator()() {

    while (1) {
        std::string srvAnswer = "";
        std::string line = "";
        bool shouldTerminate = false;

        // Get Server msgs.
        if (!connectionHandler.getLine(srvAnswer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        short opcode = stringToShort(srvAnswer.substr(0,2));
        std::string msg = srvAnswer.substr(2);
    
        // Notificationa
        if (opcode == 9) {
            line = "NOTIFICATION ";

            // Add PM/Public.
            if (msg.at(0) == 0) {
                line += "PM ";
            } else if (msg.at(0) == 1) {
                line += "Public ";
            }
            msg = msg.substr(1);

            // Add username and content
            std::replace(msg.begin(), msg.end(), '\0', ' '); // Change '\0' to ' '
            line += msg;

        // Ack
        } else if (opcode == 10) {
            short msgOpcode = stringToShort(msg.substr(0,2));
            line = "ACK " + std::to_string(msgOpcode) + " " + msg.substr(2);

            // Terminate if getting ack after logout.
            if (msgOpcode == 3) {
                shouldTerminate = true;
            }

        // Error
        } else if (opcode == 11) {
            short msgOpcode = stringToShort(msg.substr(0,2));
            line = "ERROR " + std::to_string(msgOpcode);

        } else {
            line = "Wrong opcode recieved " + std::to_string(opcode);
        }

        // Print line to the console.
        std::cout << line << std::endl;

        if (shouldTerminate) {
            break;
        }
        // if (ans.)
    }
        
}

