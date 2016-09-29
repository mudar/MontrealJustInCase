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

package ca.mudar.mtlaucasou.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.RawRes;
import android.util.Log;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.api.ApiClient;
import ca.mudar.mtlaucasou.api.GeoApiService;
import ca.mudar.mtlaucasou.data.RealmQueries;
import ca.mudar.mtlaucasou.data.UserPrefs;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.geojson.PointsFeatureCollection;
import io.realm.Realm;
import retrofit2.Response;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class SyncService extends IntentService {
    private static final String TAG = makeLogTag("SyncService");

    private Realm mRealm;

    public static Intent getIntent(Context context) {
        return new Intent(context, SyncService.class);
    }

    public SyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mRealm = Realm.getDefaultInstance();
        final long startTime = System.currentTimeMillis();

        if (!UserPrefs.getInstance(this).hasLoadedData()) {
            loadInitialLocalData();
        } else {
            // TODO handle updates frequency and redundancy
//            downloadRemoteUpdatesIfAvailable();
        }

        Log.v(TAG, String.format("Data sync duration: %dms", System.currentTimeMillis() - startTime));

        mRealm.close();
    }

    private void loadInitialLocalData() {
        mRealm.beginTransaction();

        importLocalData(R.raw.fire_halls, Const.MapTypes.FIRE_HALLS);
        importLocalData(R.raw.spvm_stations, Const.MapTypes.SVPM_STATIONS);
        importLocalData(R.raw.water_supplies, Const.MapTypes.WATER_SUPPLIES);
        importLocalData(R.raw.air_conditioning, Const.MapTypes.WATER_SUPPLIES);
        importLocalData(R.raw.emergency_hostes, Const.MapTypes.EMERGENCY_HOSTELS);
        importLocalData(R.raw.hospitals, Const.MapTypes.HOSPITALS);

        mRealm.commitTransaction();
    }

    private void downloadRemoteUpdatesIfAvailable() {
        importRemoteData(Const.MapTypes.FIRE_HALLS);
        importRemoteData(Const.MapTypes.SVPM_STATIONS);
        importRemoteData(Const.MapTypes.WATER_SUPPLIES);
        importRemoteData(Const.MapTypes.EMERGENCY_HOSTELS);
        importRemoteData(Const.MapTypes.HOSPITALS);
    }

    private void importLocalData(@RawRes int resource, @MapType String mapType) {
        final InputStream inputStream = getResources().openRawResource(resource);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        final PointsFeatureCollection collection = new Gson()
                .fromJson(inputStreamReader, PointsFeatureCollection.class);

        RealmQueries.cacheMapData(mRealm, collection.getFeatures(), mapType, false);
    }

    /**
     * Request the GeoJSON data from the API
     *
     * @param mapType
     */
    private void importRemoteData(@MapType String mapType) {
        final GeoApiService apiService = ApiClient.getService();

        Response<PointsFeatureCollection> response;
        switch (mapType) {
            case Const.MapTypes.FIRE_HALLS:
                response = ApiClient.getFireHalls(apiService);
                break;
            case Const.MapTypes.SVPM_STATIONS:
                response = ApiClient.getSpvmStations(apiService);
                break;
            case Const.MapTypes.WATER_SUPPLIES:
                response = ApiClient.getWaterSupplies(apiService);
                break;
            case Const.MapTypes.EMERGENCY_HOSTELS:
                response = ApiClient.getEmergencyHostels(apiService);
                break;
            case Const.MapTypes.HOSPITALS:
                response = ApiClient.getHospitals(apiService);
                break;
            default:
                response = null;
                break;
        }

        if (response != null) {
            PointsFeatureCollection collection = response.body();
            if (collection != null && collection.getFeatures() != null) {
                RealmQueries.cacheMapData(mRealm, collection.getFeatures(), mapType, true);
            }
        }

        if (Const.MapTypes.WATER_SUPPLIES.equals(mapType)) {
            // WATER_SUPPLIES supplies has an extra source: air conditioning
            response = ApiClient.getWaterSupplies(apiService);
            if (response != null) {
                PointsFeatureCollection collection = response.body();
                if (collection != null && collection.getFeatures() != null) {
                    RealmQueries.cacheMapData(mRealm, collection.getFeatures(), mapType, true);
                }
            }
        }
    }
}
