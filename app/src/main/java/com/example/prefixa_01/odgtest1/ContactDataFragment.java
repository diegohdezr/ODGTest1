package com.example.prefixa_01.odgtest1;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.twilio.conversations.OutgoingInvite;

import java.util.UUID;

/**
 * Created by Prefixa_01 on 14/01/2016.
 */
public class ContactDataFragment extends Fragment {
    private static final String ARG_CLIENT_ID="client_id";


    /*
 * An OutgoingInvite represents an invitation to start or join a conversation with one or more participants
 */
    private OutgoingInvite outgoingInvite;

    private Client mClient;
    private TextView mNameTextview;
    private TextView mCallerIDTextView;
    private Button mBtn1;
    private Button mBtn2;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //mCrime = new Crime();
        //UUID crimeId = (UUID) getActivity().getIntent().getSerializableExtra(CrimeActivity.EXTRA_CRIME_ID);
        UUID clientId = (UUID) getArguments().getSerializable(ARG_CLIENT_ID);
        mClient = ClientLab.get(getActivity()).getClient(clientId);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_startcall,container,false);
        mBtn1 = (Button) v.findViewById(R.id.btn1);
        mBtn2 = (Button) v.findViewById(R.id.btn2);
        mNameTextview = (TextView) v.findViewById(R.id.name);
        mCallerIDTextView = (TextView) v.findViewById(R.id.number);
        mNameTextview.setText(mClient.getmName());
        mCallerIDTextView.setText(mClient.getmClientID());
        mBtn1.setText("Call");
        mBtn2.setText("Back");
        mBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment, CallFragment.newInstance(mClient.getmID()));
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        mBtn1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setBackgroundColor(Color.rgb(255, 165, 0));
                } else {
                    v.setBackgroundColor(Color.rgb(255, 255, 255));
                }
            }
        });


        return v;
    }


    public static ContactDataFragment newInstance(UUID clientId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLIENT_ID, clientId);
        ContactDataFragment fragment = new ContactDataFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
