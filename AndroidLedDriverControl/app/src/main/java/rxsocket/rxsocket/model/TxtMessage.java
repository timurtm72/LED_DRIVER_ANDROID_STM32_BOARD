package rxsocket.rxsocket.model;

public class TxtMessage {
    public String[] message = new String[5];
    public TxtMessage(String temperaturaMessage, String humidityMessage,
                      String LightMessage, String wifiStatusMessage) {

        this.message[0] = temperaturaMessage;
        this.message[1] = humidityMessage;
        this.message[2] = LightMessage;
        this.message[3] = wifiStatusMessage;
     }
        public String[] getMessage() {
            return message;
        }
}