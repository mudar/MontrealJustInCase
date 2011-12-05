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

package ca.mudar.mtlaucasou.services;

import ca.mudar.mtlaucasou.provider.PlacemarkContract;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.ConditionedPlaces;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.EmergencyHostels;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.FireHalls;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.PlacemarkColumns;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.SpvmStations;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.WaterSupplies;
import ca.mudar.mtlaucasou.utils.Const;
import ca.mudar.mtlaucasou.utils.Const.PrefsNames;
import ca.mudar.mtlaucasou.utils.Lists;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

public class DistanceUpdateService extends IntentService {
    private static final String TAG = "DistanceUpdateService";

    protected ContentResolver contentResolver;
    protected SharedPreferences prefs;
    protected Editor prefsEditor;

    public DistanceUpdateService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        contentResolver = getContentResolver();

        prefs = getSharedPreferences(Const.APP_PREFS_NAME, Context.MODE_PRIVATE);
        prefsEditor = prefs.edit();
    }

    /**
     * Calculate distance and save it in the database. This way distance is
     * calculated at write time which is less often than number of reads (or
     * bindView). This also allows for updates in the listView by the
     * CursorLoader.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        final long startLocal = System.currentTimeMillis();

        double latitude = intent.getDoubleExtra(Const.INTENT_EXTRA_GEO_LAT, Double.NaN);
        double longitude = intent.getDoubleExtra(Const.INTENT_EXTRA_GEO_LNG, Double.NaN);

        if (latitude == Double.NaN || longitude == Double.NaN) {
            return;
        }

        /**
         * Check to see if this is a forced update. Currently not in use in the
         * UI.
         */
        boolean doUpdate = intent.getBooleanExtra(Const.INTENT_EXTRA_FORCE_UPDATE, false);

        /**
         * If it's not a forced update then check to see if we've moved far
         * enough, or there's been a long enough delay since the last update and
         * if so, enforce a new update.
         */
        if (!doUpdate) {
            Location newLocation = new Location(Const.LOCATION_PROVIDER);
            newLocation.setLatitude(latitude);
            newLocation.setLongitude(longitude);

            /**
             * Retrieve the last update time and place.
             */
            long lastTime = prefs.getLong(PrefsNames.LAST_UPDATE_TIME, Long.MIN_VALUE);
            float lastLat = prefs.getFloat(PrefsNames.LAST_UPDATE_LAT, Float.NaN);
            float lastLng = prefs.getFloat(PrefsNames.LAST_UPDATE_LNG, Float.NaN);

            if ((lastLat == Float.NaN) || (lastLng == Float.NaN)) {
                return;
            }

            Location lastLocation = new Location(Const.LOCATION_PROVIDER);
            lastLocation.setLatitude(lastLat);
            lastLocation.setLongitude(lastLng);

            /**
             * If update time and distance bounds have been passed, do an
             * update.
             */
            if ((lastTime < System.currentTimeMillis() - Const.MAX_TIME)
                    || (lastLocation.distanceTo(newLocation) > Const.MAX_DISTANCE)) {
                doUpdate = true;
            }
        }

        if (doUpdate) {
            try {
                contentResolver.applyBatch(PlacemarkContract.CONTENT_AUTHORITY,
                        updateDistance(FireHalls.CONTENT_URI, latitude, longitude));
                contentResolver.applyBatch(PlacemarkContract.CONTENT_AUTHORITY,
                        updateDistance(SpvmStations.CONTENT_URI, latitude, longitude));
                contentResolver.applyBatch(PlacemarkContract.CONTENT_AUTHORITY,
                        updateDistance(WaterSupplies.CONTENT_URI, latitude, longitude));
                contentResolver.applyBatch(PlacemarkContract.CONTENT_AUTHORITY,
                        updateDistance(EmergencyHostels.CONTENT_URI, latitude, longitude));
                contentResolver.applyBatch(PlacemarkContract.CONTENT_AUTHORITY,
                        updateDistance(ConditionedPlaces.CONTENT_URI, latitude, longitude));
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
            } catch (OperationApplicationException e) {
                Log.e(TAG, e.getMessage());
            }

            /**
             * Save the last update time and place to the Shared Preferences.
             */
            prefsEditor.putFloat(PrefsNames.LAST_UPDATE_LAT, (float) latitude);
            prefsEditor.putFloat(PrefsNames.LAST_UPDATE_LNG, (float) longitude);
            prefsEditor.putLong(PrefsNames.LAST_UPDATE_TIME, System.currentTimeMillis());
            prefsEditor.commit();
        }
        Log.v(TAG, "Distance calculation took " + (System.currentTimeMillis()
                - startLocal) + " ms");
    }

    /**
     * The cursor columns projection.
     */
    static final String[] PLACEMARKS_SUMMARY_PROJECTION = new String[] {
            BaseColumns._ID,
            PlacemarkColumns.PLACEMARK_GEO_LAT,
            PlacemarkColumns.PLACEMARK_GEO_LNG,
            PlacemarkColumns.PLACEMARK_DISTANCE
    };

    protected ArrayList<ContentProviderOperation> updateDistance(Uri contentUri,
            double startLatitude, double startLongitude) {
        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newUpdate(contentUri);

        Cursor queuedPlacemarks = contentResolver.query(contentUri, PLACEMARKS_SUMMARY_PROJECTION,
                null, null, PlacemarkColumns.PLACEMARK_DISTANCE);

        try {
            final int indexId = queuedPlacemarks.getColumnIndexOrThrow(BaseColumns._ID);
            final int indexLat = queuedPlacemarks
                    .getColumnIndexOrThrow(PlacemarkColumns.PLACEMARK_GEO_LAT);
            final int indexLng = queuedPlacemarks
                    .getColumnIndexOrThrow(PlacemarkColumns.PLACEMARK_GEO_LNG);
            final int indexDistance = queuedPlacemarks
                    .getColumnIndexOrThrow(PlacemarkColumns.PLACEMARK_DISTANCE);
            final String selection = BaseColumns._ID + " = ? ";

            while (queuedPlacemarks.moveToNext()) {
                String[] queuedId = new String[] {
                        queuedPlacemarks.getString(indexId)
                };
                double endLat = queuedPlacemarks.getDouble(indexLat);
                double endLng = queuedPlacemarks.getDouble(indexLng);
                int oldDistance = queuedPlacemarks.getInt(indexDistance);

                /**
                 * Calculate the new distance.
                 */
                float[] results = new float[1];
                Location.distanceBetween(startLatitude, startLongitude, endLat, endLng, results);
                int distance = (int) results[0];

                /**
                 * Compare the new distance to the old one, to avoid the db
                 * write operation if not necessary.
                 */
                if (Math.abs(oldDistance - distance) > Const.DB_MAX_DISTANCE) {
                    builder = ContentProviderOperation.newUpdate(contentUri);
                    builder.withValue(PlacemarkColumns.PLACEMARK_DISTANCE, distance);
                    builder.withSelection(selection, queuedId);

                    batch.add(builder.build());
                }
            }

        } finally {
            queuedPlacemarks.close();
        }

        return batch;
    }
}
