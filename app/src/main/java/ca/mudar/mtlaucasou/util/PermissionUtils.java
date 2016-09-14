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
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Utility class for access to runtime permissions.
 */
public abstract class PermissionUtils {

    public static boolean checkLocationPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED);
    }

    public static void requestLocationPermission(AppCompatActivity activity,
                                                 @Nullable View snackbarView) {

        requestPermissionOrShowRationale(activity,
                ACCESS_FINE_LOCATION,
                Const.RequestCodes.LOCATION_PERMISSION,
                snackbarView);
    }

    /**
     * Requests the fine location permission. If a rationale with an additional explanation should
     * be shown to the user, displays a Snackbar that triggers the request.
     */
    private static void requestPermissionOrShowRationale(final AppCompatActivity activity,
                                                         final String permission,
                                                         final int requestId,
                                                         @Nullable View snackbarView) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            final View view = (snackbarView != null) ? snackbarView :
                    activity.findViewById(android.R.id.content);
            // Display a dialog with rationale.
            Snackbar
                    .make(view,
                            R.string.snackbar_location_permission_needed,
                            Snackbar.LENGTH_LONG)
                    .setAction(R.string.btn_ok,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    requestPermission(activity, permission, requestId);
                                }
                            })
                    .show();
        } else {
            // Location permission has not been granted yet, request it.
            requestPermission(activity, permission, requestId);
        }
    }

    private static void requestPermission(AppCompatActivity activity, String permission,
                                          int requestId) {

        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestId);
    }
}