package rxsocket.rxsocket.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rxsocket.rxsocket.R;
import rxsocket.rxsocket.model.BtnMessage;
import rxsocket.rxsocket.model.PwmMessage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BottomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BottomFragment extends Fragment  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Button connectBtn;
    Button disconnectBtn;
    Button sendBtn;
    Button findBtn;
    Switch swControlPwm;
    SeekBar sbSetPwm;
    TextView twLblSwControlPwm;
    private int BtnClickIdFragment = 0;
    private int swStatusIdFragment = 0;
    private int SbDataFragment = 0;
    public BottomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BottomFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BottomFragment newInstance(String param1, String param2) {
        BottomFragment fragment = new BottomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public interface FragmentCallbacks {
        /*Callback for when an item has been selected. */
        public void onButtonClickedInFragment(int id);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onMessage(PwmMessage event){
       int pwmLevel = Integer.parseInt(event.getPwmMessage());
       if(swControlPwm.isChecked()==false){
           sbSetPwm.setProgress(pwmLevel);
           twLblSwControlPwm.setText(String.valueOf(pwmLevel) + " %");
       }

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
    private FragmentCallbacks FragmentmCallbacks;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bottom, container, false);
        Button connectBtn = (Button)view.findViewById(R.id.connect_btn);
        Button disconnectBtn = (Button)view.findViewById(R.id.disconnect_btn);
        Button sendBtn = (Button)view.findViewById(R.id.send_btn);
        Button findBtn = (Button)view.findViewById(R.id.find_btn);
        swControlPwm = (Switch)view.findViewById(R.id.sw_control_pwm);
        sbSetPwm = (SeekBar)view.findViewById(R.id.sb_set_pwm);
        twLblSwControlPwm = (TextView)view.findViewById(R.id.tw_lbl_sw_control_pwm);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sbSetPwm.setMin(0);
        }
        sbSetPwm.setMax(100);
        twLblSwControlPwm.setText("0 %");
        swControlPwm.setChecked(false);
        swControlPwm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(swControlPwm.isChecked()){
                  swStatusIdFragment = 1;
              }else{
                  swStatusIdFragment = 0;
              }
                SbDataFragment =  sbSetPwm.getProgress();
              EventBus.getDefault().postSticky(new BtnMessage(0, swStatusIdFragment,SbDataFragment ));
            }
        });
        sbSetPwm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //if((fromUser==true)&&(swControlPwm.isChecked()==true))
                {
                    twLblSwControlPwm.setText(String.valueOf(progress) + " %");
                    SbDataFragment = progress;
                    EventBus.getDefault().postSticky(new BtnMessage(0, swStatusIdFragment, progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        connectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new BtnMessage(1, swStatusIdFragment, SbDataFragment));
            }
        });
        disconnectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new BtnMessage(2, swStatusIdFragment, SbDataFragment));
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new BtnMessage(3, swStatusIdFragment, SbDataFragment));
            }
        });
        findBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new BtnMessage(4, swStatusIdFragment, SbDataFragment));
            }
        });
        return view;
    }
}