package rxsocket.rxsocket.model;

public class PwmMessage {
    private String pwmLevel;

    public PwmMessage(String pwmLevel) {
        this.pwmLevel = pwmLevel;
    }
    public String getPwmMessage(){
        return pwmLevel;
    }
}
