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
import android.widget.FrameLayout;
import android.widget.Toast;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.common.TwilioAccessManagerListener;
import com.twilio.conversations.AudioOutput;
import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationException;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaFactory;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.LocalVideoTrackFactory;
import com.twilio.conversations.OutgoingInvite;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.VideoViewRenderer;

public class MainActivity extends FragmentActivity{
    private static final String TAG = MainActivity.class.getName();

    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;

    private static final String ACCESS_TOKEN = "TWILIO_ACCESS_TOKEN";

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
    private OutgoingInvite outgoingInvite;
    /*
     * A VideoViewRenderer receives frames from a local or remote video track and renders the frames to a provided view
     */

    private TwilioAccessManager accessManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        /*
         * Initialize the Twilio Conversations SDK
         */
        initializeTwilioSdk();

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        MainActivityFragment myFragment = new MainActivityFragment();
        ft.add(R.id.fragment,myFragment);
        ft.commit();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                                    TwilioAccessManagerFactory.createAccessManager(ACCESS_TOKEN, accessManagerListener());
                            conversationsClient =
                                    TwilioConversations.createConversationsClient(accessManager, conversationsClientListener());
                            // Specify the audio output to use for this conversation client
                            conversationsClient.setAudioOutput(AudioOutput.SPEAKERPHONE);
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
               /*conversationStatusTextView.setText("onIncomingInvite");
                if (conversation == null) {
                    showInviteDialog(incomingInvite);
                } else {
                    Log.w(TAG, String.format("Conversation in progress. Invite from %s ignored", incomingInvite.getInvitee()));
                }*/
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
    /*
     * Helper methods
     */



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
