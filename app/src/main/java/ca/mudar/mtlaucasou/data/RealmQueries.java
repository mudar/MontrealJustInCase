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

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.Const.LayerTypes;
import ca.mudar.mtlaucasou.model.LayerType;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.RealmPlacemark;
import ca.mudar.mtlaucasou.model.geojson.PointsFeature;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class RealmQueries {
    private static final String TAG = makeLogTag("RealmQueries");
    private static final Set<String> HEAT_WAVE_LAYERS = new HashSet<>(Arrays.asList(
            LayerTypes.AIR_CONDITIONING,
            LayerTypes.POOLS,
            LayerTypes.WADING_POOLS,
            LayerTypes.PLAY_FOUNTAINS));
    private static final Set<String> HEALTH_LAYERS = new HashSet<>(Arrays.asList(
            LayerTypes.HOSPITALS,
            LayerTypes.AIR_CONDITIONING));


    /**
     * Delete data from the Realm db
     *
     * @param realm
     * @param layerType
     */
    public static void clearMapData(Realm realm, @LayerType String layerType) {
        realm.beginTransaction();

        final RealmQuery query = realm.where(RealmPlacemark.class);
        if (LayerTypes._HEAT_WAVE_MIXED.equals(layerType)) {
            // The `water_supplies` endpoint provides 3 layerTypes we need to delete
            query.in(RealmPlacemark.FIELD_LAYER_TYPE, new String[]{
                    LayerTypes.POOLS,
                    LayerTypes.WADING_POOLS,
                    LayerTypes.PLAY_FOUNTAINS
            });
        } else {
            query.equalTo(RealmPlacemark.FIELD_LAYER_TYPE, layerType);
        }
        query.findAll()
                .deleteAllFromRealm();

        realm.commitTransaction();
    }

    /**
     * Save the downloaded data to the Realm db
     *
     * @param realm
     * @param pointsFeatures
     * @param mapType
     * @param layerType
     * @param transaction
     */
    public static void cacheMapData(Realm realm, List<PointsFeature> pointsFeatures,
                                    @MapType String mapType, @LayerType String layerType,
                                    boolean transaction) {
        if (transaction) {
            realm.beginTransaction();
        }
        // Loop over results, convert GeoJSON to Realm then add to db
        for (PointsFeature feature : pointsFeatures) {
            realm.copyToRealm(new RealmPlacemark.Builder(feature)
                    .mapType(mapType)
                    .layerType(layerType, feature.getProperties().getType())
                    .build());
        }

        if (transaction) {
            realm.commitTransaction();
        }
    }

    /**
     * Get all Placemarks for requested mapType and selected layers
     *
     * @param realm
     * @param mapType
     * @param layers
     * @return
     */
    public static RealmQuery<RealmPlacemark> queryPlacemarksByMapType(Realm realm,
                                                                      @MapType String mapType,
                                                                      @LayerType Set<String> layers) {
        final RealmQuery<RealmPlacemark> query = queryPlacemarksByMapType(realm, mapType);
        if (layers != null) {
            Set<String> filterLayers = null;

            if (Const.MapTypes.HEAT_WAVE.equals(mapType)) {
                filterLayers = new HashSet<>(HEAT_WAVE_LAYERS);
                filterLayers.retainAll(layers);
            } else if (Const.MapTypes.HEALTH.equals(mapType)) {
                filterLayers = new HashSet<>(HEALTH_LAYERS);
                filterLayers.retainAll(layers);
            }

            if (filterLayers != null && filterLayers.size() > 0) {
                return query.in(RealmPlacemark.FIELD_LAYER_TYPE,
                        filterLayers.toArray(new String[filterLayers.size()]));
            }
        }

        return query;
    }

    /**
     * Get all Placemarks for requested mapType
     *
     * @param realm
     * @param mapType
     * @return
     */
    private static RealmQuery<RealmPlacemark> queryPlacemarksByMapType(Realm realm, @MapType String mapType) {
        return realm
                .where(RealmPlacemark.class)
                .equalTo(RealmPlacemark.FIELD_MAP_TYPE, mapType);
    }

    /**
     * Filter RealmResults by the visibleRegion bounding box
     * Deprecated: not used anymore to allow adding all markers immediately. Limiting the results
     * to the bounds means adding multiple markers repeatedly, which blocks the UI thread.
     *
     * @param query
     * @param bounds
     * @return
     */
    @Deprecated
    public static RealmResults<RealmPlacemark> filterPlacemarksQueryByBounds(RealmQuery<RealmPlacemark> query, LatLngBounds bounds) {
        return query
                .greaterThan(RealmPlacemark.FIELD_COORDINATES_LAT, bounds.southwest.latitude)
                .greaterThan(RealmPlacemark.FIELD_COORDINATES_LNG, bounds.southwest.longitude)
                .lessThan(RealmPlacemark.FIELD_COORDINATES_LAT, bounds.northeast.latitude)
                .lessThan(RealmPlacemark.FIELD_COORDINATES_LNG, bounds.northeast.longitude)
                .findAll();
    }

    /**
     * Get all Placemarks with name containing the search-word
     *
     * @param realm
     * @param name
     * @return
     */
    public static RealmQuery<RealmPlacemark> queryPlacemarksByName(Realm realm, String name) {
        return realm
                .where(RealmPlacemark.class)
                .contains(RealmPlacemark.FIELD_PROPERTIES_NAME, String.valueOf(name), Case.INSENSITIVE);
    }
}
