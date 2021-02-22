package rxsocket.rxsocket.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.stetcho.rxwifip2pmanager.data.wifi.RxWifiP2pManager;
import com.stetcho.rxwifip2pmanager.data.wifi.broadcast.factory.WifiP2pBroadcastObservableManagerFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rxsocket.rxsocket.R;
import rxsocket.rxsocket.controller.Presenter;
import rxsocket.rxsocket.controller.RxConnectionHelper;
import rxsocket.rxsocket.fragment.BottomFragment;
import rxsocket.rxsocket.fragment.TopFragment;
import rxsocket.rxsocket.model.BtnMessage;
import rxsocket.rxsocket.model.Contract;
import rxsocket.rxsocket.model.PwmMessage;
import rxsocket.rxsocket.model.TxtMessage;
import rxsocket.rxsocket.util.WifiUtil;

//==============================================================================================
public class MainActivity extends AppCompatActivity implements View.OnClickListener, Contract.View {

    //==============================================================================================
    private static final int REQUEST_CODE_LOC = 100;
    private static final String NO_CONNECT_DEVICE = "<unknown ssid>";
    private static final int REQUEST_CODE = 121;
    private Presenter presenter;
    private WifiManager wifi;
    private WifiUtil wu;
    private RxWifiP2pManager mRxWifiP2pManager;
    private FragmentManager fmTop;
    private FragmentManager fmBottom;
    private Fragment fragmentTop;
    private Fragment fragmentBottom;
    private int BtnClickId = 0;
    private int swStatusId = 0;
    private int sbData = 0;
    private boolean wifiIsConnected = false;
    private boolean timerTaskIsWorking = false;
    private String ip = "192.168.4.1";
    private int port = 9876;
    private String info = "";
    private String temperaturaData = "";
    private String humidityData = "";
    private String lightData = "";
    private String pwmLevel = "";
    private Toolbar toolbar;
    private static final int WRITE_DATA_TIME_OUT = 15;
    private static final int CONNECT_ITEM_CLICK = 1;
    private static final int DISCONNECT_ITEM_CLICK = 2;
    private static final int SEND_ITEM_CLICK = 3;
    private static final int SEACH_ITEM_CLICK = 4;
    //==============================================================================================
    int getSbData() {
        return sbData;
    }
    //==============================================================================================
    int getSWStatusId() {
        return swStatusId;
    }
    //==============================================================================================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.disConnectServer();
            presenter = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        setTimerTaskIsWorking(false);
        EventBus.getDefault().unregister(this);
    }

    //==============================================================================================
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessage(BtnMessage event) {
        int[] getData = {0, 0, 0};
        getData = event.getMessage();
        BtnClickId = getData[0];
        swStatusId = getData[1];
        sbData = getData[2];
        getStatusFromBottomFragment(BtnClickId);
        Log.i("my", "Read data from BottomFragment: BtnClickId = " + BtnClickId
                + " SwStatusId = " + swStatusId + " SbData = " + sbData);
    }
    //==============================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
    //============================================================================================
    //============================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
                case R.id.connect_item:
                    //Toast.makeText(this, "Connect", Toast.LENGTH_SHORT).show();
                    getStatusFromBottomFragment(CONNECT_ITEM_CLICK);
                    break;

                case R.id.disconnect_item:
                    //Toast.makeText(this, "Disconnect", Toast.LENGTH_SHORT).show();
                    getStatusFromBottomFragment(DISCONNECT_ITEM_CLICK);
                    break;

                case R.id.seach_item:
                    //Toast.makeText(this, "Seach", Toast.LENGTH_SHORT).show();
                    getStatusFromBottomFragment(SEACH_ITEM_CLICK);
                    break;
                case R.id.send_item:
                    //Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
                    getStatusFromBottomFragment(SEND_ITEM_CLICK);
                break;
        }
        return true;
    }

    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.wifi_app);

        fmTop = getSupportFragmentManager();
        fmBottom = getSupportFragmentManager();
        fragmentTop = fmTop.findFragmentById(R.id.containerTop);
        fragmentBottom = fmBottom.findFragmentById(R.id.containerBottom);
        if (fragmentTop == null) {
            fragmentTop = new TopFragment();
            fmTop.beginTransaction()
                    .add(R.id.containerTop, fragmentTop)
                    .commit();
        }
        if (fragmentBottom == null) {
            fragmentBottom = new BottomFragment();
            fmBottom.beginTransaction()
                    .add(R.id.containerBottom, fragmentBottom)
                    .commit();
        }
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wu = new WifiUtil(getApplicationContext(), wifi);
        RxConnectionHelper.setSettings(ip, port);
        presenter = new Presenter(this);
        EventBus.getDefault().register(this);
        //==============================================================================================
        mRxWifiP2pManager = new RxWifiP2pManager(
                getApplicationContext(),
                (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE),
                new WifiP2pBroadcastObservableManagerFactory(getApplicationContext()));
        //==============================================================================================
        accessLocationPermission();
    }

    //==============================================================================================
    public boolean getTimerTaskIsWorking() {
        return timerTaskIsWorking;
    }

    //==============================================================================================
    public void setTimerTaskIsWorking(boolean timerTaskIsWorking) {
        this.timerTaskIsWorking = timerTaskIsWorking;
    }

    //==============================================================================================
    private void accessLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int accessCoarseLocation = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFineLocation = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

            List<String> listRequestPermission = new ArrayList<String>();

            if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
                listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
                listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (!listRequestPermission.isEmpty()) {
                String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
                requestPermissions(strRequestPermission, REQUEST_CODE_LOC);
            }
        }
    }

    //==============================================================================================
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOC:
                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    //TODO - Add your code here to start Discovery
                }
                break;
            default:
                return;
        }
    }

    //==============================================================================================
    void makeToast(String data) {
        Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
    }

    //=============================================================================================
    Timer timer;
    TimerTask timerTask;
    static int countWriteData = 0;

    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if ((wifiIsConnected == true) && (presenter != null) && (wu.getSSID(getApplicationContext()).indexOf(NO_CONNECT_DEVICE) == -1)) {
                            JsonObject data = new JsonObject();
                            data.addProperty("request", 2020);
                            data.addProperty("pwmlevel", getSbData());
                            data.addProperty("switch", getSWStatusId());
                            presenter.sendData(data.toString().concat("\r\n"));
                            Log.i("my", "Main: Send data to board: " + data.toString() + "\r\n");
                        }
                        if ((countWriteData++) >= WRITE_DATA_TIME_OUT) {
                            countWriteData = 0;
                            makeToast(getString(R.string.no_response_from_device));
                            getStatusFromBottomFragment(2);
                        }
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    //==============================================================================================
    @Override
    public void onResult(String data) throws JSONException {
        if (!data.isEmpty()) {
            JSONObject jsonRoot = new JSONObject(data);
            temperaturaData = jsonRoot.getString("temperatura");
            humidityData = jsonRoot.getString("humidity");
            lightData = jsonRoot.getString("light");
            pwmLevel = jsonRoot.getString("pwm");
            if (fragmentTop != null) {
                EventBus.getDefault().postSticky(new TxtMessage(temperaturaData,
                        humidityData, lightData, Boolean.toString(wifiIsConnected)));
            }
            if (fragmentBottom != null) {
                EventBus.getDefault().postSticky(new PwmMessage(pwmLevel));
            }
            Log.i("my", "Main: Read data from board: " + data.toString() + "\r\n");
            data = "";
            countWriteData = 0;
        }

    }

    //==============================================================================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("my", "Main: requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (requestCode == REQUEST_CODE) {
            if (wifi != null) {
                info = wu.getSSID(getApplicationContext());
            }
            if (info != null) {
//                Toast toast = Toast.makeText(getApplicationContext(),
//                        info,
//                        Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//                toast.show();
                Log.i("my", "Main: Return connection device name: " + info);
            }
        }
    }

    //==============================================================================================
    @Override
    public void OnConnected() {
        //makeToast("Main: Client OnConnected");
        if (getTimerTaskIsWorking() == false) {
            setTimerTaskIsWorking(true);
            startTimer();
        }
        wifiIsConnected = true;
        if (fragmentTop != null) {
            EventBus.getDefault().postSticky(new TxtMessage(temperaturaData,
                    humidityData, lightData, "true"));
            Log.i("my", "OnConnected, wifiIsConnected = true ");
        }
    }

    //==============================================================================================
    @Override
    public void OnDisconnected() {
        //makeToast("Main: Client OnDisconnected");
        wifiIsConnected = false;
        if (fragmentTop != null) {
            EventBus.getDefault().postSticky(new TxtMessage(temperaturaData,
                    humidityData, lightData, "false"));

            Log.i("my", "OnDisconnected, wifiIsConnected = false ");
        }
        setTimerTaskIsWorking(false);
        presenter.presenterDispose();
    }

    //=========================================================================================
    void destroyTimer(){
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }
    //=========================================================================================
    public void getStatusFromBottomFragment(int id) {
        switch (id) {
            case 1:
                if ((wifiIsConnected == false) && (!info.contains(NO_CONNECT_DEVICE) && (presenter != null))) {
                    presenter = null;
                    destroyTimer();
                    presenter = new Presenter(this);
                    presenter.connectServer();
                }
                break;

            case 2:
                if (presenter != null) {
                    destroyTimer();
                    presenter.disConnectServer();
                    Log.i("my", "Main: Disconnect " + info);
                }
                break;
            case 3:
                if ((wifiIsConnected) && (presenter != null) && (getTimerTaskIsWorking() == false)) {
                    startTimer();
                }
                break;
            case 4:
                startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), REQUEST_CODE);
                Log.i("my", "Main: Find device");
                break;
        }
    }

    @Override
    public void onClick(View v) {

    }


    //==============================================================================================
}