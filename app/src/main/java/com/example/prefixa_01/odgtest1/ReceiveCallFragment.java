package com.example.prefixa_01.odgtest1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.humberto.pnwebrtc.PnPeerConnectionClient;
import com.example.prefixa_01.odgtest1.util.Constants;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

import org.json.JSONObject;

/**
 * Created by Prefixa_01 on 14/01/2016.
 */
public class ReceiveCallFragment extends Fragment {

    private String userName;
    private String callUser;
    private TextView nameTextView;
    private Client mClient;
    private Pubnub mPubNub;
    private Button callButton;
    private Button rejectButton;


    public static ReceiveCallFragment newInstance(String localUser, String callUser){
        Bundle args = new Bundle();
        args.putString(Constants.USER_NAME, localUser);
        args.putString(Constants.CALL_USER, callUser);

        ReceiveCallFragment fragment = new ReceiveCallFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        this.userName = getArguments().getString(Constants.USER_NAME);
        this.callUser = getArguments().getString(Constants.CALL_USER);
        //mClient = ClientLab.get(getActivity()).getClient(this.callUser);

        View v = inflater.inflate(R.layout.fragment_startcall,container,false);
        nameTextView = (TextView) v.findViewById(R.id.name);
        nameTextView.setText(this.callUser);

        this.mPubNub  = new Pubnub(Constants.PUB_KEY, Constants.SUB_KEY);
        this.mPubNub.setUUID(this.userName);

        callButton = (Button) v.findViewById(R.id.btn1);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Call button clicked", Toast.LENGTH_SHORT).show();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragment, CallFragment.newInstance(userName, callUser));
                ft.addToBackStack(null);
                ft.commit();

            }
        });

        rejectButton = (Button) v.findViewById(R.id.btn2);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return v;
    }


    /**
     * Publish a hangup command if rejecting call.
     * @param view
     */
    public void rejectCall(View view){
        JSONObject hangupMsg = PnPeerConnectionClient.generateHangupPacket(this.userName);
        this.mPubNub.publish(mClient.getmName(), hangupMsg, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.detach(getParentFragment());
                transaction.commit();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if(this.mPubNub!=null){
            this.mPubNub.unsubscribeAll();
        }
    }
}
