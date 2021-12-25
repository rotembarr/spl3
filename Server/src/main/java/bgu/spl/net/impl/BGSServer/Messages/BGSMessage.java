package bgu.spl.net.impl.BGSServer.Messages;


public class BGSMessage {

    public enum OPCODES {
        NONE            {public String toString() {return "00";}}, 
        REGISTER        {public String toString() {return "01";}},
        LOGIN           {public String toString() {return "02";}},
        LOGOUT          {public String toString() {return "03";}},
        FOLLOW          {public String toString() {return "04";}},
        POST            {public String toString() {return "05";}},
        PM              {public String toString() {return "06";}},
        LOGSTAT         {public String toString() {return "07";}},
        STAT            {public String toString() {return "08";}},
        NOTIFICATION    {public String toString() {return "09";}},
        ACK             {public String toString() {return "00";}},
        ERROR           {public String toString() {return "00";}},
        BLOCK           {public String toString() {return "00";}}
    };
}
