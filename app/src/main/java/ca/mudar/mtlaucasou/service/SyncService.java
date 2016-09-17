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
import ca.mudar.mtlaucasou.data.RealmQueries;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.geojson.PointsFeatureCollection;
import io.realm.Realm;

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

        importLocalData(R.raw.fire_halls, Const.MapTypes.FIRE_HALLS);
        importLocalData(R.raw.spvm_stations, Const.MapTypes.SVPM_STATIONS);
        importLocalData(R.raw.water_supplies, Const.MapTypes.WATER_SUPPLIES);
        importLocalData(R.raw.emergency_hostes, Const.MapTypes.EMERGENCY_HOSTELS);

        Log.v(TAG, String.format("Duration: %dms", System.currentTimeMillis() - startTime));

        mRealm.close();
    }

    private void importLocalData(@RawRes int resource, @MapType String mapType) {
        final InputStream inputStream = getResources().openRawResource(resource);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        final PointsFeatureCollection collection = new Gson()
                .fromJson(inputStreamReader, PointsFeatureCollection.class);

        RealmQueries.cacheMapData(mRealm, collection.getFeatures(), mapType);
    }
}
