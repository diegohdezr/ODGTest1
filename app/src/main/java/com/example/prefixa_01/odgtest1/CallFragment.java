package com.example.prefixa_01.odgtest1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaFactory;
import com.twilio.conversations.LocalMediaListener;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.LocalVideoTrackFactory;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.VideoViewRenderer;

import java.util.UUID;

/**
 * Created by Prefixa_01 on 14/01/2016.
 */
public class CallFragment extends Fragment {
    private static final String ARG_CLIENT_ID="client_id";
    private static final String TAG = MainActivity.class.getName();

    /*
 * A VideoViewRenderer receives frames from a local or remote video track and renders the frames to a provided view
 */
    private VideoViewRenderer participantVideoRenderer;
    private VideoViewRenderer localVideoRenderer;

    private CameraCapturer cameraCapturer;
    private Client mClient;
    private TextView mCallNameTextView;
    private TextView mCallCallerIDTextView;
    private Button mButtonEnd;
    private Button mButtonHold;
    private FrameLayout previewFrameLayout;
    private ViewGroup localContainer;

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
        localContainer = (ViewGroup)view.findViewById(R.id.localContainer);
        previewFrameLayout = (FrameLayout) view.findViewById(R.id.previewFrameLayout);

        // Initialize the camera capturer and start the camera preview
        cameraCapturer = CameraCapturerFactory.createCameraCapturer(getActivity(),
                CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA,
                previewFrameLayout,
                capturerErrorListener());
        startPreview();


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (participantVideoRenderer != null) {
            participantVideoRenderer.onResume();
        }

        if (localVideoRenderer != null) {
            localVideoRenderer.onResume();
        }

        if (TwilioConversations.isInitialized() &&
                MainActivity.conversationsClient != null &&
                !MainActivity.conversationsClient.isListening()) {
            MainActivity.conversationsClient.listen();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(participantVideoRenderer != null) {
            participantVideoRenderer.onPause();
        }
        if (localVideoRenderer != null) {
            localVideoRenderer.onPause();
        }
        if (TwilioConversations.isInitialized() &&
                MainActivity.conversationsClient != null  &&
                MainActivity.conversationsClient.isListening()) {
            MainActivity.conversationsClient.unlisten();
        }
    }

    private CapturerErrorListener capturerErrorListener() {
        return new CapturerErrorListener() {
            @Override
            public void onError(CapturerException e) {
                Log.e(TAG, "Camera capturer error:" + e.getMessage());
            }
        };
    }
    private void startPreview() {
        cameraCapturer.startPreview();
    }

    private void stopPreview() {
        if(cameraCapturer != null && cameraCapturer.isPreviewing()) {
            cameraCapturer.stopPreview();
        }
    }

    public static CallFragment newInstance(UUID clientId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLIENT_ID, clientId);
        CallFragment fragment = new CallFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private LocalMedia setupLocalMedia() {
        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());
        LocalVideoTrack localVideoTrack = LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);

        return localMedia;
    }

    private LocalMediaListener localMediaListener(){
        return new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(Conversation conversation, LocalVideoTrack localVideoTrack) {
                //conversationStatusTextView.setText("onLocalVideoTrackAdded");
                localVideoRenderer = new VideoViewRenderer(getActivity(), localContainer);
                localVideoTrack.addRenderer(localVideoRenderer);
                //myfragment.setrenderer(localVideoRenderer);
            }

            @Override
            public void onLocalVideoTrackRemoved(Conversation conversation, LocalVideoTrack localVideoTrack) {
                //conversationStatusTextView.setText("onLocalVideoTrackRemoved");
                localContainer.removeAllViews();
            }
        };
    }


}
