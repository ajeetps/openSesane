
package com.example.ajeetps.userApp;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_EMAIL_ADDRESS;

import com.google.android.apps.enterprise.dmagent.androidapi.AndroidApiFactory;
import com.google.android.apps.enterprise.dmagent.androidapi.IDevicePolicyManager;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Device admin state change receiver. This is a listener for device admin state
 * changes. We simply pass on all events to SecurityPolicy class for centralized
 * processing. Note: This is instantiated by incoming messages.
 *
 */
public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {

  // This is method for this package and subpackages only.
  public static ComponentName getComponentName(Context context) {
    return new ComponentName(context, DeviceAdminReceiver.class);
  }



  @Override
  public void onPasswordSucceeded(Context context, Intent intent) {
    IDevicePolicyManager devicePolicyManager = AndroidApiFactory.getDevicePolicyManager(context);
    Log.i("sachinp", "Password attempt successful.")
  }

}