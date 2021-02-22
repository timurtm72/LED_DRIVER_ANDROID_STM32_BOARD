package rxsocket.rxsocket.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rxsocket.rxsocket.R;
import rxsocket.rxsocket.model.TxtMessage;
import rxsocket.rxsocket.model.OnReceive;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3= "param3";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mParam3;
    TextView humidity;
    TextView temperatura;
    TextView light;
    ImageView ivConnectStatus;
    CardView cardViewTop;
    public TopFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
     }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }
    public  void setRecievData(float _temperatura, float _humidity, float _light){
        temperatura.setText(String.format("%2.1f %s",_temperatura," C"));
        humidity.setText(String.format("%2.1f %s",_humidity," %"));
        light.setText(String.format("%2.1f %s",_light," L"));
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TopFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TopFragment newInstance(String param1, String param2, String param3) {
        TopFragment fragment = new TopFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onMessage(TxtMessage event){
        String[] getStr;
        getStr = event.getMessage();
        setRecievData(Float.valueOf(getStr[0]),Float.valueOf(getStr[1]),Float.valueOf(getStr[2]));
        if(getStr[3].equalsIgnoreCase("true")){
            ivConnectStatus.setImageResource(R.drawable.wifi_green);
        }
        if(getStr[3].equalsIgnoreCase("false")){
            ivConnectStatus.setImageResource(R.drawable.wifi_red);
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
         }

    }
    boolean isCreatedFragment = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top, container, false);
        humidity = (TextView) view.findViewById(R.id.tvHumidity);
        temperatura = (TextView) view.findViewById(R.id.tvTemperatura);
        light = (TextView) view.findViewById(R.id.tvLighte);
        ivConnectStatus = (ImageView)view.findViewById(R.id.ivConnectStatus);
        isCreatedFragment = true;
        // Inflate the layout for this fragment
        ivConnectStatus.setImageResource(R.drawable.wifi_red);
        setRecievData(0,0,0);
        return view;
    }
}