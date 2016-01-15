package com.example.prefixa_01.odgtest1;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    //Button mTransparentButton;

    public MainActivityFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transparent, container, false);
        //mTransparentButton =(Button) v.findViewById(R.id.transparent_textview);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new ContactListFragment();
                FragmentTransaction transaction  = getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                Toast.makeText(getActivity(),"click on button transparent", Toast.LENGTH_SHORT).show();
            }
        });
        return v;

    }
}
