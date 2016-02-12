package com.example.prefixa_01.odgtest1;

import android.graphics.Color;
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

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
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

    /**
     * used to open a websocket
     */
    private WebSocketClient mWebSocketClient;

    private CanvasView customCanvas;

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

        customCanvas = (CanvasView) view.findViewById(R.id.canvas_view);

        mButtonEnd = (Button) view.findViewById(R.id.btn_end_call);
        //mButtonHold = (Button) view.findViewById(R.id.btn_hold_call);
        //mButtonHold.setText("Hold");
        mButtonEnd.setText(R.string.end_button_text);
        mButtonEnd.setTextColor(Color.parseColor("#FFFFFF"));
        mCallCallerIDTextView = (TextView)view.findViewById(R.id.call_fragment_caller_id_text_view);
        mCallNameTextView = (TextView)view.findViewById(R.id.call_fragment_name_text_view);

        mButtonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.conversation.disconnect();
                MainActivity.reset();
            }
        });

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

        /**
         * Connect to the websocket in port 8081
         */
        connectWebSocket();

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

    /**
     * method to connect websocket in order to draw a circle
     */
    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://177.227.224.219:8081");


        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mWebSocketClient = new WebSocketClient(uri) {

            JSONObject json;
            String msg;
            JSONObject data;
            JSONObject color;
            JSONArray points;
            double point1;
            double point2;
            double point3;
            double point4;
            double a;
            double b;

            Circle circle = Circle.get(getActivity());

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                //mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                //Log.i("Websocket", "JSON " + s);
                try {
                    json = new JSONObject(s);
                    msg = json.getString("msg");

                    if(msg.equals("drawCircle")){

                        Log.i("Websocket", "drawCircle ");
                        data = json.getJSONObject("data");
                        points = data.getJSONArray("points");

                        point1 = points.getDouble(0);
                        point2 = points.getDouble(1);
                        point3 = points.getDouble(2);
                        point4 = points.getDouble(3);
                        a = point1 - point3;
                        b = point2 - point4;

                        //Log.i("Websocket", color.toString());
                        //Log.i("Websocket", "drawCircle 2");

                        circle.setX(point1);
                        circle.setY(point2);
                        circle.setRadius(Math.sqrt((a*a)+(b*b)));

                    }
                    else if(msg.equals("initCommands")){
                        //Log.i("Websocket", "initCommands " + msg);
                        //TODO Init Commands
                        JSONArray commands = json.getJSONArray("data");
                        //Log.i("Websocket", "commands " + commands);
                    }
                    else if(msg.equals("clear")){
                        Log.i("Websocket", "clear ");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                customCanvas.invalidate();
                            }
                        });

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

}
