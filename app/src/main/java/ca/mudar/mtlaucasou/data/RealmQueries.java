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

package ca.mudar.mtlaucasou.data;

import android.arch.lifecycle.LiveData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.mudar.mtlaucasou.model.LayerType;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.RealmPlacemark;
import ca.mudar.mtlaucasou.model.geojson.MixedPolygonsFeature;
import ca.mudar.mtlaucasou.model.geojson.PointsFeature;
import ca.mudar.mtlaucasou.model.geojson.base.GeometryFeature;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class RealmQueries {
    private static final String TAG = makeLogTag("RealmQueries");
    private static final Set<String> HEAT_WAVE_LAYERS = new HashSet<>(Arrays.asList(
            LayerType.AIR_CONDITIONING,
            LayerType.POOLS,
            LayerType.WADING_POOLS,
            LayerType.PLAY_FOUNTAINS));
    private static final Set<String> HEALTH_LAYERS = new HashSet<>(Arrays.asList(
            LayerType.HOSPITALS,
            LayerType.CLSC));


    /**
     * Delete data from the Realm db
     *
     * @param db
     * @param layerType
     */
    public static void clearMapData(AppDatabase db, @LayerType String layerType) {
        db.beginTransaction();

        try {
            if (LayerType._HEAT_WAVE_MIXED.equals(layerType)) {
                // The `water_supplies` endpoint provides 3 layerTypes we need to delete
                db.placemarkDao().deleteByLayerType(new String[]{
                        LayerType.POOLS,
                        LayerType.WADING_POOLS,
                        LayerType.PLAY_FOUNTAINS});
            } else {
                db.placemarkDao().deleteByLayerType(new String[]{layerType});
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Save the downloaded data to the Realm db
     *
     * @param db
     * @param geometryFeatures
     * @param mapType
     * @param layerType
     */
    public static <G extends GeometryFeature> void cacheMapData(AppDatabase db,
                                                                List<G> geometryFeatures,
                                                                @MapType String mapType,
                                                                @LayerType String layerType) {
        // Loop over results, convert GeoJSON to Realm then add to db
        for (GeometryFeature feature : geometryFeatures) {
            if (feature instanceof PointsFeature) {
                db.placemarkDao().insert(new RealmPlacemark.Builder((PointsFeature) feature)
                        .mapType(mapType)
                        .layerType(layerType, feature.getProperties().getType())
                        .build());
            } else if (feature instanceof MixedPolygonsFeature) {
// TODO save polygons to Room db
            }
        }
    }

    /**
     * Save the downloaded data to the Realm db enclosed in a transaction
     *
     * @param db
     * @param geometryFeatures
     * @param mapType
     * @param layerType
     */
    public static <G extends GeometryFeature> void cacheMapDataWithTransaction(AppDatabase db,
                                                                               List<G> geometryFeatures,
                                                                               @MapType String mapType,
                                                                               @LayerType String layerType) {
        db.beginTransaction();
        try {
            cacheMapData(db, geometryFeatures, mapType, layerType);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Get all Placemarks for requested mapType and selected layers
     *
     * @param db
     * @param mapType
     * @param layers
     * @return
     */
    public static LiveData<List<RealmPlacemark>> queryPlacemarksByMapType(AppDatabase db,
                                                                          @MapType String mapType,
                                                                          @LayerType Set<String> layers) {
        Set<String> filterLayers = null;
        if (layers != null && !layers.isEmpty()) {
            if (MapType.HEAT_WAVE.equals(mapType)) {
                filterLayers = new HashSet<>(HEAT_WAVE_LAYERS);
                filterLayers.retainAll(layers);
            } else if (MapType.HEALTH.equals(mapType)) {
                filterLayers = new HashSet<>(HEALTH_LAYERS);
                filterLayers.retainAll(layers);
            }
        }

        if (filterLayers == null || filterLayers.isEmpty()) {
            return queryPlacemarksByMapType(db, mapType);
        } else {
            final int size = filterLayers.size();
            return db.placemarkDao().getByMapAndLayerType(mapType, filterLayers.toArray(new String[size]));
        }
    }

    /**
     * Get all Placemarks for requested mapType
     *
     * @param db
     * @param mapType
     * @return
     */
    private static LiveData<List<RealmPlacemark>> queryPlacemarksByMapType(AppDatabase db,
                                                                           @MapType String mapType) {
        return db.placemarkDao().getByMapType(mapType);
    }

    /**
     * Get all Placemarks with name containing the search-word
     *
     * @param db
     * @param name
     * @return
     */
    public static List<RealmPlacemark> queryPlacemarksByName(AppDatabase db, String name) {
        return db.placemarkDao()
                .getByName("% " + name + "%");
    }
}
