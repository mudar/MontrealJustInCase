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
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.mudar.mtlaucasou.model.LayerType;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.RoomPlacemark;
import ca.mudar.mtlaucasou.model.RoomPolygon;
import ca.mudar.mtlaucasou.model.geojson.Feature;
import ca.mudar.mtlaucasou.model.geojson.GeoPoint;
import ca.mudar.mtlaucasou.model.geojson.MultiPolygonGeometry;
import ca.mudar.mtlaucasou.model.geojson.PointGeometry;
import ca.mudar.mtlaucasou.model.geojson.SimplePolygonGeometry;
import ca.mudar.mtlaucasou.model.geojson.base.BaseGeometry;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class RoomQueries {
    private static final String TAG = makeLogTag("RoomQueries");
    private static final Set<String> HEAT_WAVE_LAYERS = new HashSet<>(Arrays.asList(
            LayerType.AIR_CONDITIONING,
            LayerType.POOLS,
            LayerType.WADING_POOLS,
            LayerType.PLAY_FOUNTAINS));
    private static final Set<String> HEALTH_LAYERS = new HashSet<>(Arrays.asList(
            LayerType.HOSPITALS,
            LayerType.CLSC));


    /**
     * Delete data from the Room db
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
     * Save the downloaded data to the Room db
     *
     * @param db
     * @param features
     * @param mapType
     * @param layerType
     */
    public static void cacheMapData(AppDatabase db,
                                    List<Feature> features,
                                    @MapType String mapType,
                                    @LayerType String layerType) {
        // Loop over results, convert GeoJSON to Room then add to db
        for (Feature feature : features) {
            final BaseGeometry baseGeometry = feature.getGeometry();

            if (baseGeometry instanceof PointGeometry) {
                db.placemarkDao().insert(new RoomPlacemark.Builder(feature)
                        .mapType(mapType)
                        .layerType(layerType, feature.getProperties().getType())
                        .build());
            } else if (baseGeometry instanceof SimplePolygonGeometry) {
                db.polygonDao().insert(new RoomPolygon.Builder(feature)
                        .mapType(mapType)
                        .layerType(layerType)
                        .coordinates(((SimplePolygonGeometry) baseGeometry).getCoordinates())
                        .build());
            } else if (baseGeometry instanceof MultiPolygonGeometry) {
                // Create multiple simple polygons instead
                Log.w(TAG, String.format("Verification needed: feature %s is a MultiPolygonGeometry.",
                        feature.getId()));
                final RoomPolygon.Builder builder = new RoomPolygon.Builder(feature)
                        .mapType(mapType)
                        .layerType(layerType);
                final List<List<List<GeoPoint>>> geometry = ((MultiPolygonGeometry) baseGeometry).getCoordinates();
                for (List<List<GeoPoint>> polygonCoordinates : geometry) {
                    final RoomPolygon polygon = builder.coordinates(polygonCoordinates)
                            .build();
                    db.polygonDao().insert(polygon);
                }
            }
        }
    }

    /**
     * Save the downloaded data to the Room db enclosed in a transaction
     *
     * @param db
     * @param features
     * @param mapType
     * @param layerType
     */
    public static void cacheMapDataWithTransaction(AppDatabase db,
                                                   List<Feature> features,
                                                   @MapType String mapType,
                                                   @LayerType String layerType) {
        db.beginTransaction();
        try {
            cacheMapData(db, features, mapType, layerType);

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
    public static LiveData<List<RoomPlacemark>> queryPlacemarksByMapType(AppDatabase db,
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
    private static LiveData<List<RoomPlacemark>> queryPlacemarksByMapType(AppDatabase db,
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
    public static List<RoomPlacemark> queryPlacemarksByName(AppDatabase db, String name) {
        return db.placemarkDao()
                .getByName("% " + name + "%");
    }

    /**
     * Get all Polygons for requested placemarkId
     *
     * @param db
     * @param placemarkId
     * @return
     */
    public static LiveData<List<RoomPolygon>> queryPolygonsByPlacemarkId(AppDatabase db, long placemarkId) {
        return db.polygonDao()
                .getByPlacemarkId(placemarkId);
    }
}
