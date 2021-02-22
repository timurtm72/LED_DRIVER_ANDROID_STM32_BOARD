package rxsocket.rxsocket.controller;

import java.nio.charset.Charset;

import moe.codeest.rxsocketclient.RxSocketClient;
import moe.codeest.rxsocketclient.SocketClient;
import moe.codeest.rxsocketclient.meta.SocketConfig;
import moe.codeest.rxsocketclient.meta.ThreadStrategy;

public class RxConnectionHelper {
    private static final int TIME_OUT = 15;
    private static String ip = "";//"192.168.0.1";
    private static int port = 0;//9876;

    private static RxConnectionHelper instance;

    private static SocketClient client;

    private RxConnectionHelper() {
    }

    public static void setSettings(String ip, int port) {
        RxConnectionHelper.ip = ip;
        RxConnectionHelper.port = port;
    }

    public static synchronized RxConnectionHelper getInstance() {

        if (instance == null) {
            instance = new RxConnectionHelper();
        }
        return instance;
    }

    public SocketClient getClient() {
        client = RxSocketClient
                .create(new SocketConfig.Builder()
                        .setIp(ip)
                        .setPort(port)
                        .setCharset(Charset.defaultCharset())
                        .setThreadStrategy(ThreadStrategy.ASYNC)
                        .setTimeout(TIME_OUT * 1000)
                        .build());
        return client;
    }
}
