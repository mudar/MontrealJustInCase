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

package ca.mudar.mtlaucasou.ui.listener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;

import ca.mudar.mtlaucasou.ui.adapter.PlacemarkInfoWindowAdapter;
import ca.mudar.mtlaucasou.util.MapUtils;
import ca.mudar.mtlaucasou.util.PermissionUtils;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class LocationUpdatesManager implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnCameraMoveStartedListener,
        PlacemarkInfoWindowAdapter.InfoWindowLocationCallbacks {

    private static final String TAG = makeLogTag("LocationUpdatesManager");
    private static final long LOCATION_UPDATES_INTERVAL = DateUtils.SECOND_IN_MILLIS * 10;
    private static final long LOCATION_UPDATES_FASTEST_INTERVAL = DateUtils.SECOND_IN_MILLIS * 5;

    private final Context mContext;
    private final LocationUpdatesCallbacks mListener;
    private GoogleMap mMap;
    private final GoogleApiClient mGoogleApiClient;
    private Location mUserLocation;
    private LocationRequest mLocationRequest;
    private boolean mHasCameraMoved;

    public LocationUpdatesManager(Context context, LocationUpdatesCallbacks listener) {
        mContext = context;
        mListener = listener;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void setGoogleMap(GoogleMap map) {
        mMap = map;

        mMap.setOnCameraMoveStartedListener(this);
        moveMapToMyLocation();
    }

    /**
     * Connect the GoogleApiClient, should be called from activity's onStart()
     */
    public void onStart() {
        mGoogleApiClient.connect();
    }

    /**
     * Disconnect the GoogleApiClient, should be called from activity's onStop()
     */
    public void onStop() {
        mGoogleApiClient.disconnect();
    }

    public void onLocationSettingsResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK &&
                PermissionUtils.checkLocationPermission(mContext) &&
                mGoogleApiClient.isConnected()) {
            // Start locationUpdates requests
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest,
                    this);
        }
    }

    public void onLocationPermissionGranted() {
        if (mGoogleApiClient.isConnected()) {
            moveMapToLastLocation();
        }
    }

    @Nullable
    public Location getUserLocation() {
        return mUserLocation;
    }


    /**
     * Implements GoogleApiClient.ConnectionCallbacks
     *
     * @param bundle Bundle of data provided to clients by Google Play services.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        moveMapToLastLocation();
    }

    /**
     * Implements GoogleApiClient.ConnectionCallbacks
     *
     * @param cause The reason for the disconnection.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(TAG, "onConnectionSuspended");
    }

    /**
     * Implements LocationListener
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
        mUserLocation = location;
        moveMapToMyLocation();
    }

    /**
     * Implements GoogleApiClient.OnConnectionFailedListener
     *
     * @param connectionResult A ConnectionResult that can be used for resolving the error,
     *                         and deciding what sort of error occurred.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }

    /**
     * Implements GoogleMap.OnCameraMoveStartedListener
     *
     * @param reason The reason for the camera change.
     */
    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == REASON_GESTURE) {
            mHasCameraMoved = true;
        }
    }

    private void moveMapToLastLocation() {
        if (!PermissionUtils.checkLocationPermission(mContext)) {
            return;
        }

        mUserLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mUserLocation == null) {
            checkLocationSettings();
        } else {
            moveMapToMyLocation();
        }
    }

    private void checkLocationSettings() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_UPDATES_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_UPDATES_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        final PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(
                        mGoogleApiClient,
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(mLocationRequest)
                                .build()
                );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                onLocationSettingsResult(result);
            }
        });
    }

    private void onLocationSettingsResult(@NonNull LocationSettingsResult result) {
        // TODO Verify the use of this?
        final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();

        final Status status = result.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // Location settings are satisfied
                onLocationSettingsResult(Activity.RESULT_OK, null);
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().

                    mListener.requestLocationSettingsChange(status);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way
                // to fix the settings so we won't show the dialog.
                break;
        }
    }

    private void moveMapToMyLocation() {
        if (!mHasCameraMoved && mUserLocation != null && mMap != null) {
            MapUtils.moveCameraToMyLocation(mMap, mUserLocation);
        }
    }

    public interface LocationUpdatesCallbacks {
        void requestLocationSettingsChange(Status status) throws IntentSender.SendIntentException;

        void onLocationSettingsActivityResult(int resultCode, Intent data);
    }
}
