/*
    Montréal Just in Case
    Copyright (C) 2011  Mudar Noufal <mn@mudar.ca>

    Geographic locations of public safety services. A Montréal Open Data
    project.

    This file is part of Montréal Just in Case.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.mudar.mtlaucasou.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.data.UserPrefs;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

/**
 * Utility class for access to runtime permissions.
 */
public abstract class PermissionUtils {
    private static final String TAG = makeLogTag("PermissionUtils");

    public static boolean checkLocationPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Requests the fine location permission.
     */
    public static void requestLocationPermission(AppCompatActivity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{ACCESS_FINE_LOCATION},
                Const.RequestCodes.LOCATION_PERMISSION);
    }

    /**
     * After onRequestPermissionsResult(), if the permission is still not granted, a call to
     * this method tries to explain to the user why
     *
     * @param activity
     * @param snackbarView
     * @return
     */
    public static void showLocationRationaleOrSurrender(final AppCompatActivity activity,
                                                        @Nullable View snackbarView) {

        final View view = (snackbarView != null) ? snackbarView :
                activity.findViewById(android.R.id.content);
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)) {
            // Display a dialog with rationale.
            Snackbar
                    .make(view,
                            R.string.snackbar_location_permission_needed,
                            Snackbar.LENGTH_LONG)
                    .setAction(R.string.btn_ok,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    requestLocationPermission(activity);
                                }
                            })
                    .show();
        } else {
            UserPrefs.getInstance(activity).setPermissionDeniedForEver(true);
            Snackbar
                    .make(view,
                            R.string.snackbar_location_permission_denied,
                            Snackbar.LENGTH_LONG)
                    .setAction(R.string.btn_device_settings,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                    intent.setData(uri);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    activity.startActivity(intent);
                                }
                            })
                    .show();
        }
    }

    public static boolean checkPermissionWasDeniedForEver(Context context) {
        final UserPrefs prefs = UserPrefs.getInstance(context);

        if (prefs.isPermissionDeniedForEver()) {
            // User has previously deniedForEver
            if (checkLocationPermission(context)) {
                // User has changed his mind, granting permission from app settings.
                prefs.setPermissionDeniedForEver(false);
            } else {
                // User has previously deniedForEver
                return true;
            }
        }

        return false;
    }
}