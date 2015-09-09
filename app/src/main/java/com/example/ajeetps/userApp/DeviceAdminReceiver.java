
package com.example.ajeetps.userApp;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_EMAIL_ADDRESS;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Device admin state change receiver. This is a listener for device admin state
 * changes. We simply pass on all events to SecurityPolicy class for centralized
 * processing. Note: This is instantiated by incoming messages.
 *
 */
public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {
  private static final String TAG = "Open Sesame";

  // This is method for this package and subpackages only.
  public static ComponentName getComponentName(Context context) {
    return new ComponentName(context, DeviceAdminReceiver.class);
  }

  @Override
  public void onPasswordSucceeded(Context context, Intent intent) {
    DevicePolicyManager devicePolicyManager = (DevicePolicyManager)
            context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    Log.w(TAG, "Password attempt successful.");
  }

  @Override
  public void onPasswordFailed(Context context, Intent intent) {
    DevicePolicyManager devicePolicyManager = (DevicePolicyManager)
            context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    Log.w(TAG, "Password attempt failed.");
  }
}