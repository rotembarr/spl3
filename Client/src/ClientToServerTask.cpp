#include <ClientToServerTask.h>

std::string shortToString(short num) {
    std::string s = "";
    s = (char)((num >> 8) & 0xFF);
    s += (char)(num & 0xFF);
    return s;
}

ClientToServerTask::ClientToServerTask(ConnectionHandler& _connectionHandler) :
    connectionHandler(_connectionHandler) {
}

void ClientToServerTask::operator()() {

    while (1) {
        // Variables.
        std::vector<std::string> command;
        std::string line = "";

        // Get user command.
        std::getline(std::cin, line);
        std::istringstream stream(line);

        // Parse command
        std::string word;
        while (stream >> word) {
            command.push_back(word);
        }

        // Send msg to server.
        std::string msg = "";
        if (command[0].compare("REGISTER") == 0) {
            msg = shortToString(1) + command[1] + '\0' + command[2] + '\0' + command[3] + '\0';
            
        } else if (command[0].compare("LOGIN") == 0) {
            msg = shortToString(2) + command[1] + '\0' + command[2] + '\0' + (char)1;

        } else if (command[0].compare("LOGOUT") == 0) {
            msg = shortToString(3);

        } else if (command[0].compare("FOLLOW") == 0) {
            int f = 0;
            if (command[1].compare("0") == 0) {
                f = 0;
            } else if (command[1].compare("1") == 0) {
                f = 1;
            } else {
                std::cout << "Bad argument" << std::endl;
            }
            msg = shortToString(4) + (char)f + command[2] + '\0';

        } else if (command[0].compare("POST") == 0) {
            msg = shortToString(5) + command[1] + '\0';

        } else if (command[0].compare("PM") == 0) {
            std::time_t now = std::time(0);
            std::tm* ltm = localtime(&now);
            std::string date = std::to_string(ltm->tm_mday) + '-' + std::to_string(1+ltm->tm_mon) + '-' + std::to_string(1900 + ltm->tm_year);
            msg = shortToString(6) + command[1] + '\0' + command[2] + '\0' + date + '\0';

        } else if (command[0].compare("LOGSTAT") == 0) {
            msg = shortToString(7);

        } else if (command[0].compare("STAT") == 0) {
            msg = shortToString(8) + command[1] + '\0';

        } else if (command[0].compare("BLOCK") == 0) {
            msg = shortToString(12) + command[1] + '\0';
        } else {
            std::cout << "Wromg command given: " + line << std::endl;
        }

        // Error madafakkaaa.
        if (!this->connectionHandler.sendLine(msg)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
    }

}