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
import java.util.Date;

import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.api.ApiClient;
import ca.mudar.mtlaucasou.data.AppDatabase;
import ca.mudar.mtlaucasou.data.RoomQueries;
import ca.mudar.mtlaucasou.data.UserPrefs;
import ca.mudar.mtlaucasou.model.LayerType;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.geojson.FeatureCollection;
import ca.mudar.mtlaucasou.model.jsonapi.Attributes;
import ca.mudar.mtlaucasou.model.jsonapi.DataItem;
import ca.mudar.mtlaucasou.model.jsonapi.HelloApi;
import ca.mudar.mtlaucasou.util.ApiDataUtils;
import ca.mudar.mtlaucasou.util.LogUtils;
import retrofit2.Response;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class SyncService extends IntentService {
    private static final String TAG = makeLogTag("SyncService");

    private AppDatabase mDatabase;

    public static Intent newIntent(Context context) {
        return new Intent(context, SyncService.class);
    }

    public SyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mDatabase = AppDatabase.getAppDatabase(this);
        final long startTime = System.currentTimeMillis();

        final UserPrefs userPrefs = UserPrefs.getInstance(this);
        if (!userPrefs.hasLoadedData()) {
            loadInitialLocalData();
        } else {
            // TODO handle updates frequency and redundancy
            downloadRemoteUpdatesIfAvailable(userPrefs);
        }

        Log.v(TAG, String.format("Data sync duration: %dms", System.currentTimeMillis() - startTime));
    }

    private void loadInitialLocalData() {
        mDatabase.beginTransaction();

        try {
            importLocalData(R.raw.fire_halls, MapType.FIRE_HALLS, LayerType.FIRE_HALLS);
            importLocalData(R.raw.spvm_stations, MapType.SPVM_STATIONS, LayerType.SPVM_STATIONS);
            importLocalData(R.raw.spvm_stations_polygons, MapType.SPVM_STATIONS, LayerType.SPVM_AREAS);
            importLocalData(R.raw.water_supplies, MapType.HEAT_WAVE, LayerType._HEAT_WAVE_MIXED);
            importLocalData(R.raw.air_conditioning, MapType.HEAT_WAVE, LayerType.AIR_CONDITIONING);
            importLocalData(R.raw.emergency_hostels, MapType.EMERGENCY_HOSTELS, LayerType.EMERGENCY_HOSTELS);
            importLocalData(R.raw.hospitals, MapType.HEALTH, LayerType.HOSPITALS);
            importLocalData(R.raw.clsc, MapType.HEALTH, LayerType.CLSC);

            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    private void downloadRemoteUpdatesIfAvailable(UserPrefs prefs) {
        try {
            Response<HelloApi> helloResponse = ApiClient.hello(ApiClient.getService());
            if (helloResponse != null && helloResponse.body() != null) {
                final HelloApi api = helloResponse.body();
                for (DataItem dataset : api.getData()) {
                    final String key = ApiDataUtils.getSharedPrefsKey(dataset.getId());
                    final Date updatedAt = dataset.getAttributes().getUpdated();

                    if (prefs.isApiDataNewer(key, updatedAt)) {
                        final boolean result = importRemoteData(dataset);
                        if (result) {
                            prefs.setDataUpdatedAt(key, updatedAt);
                        }
                    }
                }
            }

        } catch (Exception e) {
            LogUtils.REMOTE_LOG(e);
        }
    }

    private void importLocalData(@RawRes int resource, @MapType String mapType, @LayerType String layerType) {
        final InputStream inputStream = getResources().openRawResource(resource);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        final Gson gson = ApiClient.getGsonBuilder().create();
        final FeatureCollection collection = gson.fromJson(inputStreamReader,
                FeatureCollection.class);

        RoomQueries.cacheMapData(mDatabase,
                collection.getFeatures(),
                mapType,
                layerType);
    }

    /**
     * Request the GeoJSON data from the API
     *
     * @param dataset The dataset to import into the Room db
     * @return
     */
    private boolean importRemoteData(DataItem dataset) {
        final Response<FeatureCollection> response = ApiClient
                .getPlacemarks(ApiClient.getService(), dataset.getLinks().getSelf());

        if (response != null) {
            FeatureCollection collection = response.body();
            if (collection != null && collection.getFeatures() != null) {
                final Attributes attributes = dataset.getAttributes();

                if (attributes != null) {
                    RoomQueries.clearMapData(mDatabase, attributes.getLayerType());
                    RoomQueries.cacheMapDataWithTransaction(mDatabase,
                            collection.getFeatures(),
                            attributes.getMapType(),
                            attributes.getLayerType());
                }
            }

            return true;
        }

        return false;
    }
}
