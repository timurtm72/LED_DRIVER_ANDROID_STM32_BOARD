package rxsocket.rxsocket.controller;

import org.json.JSONException;

import java.nio.charset.StandardCharsets;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import moe.codeest.rxsocketclient.SocketClient;
import moe.codeest.rxsocketclient.SocketSubscriber;
import rxsocket.rxsocket.model.Contract;

public class Presenter implements Contract.Presenter {

    private Contract.View view;
    private SocketClient client;
    private Disposable ref;

    public Presenter(Contract.View view) {
        this.view = view;
        client = RxConnectionHelper.getInstance().getClient();
     }
    @Override
    public void connectServer() {
          ref = client.connect()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SocketSubscriber() {
                    @Override
                    public void onConnected() {
                        view.OnConnected();
                    }

                    @Override
                    public void onDisconnected() {
                        view.OnDisconnected();
                    }

                    @Override
                    public void onResponse( byte[] bytes) {
                        String str = new String(bytes,StandardCharsets.UTF_8);
                        try {
                            view.onResult(str);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    @Override
    public void disConnectServer() {
            client.disconnect();
            //ref.dispose();
    }

    @Override
    public void sendData(String data) {
         client.sendData(data);
    }

    @Override
    public void presenterDispose() {
        //if(ref!=null) {
            ref.dispose();
       // }
    }
}
