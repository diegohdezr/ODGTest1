package com.example.prefixa_01.odgtest1;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.humberto.pnwebrtc.PnRTCClient;
import com.example.prefixa_01.odgtest1.util.Constants;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.UUID;

/**
 * Created by Prefixa_01 on 14/01/2016.
 */
public class CallFragment extends Fragment {

    //private static final String ARG_CLIENT_ID="client_id";

    private Client mClient;
    private TextView mCallNameTextView;
    private TextView mCallCallerIDTextView;
    private Button mButtonEnd;
    private Button mButtonHold;

    public static final String VIDEO_TRACK_ID = "videoPN";
    public static final String AUDIO_TRACK_ID = "audioPN";
    public static final String LOCAL_MEDIA_STREAM_ID = "localStreamPN";

    private PnRTCClient pnRTCClient;
    private VideoSource localVideoSource;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private GLSurfaceView videoView;
    private boolean backPressed = false;
    private Thread  backPressedThread = null;

    private String username;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //mCrime = new Crime();
        //UUID crimeId = (UUID) getActivity().getIntent().getSerializableExtra(CrimeActivity.EXTRA_CRIME_ID);
        username = getArguments().getString(Constants.USER_NAME);
        UUID clientId = (UUID) getArguments().getSerializable(Constants.CALL_USER);
        mClient = ClientLab.get(getActivity()).getClient(clientId);

        PeerConnectionFactory.initializeAndroidGlobals(
                this,  // Context
                true,  // Audio Enabled
                true,  // Video Enabled
                true,  // Hardware Acceleration Enabled
                null); // Render EGL Context

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

        PeerConnectionFactory pcFactory = new PeerConnectionFactory();
        this.pnRTCClient = new PnRTCClient(Constants.PUB_KEY, Constants.SUB_KEY, this.username);

        String backFacingCam = VideoCapturerAndroid.getNameOfBackFacingDevice();

        // Creates a VideoCapturerAndroid instance for the device name
        VideoCapturer capturer = VideoCapturerAndroid.create(backFacingCam);

        // First create a Video Source, then we can make a Video Track
        localVideoSource = pcFactory.createVideoSource(capturer, this.pnRTCClient.videoConstraints());
        VideoTrack localVideoTrack = pcFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);

        // First we create an AudioSource then we can create our AudioTrack
        AudioSource audioSource = pcFactory.createAudioSource(this.pnRTCClient.audioConstraints());
        AudioTrack localAudioTrack = pcFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);

        // To create our VideoRenderer, we can use the included VideoRendererGui for simplicity
        // First we need to set the GLSurfaceView that it should render to
        this.videoView = (GLSurfaceView) view.findViewById(R.id.gl_surface);

        // Then we set that view, and pass a Runnable to run once the surface is ready
        VideoRendererGui.setView(videoView, null);

        // Now that VideoRendererGui is ready, we can get our VideoRenderer.
        // IN THIS ORDER. Effects which is on top or bottom
        remoteRender = VideoRendererGui.create(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
        localRender = VideoRendererGui.create(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);

        // We start out with an empty MediaStream object, created with help from our PeerConnectionFactory
        //  Note that LOCAL_MEDIA_STREAM_ID can be any string
        MediaStream mediaStream = pcFactory.createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);

        // Now we can add our tracks.
        mediaStream.addTrack(localVideoTrack);
        mediaStream.addTrack(localAudioTrack);

        // First attach the RTC Listener so that callback events will be triggered
        this.pnRTCClient.attachRTCListener(null);

        // Then attach your local media stream to the PnRTCClient.
        //  This will trigger the onLocalStream callback.
        this.pnRTCClient.attachLocalMediaStream(mediaStream);

        // Listen on a channel. This is your "phone number," also set the max chat users.
        this.pnRTCClient.listenOn("Kevin");
        this.pnRTCClient.setMaxConnections(1);

        // If the intent contains a number to dial, call it now that you are connected.
        //  Else, remain listening for a call.
        connectToUser(mClient.getmName());



        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.videoView.onPause();
        this.localVideoSource.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.videoView.onResume();
        this.localVideoSource.restart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.localVideoSource != null) {
            this.localVideoSource.stop();
        }
        if (this.pnRTCClient != null) {
            this.pnRTCClient.onDestroy();
        }
    }

    public void connectToUser(String user) {
        this.pnRTCClient.connect(user);
    }

    public void hangup(View view) {
        this.pnRTCClient.closeAllConnections();
        endCall();
    }

    private void endCall() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.detach(getParentFragment());
        transaction.attach(new MainActivityFragment());
        transaction.commit();
    }

    public static CallFragment newInstance(String localUser, String callUser){
        Bundle args = new Bundle();
        args.putString(Constants.USER_NAME, localUser);
        args.putString(Constants.CALL_USER, callUser);
        CallFragment fragment = new CallFragment();
        fragment.setArguments(args);
        return fragment;
    }



}
