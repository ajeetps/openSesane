package com.example.ajeetps.userApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by sachinp on 09/09/15.
 */
public class AskForDoorOpen {
    private static final String TAG = "Open Sesame";

    public void askForDoorOpen(Context context, final String email, final String mDoorId, final String mDoorKey) {
        SharedPreferences prefs = context.getSharedPreferences(
                DeviceAdminReceiver.MY_APP_PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        boolean should_open_door = prefs.getBoolean(DeviceAdminReceiver.SHOULD_OPEN_DOOR_KEY, true);
        if (!should_open_door) {
            Log.w(TAG, "Sorry, door can not be opened.");
        }

        AsyncTask<Void, Void, String> task1 = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.e(TAG, "http response 1");
                String response = "";

                try {


                    URL url = new URL("http://hangouts-xml.appspot.com/authdoor");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    HashMap<String, String> param = new HashMap<String, String>();

                    param.put("email", email);
                    param.put("doorId", mDoorId);
                    param.put("doorKey", mDoorKey);


                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(getPostDataString(param));
                    writer.flush();
                    writer.close();
                    os.close();

                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            response += line;
                        }
                    } else {
                        response = "";
                    }
                    Log.e(TAG, "http response" + response);
                } catch (java.io.IOException e) {
                    if (e.getMessage().contains("authentication challenge")) {
                    } else {
                        Log.e(TAG, "http response" + e.getStackTrace());
                    }
                }
                return response;

            }
            @Override
            protected void onPostExecute(String response) {
                Log.i(TAG, "Access token retrieved:" + response);
            }

        };

        task1.execute();
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
