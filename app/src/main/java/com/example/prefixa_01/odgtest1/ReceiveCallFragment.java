package com.example.prefixa_01.odgtest1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.twilio.conversations.IncomingInvite;

/**
 * Created by Prefixa_01 on 14/01/2016.
 */
public class ReceiveCallFragment extends Fragment {
    private Button acceptBtn;
    private Button rejectBtn;
    private TextView nameTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_startcall, container, false);
        acceptBtn =(Button) view.findViewById(R.id.btn1);
        rejectBtn = (Button) view.findViewById(R.id.btn2);
        acceptBtn.setText("Accept");
        rejectBtn.setText("Reject");
        nameTextView = (TextView) view.findViewById(R.id.name);
        nameTextView.setText(MainActivity.receivedInvite.getInvitee().toString());


        return view;

    }

    public static Fragment newInstance() {
        Bundle args = new Bundle();
        ReceiveCallFragment fragment = new ReceiveCallFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
