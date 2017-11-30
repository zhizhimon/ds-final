package DCMP;

import java.io.Serializable;

public class Request implements Serializable {
    int myId;
    int regret;
    char gender;
    public Request(int myId, int regret){
        this.myId = myId;
        this.regret = regret;
    }
    public Request(int myId, int regret, char gender){
        this.myId = myId;
        this.regret = regret;
        this.gender = gender;
    }
}
