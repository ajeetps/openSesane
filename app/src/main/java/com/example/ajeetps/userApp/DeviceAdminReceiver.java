
package com.example.ajeetps.userApp;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_EMAIL_ADDRESS;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Device admin state change receiver. This is a listener for device admin state
 * changes. We simply pass on all events to SecurityPolicy class for centralized
 * processing. Note: This is instantiated by incoming messages.
 *
 */
public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {
  private static final String TAG = "Open Sesame";
    public static final String MY_APP_PREFERENCES_FILE = "my_app_preferences";
    public static final String SHOULD_OPEN_DOOR_KEY = "should_open_door";

    // Setting some default values for testing
    public static final String email = "brokerprodsync3@gmail.com";
    public static final String doorId = "Disc6S";
    public static final String doorKey = "doortemporarykey";


    // This is method for this package and subpackages only.
  public static ComponentName getComponentName(Context context) {
    return new ComponentName(context, DeviceAdminReceiver.class);
  }

  @Override
  public void onPasswordSucceeded(Context context, Intent intent) {
    DevicePolicyManager devicePolicyManager = (DevicePolicyManager)
            context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    Log.w(TAG, "Password attempt successful.");
    SharedPreferences prefs = context.getSharedPreferences(MY_APP_PREFERENCES_FILE,
            Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putBoolean(SHOULD_OPEN_DOOR_KEY, true);
      editor.commit();

      // Now open the door.
      new AskForDoorOpen().askForDoorOpen(context, email, doorId, doorKey);
      new CustomNotificationManager().clearNotification(context);
  }

  @Override
  public void onPasswordFailed(Context context, Intent intent) {
    DevicePolicyManager devicePolicyManager = (DevicePolicyManager)
            context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    Log.w(TAG, "Password attempt failed.");
      SharedPreferences prefs = context.getSharedPreferences(MY_APP_PREFERENCES_FILE,
              Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putBoolean(SHOULD_OPEN_DOOR_KEY, false);
      editor.commit();

      // Send a notification telling, please unlock the phone to open the door.
      new CustomNotificationManager().showNotification(context, "Please unlock !!!");
//      new AskForDoorOpen().askForDoorOpen(context, email, doorId, doorKey);
  }
}