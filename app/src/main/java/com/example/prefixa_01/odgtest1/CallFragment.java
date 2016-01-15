package com.example.prefixa_01.odgtest1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.UUID;

/**
 * Created by Prefixa_01 on 14/01/2016.
 */
public class CallFragment extends Fragment {
    private static final String ARG_CLIENT_ID="client_id";

    private Client mClient;
    private TextView mCallNameTextView;
    private TextView mCallCallerIDTextView;
    private Button mButtonEnd;
    private Button mButtonHold;


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
        View view = inflater.inflate(R.layout.fragment_call,container,false);
        mButtonEnd = (Button) view.findViewById(R.id.btn_end_call);
        mButtonHold = (Button) view.findViewById(R.id.btn_hold_call);
        mButtonHold.setText("Hold");
        mButtonEnd.setText("End");
        mCallCallerIDTextView = (TextView)view.findViewById(R.id.call_fragment_caller_id_text_view);
        mCallNameTextView = (TextView)view.findViewById(R.id.call_fragment_name_text_view);
        mCallCallerIDTextView.setText(mClient.getmClientID());
        mCallNameTextView.setText(mClient.getmName());



        return view;
    }

    public static CallFragment newInstance(UUID clientId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLIENT_ID, clientId);
        CallFragment fragment = new CallFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
