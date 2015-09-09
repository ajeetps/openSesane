
package com.example.ajeetps.userApp;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Starts the uses-policies ui activity.
 */
public class ActivateDeviceAdminActivity extends Activity {
  static final String LOG_TAG = LogTag.TAG;
  static final int ACTIVITY_DONE = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(LOG_TAG, "ActivateDeviceAdminActivity is created.");
  }

  @Override
  protected void onStart() {
    super.onStart();
    ComponentName componentName = DeviceAdminReceiver.getComponentName(this);

    Log.i(LOG_TAG, "Trying to register as DeviceAdmin again.");
    // Launch the activity to have the user enable our admin.
    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
    startActivityForResult(intent, ACTIVITY_DONE);
    return;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      Log.i(LOG_TAG, "Registered as device admininstrator.");
    } else {
      Log.w(LOG_TAG, "Cancelled the device admininstrator activation.");
    }
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.i(LOG_TAG, "ActivateDeviceAdminActivity is destroyed.");
  }
}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
