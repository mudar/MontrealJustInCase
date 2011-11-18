/*
 * Mobitize for Android 
 * Payment Solutions for Mobile Platforms
 * 
 * Copyright (C) 2011 S.B. Canada <info@mobitize.com>
 * 
 * This file is part of Mobitize for Android
 * 
 * @author Mudar Noufal <mn@mudar.ca>
 */

package ca.mudar.mtlaucasou.utils;

import ca.mudar.mtlaucasou.R;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionHelper {
    public static boolean hasConnection(final Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMan.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        } else {
            return networkInfo.isConnected();
        }
    }

    public static void showDialogNoConnection(final Activity activity) {
        AppHelper mAppHelper = (AppHelper) activity.getApplicationContext();

        mAppHelper.showToastText(R.string.toast_network_connection_error, Toast.LENGTH_LONG);
    }

}
