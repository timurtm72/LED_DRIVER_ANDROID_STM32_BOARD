package rxsocket.rxsocket.model;

import org.json.JSONException;

public interface Contract {
    interface View{
        void OnConnected();
        void OnDisconnected();
        void onResult(String data) throws JSONException;
    }
    interface Presenter{
        void connectServer();
        void disConnectServer();
        void sendData(String data);
        void presenterDispose();
    }
}
