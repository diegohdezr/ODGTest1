package com.example.prefixa_01.odgtest1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.common.TwilioAccessManagerListener;
import com.twilio.conversations.AudioOutput;
import com.twilio.conversations.AudioTrack;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationException;
import com.twilio.conversations.ConversationListener;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.MediaTrack;
import com.twilio.conversations.OutgoingInvite;
import com.twilio.conversations.Participant;
import com.twilio.conversations.ParticipantListener;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.VideoTrack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends FragmentActivity{
    private static final String TAG = "ODGTest";

    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    /*
    * object with the Token used to Initialize the TwilioSDK
    */
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
    public static IncomingInvite receivedInvite;
    private TwilioAccessManager accessManager;
    public static android.support.v4.app.FragmentManager fm;
    HttpURLConnection connection = null;
    BufferedReader reader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        * Get the Token and Identity of the user of the app
        */
        UIdentity = new Identity();
        getToken();

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

        if (connection!= null){
            connection.disconnect();
        }
        /*
        *initialize a fragment manager and push the MainActivity fragment onto it
        */
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        MainActivityFragment myFragment = new MainActivityFragment();
        ft.add(R.id.fragment, myFragment);
        ft.commit();


    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
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

    private void getToken() {
        try {
            URL url = new URL("https://f265df59.ngrok.io/token");
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine())!=null){
                buffer.append(line);
            }
            String jsonString = buffer.toString();
            JSONObject myJSONO = new JSONObject(jsonString);
            UIdentity.setUName(myJSONO.getString("identity"));
            UIdentity.setToken(myJSONO.getString("token"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG,"error en la URL");
        } catch (IOException e) {
            Log.e(TAG,"error al leer el string de JSON");
            e.printStackTrace();
        }catch (JSONException je){
            Log.e(TAG,"error al parsear el JSON");
            je.printStackTrace();
        }finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
