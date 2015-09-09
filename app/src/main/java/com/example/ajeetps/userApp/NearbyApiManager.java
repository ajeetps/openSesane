package com.example.ajeetps.userApp;

/**
 * Created by ajeetps on 9/8/15.
 */
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.Strategy;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Class for managing the call to nearby API client.
 */
public class NearbyApiManager implements ConnectionCallbacks, OnConnectionFailedListener {
    public static final int REQUEST_CODE_NEARBY_PERMISSION = 1001;
    private static String TAG = "NearbyApiManager";

    private GoogleApiClient googleApiClient;
    private MessageListener messageListener;
    private boolean resolvingNearbyPermissionError = false;
    private Activity activity;

    public NearbyApiManager(final Activity activity, final MessageListener messageListener) {
        this.activity = activity;

        googleApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                        //.addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        this.messageListener = messageListener;
        connectToGoogleClient();
    }

    private void connectToGoogleClient() {
        if (googleApiClient != null && !isClientConnecting()) {
            googleApiClient.connect();
        }
    }

    /**
     * Returns true if a client is connected or attempting to connect.
     */
    private boolean isClientConnecting() {
        return googleApiClient.isConnected() || googleApiClient.isConnecting();
    }

    /**
     * Called when the related activity is becoming visible to the user.
     */
    public void onStart() {
        connectToGoogleClient();
    }

    /**
     * Called when the related activity is no longer visible to the user, because another activity
     * has been resumed and is covering this one. This may happen either because a new activity is
     * being started, an existing one is being brought in front of this one, or this one is being
     * destroyed.
     */
    public void onStop() {
        if (googleApiClient != null && isClientConnecting()) {
            googleApiClient.disconnect();
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        subscribe();
    }

    public void subscribe() {
        MessageFilter filter = new MessageFilter.Builder()
                // Include whichever of these you are interested in (or all of them, or none):
                //.includeNamespacedType("com.google.location.locus", "googleLocationId")
                //.includeNamespacedType("com.google.location.locus", "googleRoomOrgstoreName")
                //.includeNamespacedType("com.google.location.locus", "googleRoomOrgstoreType")
                //.includeNamespacedType("com.google.location.locus", "googleRoomFeatureId")
                .includeNamespacedType("open-sesame-1050", "opensesame")
                        // If you've added your own attachments (see "add your attachments" below):
                //.includeAllMyTypes()
                .build();
        if (googleApiClient != null) {
            Nearby.Messages.subscribe(googleApiClient, messageListener, Strategy.BLE_ONLY, filter)
                    // You must supply this callback to handle the runtime opt-in requirement.
                    .setResultCallback(new ErrorCheckingCallback());
            Log.i(TAG, "subscribing to nearby client");
        }
    }

    private class ErrorCheckingCallback implements ResultCallback<Status> {
        @Override
        public void onResult(@NonNull final Status status) {
            // Currently, the only resolvable error is that the device is not opted
            // in to Nearby. Starting the resolution displays an opt-in dialog.
            if (status.isSuccess()) {
                Log.i(TAG, "successfully subscribed");
            } else {
                Log.e(TAG, "could not subscribe");
                handleUnsuccessfulNearbyResult(status);
            }
        }
    }

    private void handleUnsuccessfulNearbyResult(Status status) {
        Log.e(TAG, "processing error, status = " + status);
        if (status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN) {
            if (!resolvingNearbyPermissionError) {
                try {
                    resolvingNearbyPermissionError = true;
                    status.startResolutionForResult(activity, REQUEST_CODE_NEARBY_PERMISSION);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (status.getStatusCode() == ConnectionResult.NETWORK_ERROR) {
                Log.e(TAG, "No connectivity, cannot proceed");
            } else {
                Log.e(TAG, "Unsuccessful: " + status.getStatusMessage());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GCore api connection suspended. Reason: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "gCore api connection failed. Reason: " + connectionResult);
    }
}
