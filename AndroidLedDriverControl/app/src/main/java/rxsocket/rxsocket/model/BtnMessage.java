package rxsocket.rxsocket.model;

public class BtnMessage {
    private int[] data = new int[3];

    public BtnMessage(int btnId, int swStatus, int sbData) {
        this.data[0] = btnId;
        this.data[1] = swStatus;
        this.data[2] = sbData;
    }

   public int[] getMessage(){
        return data;
   }
}
