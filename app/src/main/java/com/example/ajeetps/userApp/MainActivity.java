package com.example.ajeetps.userApp;

import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import android.accounts.Account;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.Message;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends CommonUtil implements
        OnClickListener,
        ConnectionCallbacks,
        OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener {

    private static final int RC_SIGN_IN = 0;
    // Logcat tag
    private static final String TAG = "Open Sesame";
    private static final long TIMEOUT_DISCOVER = 1000L * 60L;

    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;

    private SignInButton btnSignIn;
    private Button btnSignOut, btnRevokeAccess, btnnearByClient;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail;
    private LinearLayout llProfileLayout;
    private NearbyApiManager nearbyApiManager;
    private String mOtherEndpointId;
    private String mDoorId;
    private String mDoorKey;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
        btnnearByClient = (Button) findViewById(R.id.button_client);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        llProfileLayout = (LinearLayout) findViewById(R.id.llProfile);

        // Button click listeners
        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);
        btnnearByClient.setOnClickListener(this);

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this).addApi(Plus.API)
//                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addApi(Nearby.CONNECTIONS_API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.PLUS_LOGIN))
                .addScope(new Scope(Scopes.PLUS_ME))
                .build();
        nearbyApiManager = new NearbyApiManager(this, new BeaconVisibilityListener());

        enableDeviceAdmin(this);
    }

    /**
     * This is responsible for enabling the device adminstrator.
     */
    private void enableDeviceAdmin(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager)
            context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!devicePolicyManager.isAdminActive(DeviceAdminReceiver.getComponentName(this))) {
            Intent intent = new Intent(this, ActivateDeviceAdminActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onConnectionRequest(final String endpointId, String deviceId, String endpointName, byte[] bytes) {
        Log.i(TAG, "Nearby: onConnectionRequest:" + endpointId + ":" + endpointName);

        byte[] payload = null;
        Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, endpointId,
                payload, MainActivity.this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Nearby: acceptConnectionRequest: SUCCESS");

                            mOtherEndpointId = endpointId;
                            updateViewVisibility(CommonUtil.STATE_CONNECTED);
                        } else {
                            Log.i(TAG, "Nearby: acceptConnectionRequest: FAILURE");
                        }
                    }
                });
    }

    private void connectTo(String endpointId, final String endpointName) {
        Log.i(TAG, "Nearby: connectTo:" + endpointId + ":" + endpointName);

        // Send a connection request to a remote endpoint. By passing 'null' for the name,
        // the Nearby Connections API will construct a default name based on device model
        // such as 'LGE Nexus 5'.
        String myName = null;
        byte[] myPayload = null;
        //String key = "doortemporarykey";
        Nearby.Connections.sendConnectionRequest(mGoogleApiClient, myName, endpointId, myPayload,
                new Connections.ConnectionResponseCallback() {
                    @Override
                    public void onConnectionResponse(String endpointId, Status status,
                                                     byte[] bytes) {
                        Log.d(TAG, "Nearby: onConnectionResponse:" + endpointId + ":" + status);
                        if (status.isSuccess()) {
                            Log.i(TAG, "Nearby: onConnectionResponse: " + endpointName + " SUCCESS");
                            Toast.makeText(MainActivity.this, "Connected to " + endpointName,
                                    Toast.LENGTH_SHORT).show();
                            mOtherEndpointId = endpointId;
//                            updateViewVisibility(CommonUtil.STATE_CONNECTED);
                        } else {
                            Log.i(TAG, "Nearby: onConnectionResponse: " + endpointName + " FAILURE");
                        }
                    }
                }, this);
    }

    @Override
    public void onEndpointFound(final String endpointId, String deviceId, String serviceId,
                                final String endpointName) {
        Log.d(TAG, "Nearby: onEndpointFound:" + endpointId + ":" + endpointName);

        MainActivity.this.connectTo(endpointId, endpointName);
    }

    @Override
    public void onEndpointLost(String s) {
        Log.d(TAG, "Nearby: onEndpointLost:" + s);
    }

    @Override
    public void onMessageReceived(String endPointId, byte[] payload, boolean b) {
        Log.i(TAG, "Nearby: onMessageReceived:" + endPointId + ":" + new String(payload));
        mDoorKey = new String(payload);

//        askForDoorOpen(this);
        new AskForDoorOpen().askForDoorOpen(this, mEmail, mDoorId, mDoorKey);
    }


    @Override
    public void onDisconnected(String s) {

    }

    private class BeaconVisibilityListener extends MessageListener {

        @Override
        public void onFound(final Message message) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDoorId = new String(message.getContent());
                    Toast.makeText(
                            MainActivity.this, "Beacon found " + mDoorId, Toast.LENGTH_SHORT).show();
                    //startDiscovery();
                    mDoorKey = "doortemporarykey";
//                    askForDoorOpen(MainActivity.this);
                    new AskForDoorOpen().askForDoorOpen(MainActivity.this, mEmail, mDoorId, mDoorKey);
                }
            });
            Log.i(TAG, "Found beacon: " + message);

        }

        // Called when a message is no longer nearby.
        @Override
        public void onLost(final Message message) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Beacon lost" + message.toString(), Toast.LENGTH_SHORT)
                            .show();
                }
            });
            Log.i(TAG, "Lost beacon: " + message);

        }
    }

    protected void onStart() {
        Log.e(TAG, "signing in 5");
        super.onStart();
        mGoogleApiClient.connect();
    }


    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        Log.e(TAG, "signing in 2");
        if (mConnectionResult.hasResolution()) {
            Log.e(TAG, "signing in 3");
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
                Log.e(TAG, "signing in 4");
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Begin discovering devices advertising Nearby Connections, if possible.
     */
    private void startDiscovery() {
        Log.i(TAG, "Nearby: startDiscovery");
        if (!isConnectedToNetwork()) {
            Log.i(TAG, "Nearby: startDiscovery: not connected to WiFi network.");
            return;
        }
        Log.i(TAG, "Nearby: Connected to wifi network");

        // Discover nearby apps that are advertising with the required service ID.
        String serviceId = getString(R.string.service_id);
        Nearby.Connections.startDiscovery(mGoogleApiClient, serviceId, TIMEOUT_DISCOVER, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Nearby Discovery : SUCCESS");
                        } else {
                            Log.i(TAG, "Nearby Discovery : FAILURE");
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }


    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        Log.i(TAG, "onConnected:" + arg0);
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

        if (txtName.getText() == null || txtName.getText().length() == 0) {
            // Get user's information
            getProfileInformation();

            // Update the UI after signin
            updateUI(true);
        }
    }

    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            Log.e(TAG, "signing in 6");
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            //btnRevokeAccess.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.VISIBLE);
            btnnearByClient.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "signing in 7");
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.GONE);
            btnnearByClient.setVisibility(View.GONE);
        }
    }

    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            Log.e(TAG, "signing in 8");
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Log.e(TAG, "signing in 9");
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                mEmail = email;
                Log.e(TAG, "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);

                txtName.setText(personName);
                txtEmail.setText(email);

                AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
                        Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                        String scopes = "audience:server:client_id:" + "7158014523-4mmqj2i9ah7j6kp5u7gse48elada74j8.apps.googleusercontent.com"; // Not the app's client ID.
                        String Auth ="";
//                        GoogleAccountCredential credential;
//                        String[] SCOPES = { TasksScopes.TASKS_READONLY , TasksScopes.TASKS};
//
//                        credential = GoogleAccountCredential.usingOAuth2( getApplicationContext(), Arrays.asList(SCOPES)) .setBackOff(new ExponentialBackOff()) .setSelectedAccountName(accountName);

                        try {
                           // Log.e(TAG,credential.getToken().toString() + " ajeet 11");

                            Log.e(TAG, "signing in 9");
                            Auth = GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);

                            Log.e(TAG, "signing in 9");
                        } catch (IOException e) {
                            Log.e(TAG, "signing in e1");
                            Log.e(TAG, "Error retrieving ID token.", e);
                        } catch (GoogleAuthException e) {
                            Log.e(TAG, "signing in e2");
                            Log.e(TAG, "Error retrieving ID token.", e);
                        }
                        Log.e(TAG,Auth + " ajeet 11");
                        return Auth;
                    }

                    @Override
                    protected void onPostExecute(String token) {
                        Log.i(TAG, "Access token retrieved:" );
                    }

                };
               // task.execute();


                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X
                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + PROFILE_PIC_SIZE;

                new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);



            } else {
                Log.e(TAG, "signing in 10");
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
        updateUI(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Button on click listener
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                // Signin button clicked
                signInWithGplus();
                break;
            case R.id.btn_sign_out:
                // Signout button clicked
                signOutFromGplus();
                break;
            case R.id.btn_revoke_access:
                // Revoke access button clicked
                revokeGplusAccess();
                break;
            case R.id.button_client:
                // Invoke NearbyClient
                invokeNearByClient();
                break;
        }
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        Log.e(TAG, "signing in");
        if (!mGoogleApiClient.isConnecting()) {
            Log.e(TAG, "signing in 1");
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Sign-out from google
     * */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateUI(false);
        }
    }

    /**
     * Revoking access from google
     * */
    private void revokeGplusAccess() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e(TAG, "User access revoked!");
                            mGoogleApiClient.connect();
                            updateUI(false);
                        }

                    });
        }
    }

    private void invokeNearByClient() {
        Log.i(TAG, "Kishore: Starting Client Activity");
        new AskForDoorOpen().askForDoorOpen(MainActivity.this, mEmail, mDoorId, mDoorKey);

//        askForDoorOpen(this);
//        Intent detailIntent = new Intent(this, NearByClientActivity.class);
//        startActivity(detailIntent);
    }
        /**
         * Background Async task to load user profile picture from url
         * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
