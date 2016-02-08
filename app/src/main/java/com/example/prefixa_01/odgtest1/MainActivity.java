package com.example.prefixa_01.odgtest1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.common.TwilioAccessManagerListener;
import com.twilio.conversations.AudioOutput;
import com.twilio.conversations.AudioTrack;
import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationException;
import com.twilio.conversations.ConversationListener;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaFactory;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.LocalVideoTrackFactory;
import com.twilio.conversations.MediaTrack;
import com.twilio.conversations.OutgoingInvite;
import com.twilio.conversations.Participant;
import com.twilio.conversations.ParticipantListener;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.VideoRendererObserver;
import com.twilio.conversations.VideoTrack;
import com.twilio.conversations.VideoViewRenderer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends FragmentActivity{
    private static final String TAG = MainActivity.class.getName();

    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    /*
    * Token used to Initialize the TwilioSDK
    */
    //private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTSzY4OTg1Y2E1MDE2YmFhN2VmZWExNjZkYTMyMzA1MTg5LTE0NTQwMTA2NjMiLCJpc3MiOiJTSzY4OTg1Y2E1MDE2YmFhN2VmZWExNjZkYTMyMzA1MTg5Iiwic3ViIjoiQUM4Zjk0MGE0M2Y5ZjdmNmFmYjNjMGIzYjhkNGMwNGE5NSIsIm5iZiI6MTQ1NDAxMDY2MywiZXhwIjoxNDU0MDE0MjYzLCJncmFudHMiOnsiaWRlbnRpdHkiOiJNYW51ZWxHb3RoYW0iLCJydGMiOnsiY29uZmlndXJhdGlvbl9wcm9maWxlX3NpZCI6IlZTOTZiNTFjY2UzMzYwNmEwMTI0ZTA1YzA2YjA5OTM3YmEifX19.AMD26UZdpkme41XkZagrsDLVptf7aJ6R1uLm9BalAnI";
    public static Identity UIdentity;
    /*
     * Twilio Conversations Client allows a client to create or participate in a conversation.
     */
    public static ConversationsClient conversationsClient;

    /*
     * A Conversation represents communication between the client and one or more participants.
     */
    public static Conversation conversation;

    /*
     * An OutgoingInvite represents an invitation to start or join a conversation with one or more participants
     */
    public static OutgoingInvite outgoingInvite;
    /*
     * A VideoViewRenderer receives frames from a local or remote video track and renders the frames to a provided view
     */

    public static IncomingInvite receivedInvite;
    private TwilioAccessManager accessManager;
    public static android.support.v4.app.FragmentManager fm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UIdentity = new Identity();
        /*
        * Get the Token and Identity of the user of the app
        */

        String json;
        try {
            json = new JSONTask().execute("http://192.168.0.25/video/token.php").get();
            //Toast.makeText(this, "json " + json, Toast.LENGTH_LONG).show();
            JSONObject myJSONO = new JSONObject(json);
            UIdentity.setUName(myJSONO.getString("identity"));
            UIdentity.setToken(myJSONO.getString("token"));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }



        /*
         * Initialize the Twilio Conversations SDK
         */
        initializeTwilioSdk();

        /*
        *initialize a fragment manager and push the MainActivity fragment onto it
        */
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        MainActivityFragment myFragment = new MainActivityFragment();
        ft.add(R.id.fragment, myFragment);
        ft.commit();

         /*
         * Check camera and microphone permissions. Needed in Android M.
         */
        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraAndMicrophone();
        }

        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);




    }

    @Override
    public void onStart(){
        super.onStart();

    }

    private void initializeTwilioSdk(){
        TwilioConversations.setLogLevel(TwilioConversations.LogLevel.DEBUG);

        if(!TwilioConversations.isInitialized()) {
            TwilioConversations.initialize(getApplicationContext(), new TwilioConversations.InitListener() {
                @Override
                public void onInitialized() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /*
                             * Now that the SDK is initialized we create a ConversationsClient and register for incoming calls.
                             */

                            // The TwilioAccessManager manages the lifetime of the access token and notifies the client of token expirations.
                            accessManager =
                                    TwilioAccessManagerFactory.createAccessManager(UIdentity.getToken(), accessManagerListener());
                            conversationsClient =
                                    TwilioConversations.createConversationsClient(accessManager, conversationsClientListener());
                            // Specify the audio output to use for this conversation client
                            conversationsClient.setAudioOutput(AudioOutput.HEADSET);
                            // Initialize the camera capturer and start the camera preview

                            // Register to receive incoming invites
                            conversationsClient.listen();

                        }
                    });


                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(MainActivity.this,
                            "Failed to initialize the Twilio Conversations SDK",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private ConversationsClientListener conversationsClientListener() {
        return new ConversationsClientListener() {
            @Override
            public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                //conversationStatusTextView.setText("onStartListeningForInvites");
            }

            @Override
            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
               // conversationStatusTextView.setText("onStopListeningForInvites");
            }

            @Override
            public void onFailedToStartListening(ConversationsClient conversationsClient, ConversationException e) {
               // conversationStatusTextView.setText("onFailedToStartListening");
            }

            @Override
            public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
               //conversationStatusTextView.setText("onIncomingInvite");
                if (conversation == null) {
                    receivedInvite = incomingInvite;
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment, ReceiveCallFragment.newInstance());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    //showInviteDialog(incomingInvite);
                } else {
                    Log.w(TAG, String.format("Conversation in progress. Invite from %s ignored", incomingInvite.getInvitee()));
                }
            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                //conversationStatusTextView.setText("onIncomingInviteCancelled");
            }
        };
    }

    private TwilioAccessManagerListener accessManagerListener() {
        return new TwilioAccessManagerListener() {
            @Override
            public void onAccessManagerTokenExpire(TwilioAccessManager twilioAccessManager) {
                //conversationStatusTextView.setText("onAccessManagerTokenExpire");

            }

            @Override
            public void onTokenUpdated(TwilioAccessManager twilioAccessManager) {
                //conversationStatusTextView.setText("onTokenUpdated");

            }

            @Override
            public void onError(TwilioAccessManager twilioAccessManager, String s) {
                //conversationStatusTextView.setText("onError");
            }
        };
    }

    public static ConversationListener conversationListener() {
        return new ConversationListener() {
            @Override
            public void onParticipantConnected(Conversation conversation, Participant participant) {
                //conversationStatusTextView.setText("onParticipantConnected " + participant.getIdentity());

                participant.setParticipantListener(participantListener());
            }

            @Override
            public void onFailedToConnectParticipant(Conversation conversation, Participant participant, ConversationException e) {
                Log.e(TAG, e.getMessage());
                //conversationStatusTextView.setText("onFailedToConnectParticipant " + participant.getIdentity());
            }

            @Override
            public void onParticipantDisconnected(Conversation conversation, Participant participant) {
                //conversationStatusTextView.setText("onParticipantDisconnected " + participant.getIdentity());
            }

            @Override
            public void onConversationEnded(Conversation conversation, ConversationException e) {
                //conversationStatusTextView.setText("onConversationEnded");
                reset();
            }
        };
    }

    public static ParticipantListener participantListener() {
        return new ParticipantListener() {
            @Override
            public void onVideoTrackAdded(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                Log.i(TAG, "onVideoTrackAdded " + participant.getIdentity());


            }

            @Override
            public void onVideoTrackRemoved(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                Log.i(TAG, "onVideoTrackRemoved " + participant.getIdentity());
                // conversationStatusTextView.setText("onVideoTrackRemoved " + participant.getIdentity());
                //participantContainer.removeAllViews();

            }

            @Override
            public void onAudioTrackAdded(Conversation conversation, Participant participant, AudioTrack audioTrack) {
                Log.i(TAG, "onAudioTrackAdded " + participant.getIdentity());
            }

            @Override
            public void onAudioTrackRemoved(Conversation conversation, Participant participant, AudioTrack audioTrack) {
                Log.i(TAG, "onAudioTrackRemoved " + participant.getIdentity());
            }

            @Override
            public void onTrackEnabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
                Log.i(TAG, "onTrackEnabled " + participant.getIdentity());
            }

            @Override
            public void onTrackDisabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
                Log.i(TAG, "onTrackDisabled " + participant.getIdentity());
            }
        };
    }

    /*
     ********************************************************************* Helper methods
     */

    public static void reset(){

        if(conversation != null) {
            conversation.dispose();
            conversation = null;
        }
        outgoingInvite = null;


        int numberOfStates ;
        for (numberOfStates=fm.getBackStackEntryCount(); numberOfStates>0;numberOfStates--){
            fm.popBackStack();
        }
    }

    private boolean checkPermissionForCameraAndMicrophone(){
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if ((resultCamera == PackageManager.PERMISSION_GRANTED) &&
                (resultMic == PackageManager.PERMISSION_GRANTED)){
            return true;
        } else {
            return false;
        }
    }

    private void requestPermissionForCameraAndMicrophone(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)){
            Toast.makeText(this,
                    "Camera and Microphone permissions needed. Please allow in App Settings for additional functionality.",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_MIC_PERMISSION_REQUEST_CODE);
        }
    }


}
