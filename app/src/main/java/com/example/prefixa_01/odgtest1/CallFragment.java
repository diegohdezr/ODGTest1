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
import android.widget.Toast;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.ConversationException;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaFactory;
import com.twilio.conversations.LocalMediaListener;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.LocalVideoTrackFactory;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.VideoViewRenderer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Prefixa_01 on 14/01/2016.
 */
public class CallFragment extends Fragment {
    private static final String ARG_CLIENT_ID="client_id";
    private static final String ARG_IS_CALLING="isCalling";
    private static final String TAG = MainActivity.class.getName();

    /*
 * A VideoViewRenderer receives frames from a local or remote video track and renders the frames to a provided view
 */
    //private VideoViewRenderer participantVideoRenderer;
    private VideoViewRenderer localVideoRenderer;

    private CameraCapturer cameraCapturer;
    private Client mClient;
    private TextView mCallNameTextView;
    private TextView mCallCallerIDTextView;
    private Button mButtonEnd;
    private Button mButtonHold;
    LocalMedia localMedia;
    //private FrameLayout previewFrameLayout;
    private ViewGroup localContainer;
    boolean isCalling;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //mCrime = new Crime();
        //UUID crimeId = (UUID) getActivity().getIntent().getSerializableExtra(CrimeActivity.EXTRA_CRIME_ID);

        isCalling = getArguments().getBoolean(ARG_IS_CALLING);





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


        localContainer = (ViewGroup)view.findViewById(R.id.localContainer);
        //previewFrameLayout = (FrameLayout) view.findViewById(R.id.previewFrameLayout);

        // Initialize the camera capturer and start the camera preview
        cameraCapturer = CameraCapturerFactory.createCameraCapturer(getActivity(),
                CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA,
                localContainer,
                capturerErrorListener());
        startPreview();


        if (isCalling){//outgoing call
            UUID clientId = (UUID) getArguments().getSerializable(ARG_CLIENT_ID);
            mClient = ClientLab.get(getActivity()).getClient(clientId);
            String participant = mClient.getmName();
            mCallNameTextView.setText(mClient.getmName());
            mCallCallerIDTextView.setText(mClient.getmClientID());
            if (!participant.isEmpty() && (MainActivity.conversationsClient != null)) {
                stopPreview();
                // Create participants set (we support only one in this example)
                Set<String> participants = new HashSet<>();
                participants.add(participant);
                // Create local media
                LocalMedia localMedia = setupLocalMedia();

                // Create outgoing invite
                MainActivity.outgoingInvite = MainActivity.conversationsClient.sendConversationInvite(participants,
                        localMedia, new ConversationCallback() {
                            @Override
                            public void onConversation(Conversation conversation, ConversationException e) {
                                if (e == null) {
                                    // Participant has accepted invite, we are in active conversation
                                    MainActivity.conversation = conversation;
                                    conversation.setConversationListener(MainActivity.conversationListener());
                                } else {
                                    Toast.makeText(getActivity(),"Call Failed",Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, e.getMessage());
                                    hangup();
                                    MainActivity.reset();
                                }
                            }
                        });

            } else {
                Toast.makeText(getActivity(),"Invalid Participant",Toast.LENGTH_SHORT).show();
                Log.e(TAG, "invalid participant call");
                //conversationStatusTextView.setText("call participant failed");
            }
        }else {//incoming call
            localMedia = setupLocalMedia();
            mCallCallerIDTextView.setText(MainActivity.receivedInvite.getInvitee().toString());
            MainActivity.receivedInvite.accept(localMedia, new ConversationCallback() {
                @Override
                public void onConversation(Conversation conversation, ConversationException e) {
                    Log.e(TAG, "sendConversationInvite onConversation");
                    if (e == null) {
                        MainActivity.conversation = conversation;
                        conversation.setConversationListener(MainActivity.conversationListener());

                    } else {
                        Log.e(TAG, e.getMessage());
                        hangup();
                        MainActivity.reset();
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

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

    private void hangup() {
        if(MainActivity.conversation != null) {
            MainActivity.conversation.disconnect();
        } else if(MainActivity.outgoingInvite != null){
            MainActivity.outgoingInvite.cancel();
        }

        MainActivity.reset();
    }


    private LocalMedia setupLocalMedia() {
        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());
        LocalVideoTrack localVideoTrack = LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
        localMedia.addLocalVideoTrack(localVideoTrack);
        return localMedia;
    }

    private LocalMediaListener localMediaListener(){
        return new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(Conversation conversation, LocalVideoTrack localVideoTrack) {
                //conversationStatusTextView.setText("onLocalVideoTrackAdded");
                localVideoRenderer = new VideoViewRenderer(getActivity(), localContainer);

                localVideoTrack.addRenderer(localVideoRenderer);

            }

            @Override
            public void onLocalVideoTrackRemoved(Conversation conversation, LocalVideoTrack localVideoTrack) {
                //conversationStatusTextView.setText("onLocalVideoTrackRemoved");
                localContainer.removeAllViews();
            }
        };
    }

    public static CallFragment newInstance(UUID clientId, boolean isCalling){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLIENT_ID, clientId);
        args.putBoolean(ARG_IS_CALLING,isCalling);
        CallFragment fragment = new CallFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public static Fragment newInstance(boolean isCalling) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_CALLING,isCalling);
        CallFragment fragment = new CallFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
