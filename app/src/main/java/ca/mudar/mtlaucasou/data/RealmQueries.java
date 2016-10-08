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

import java.util.List;

import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.RealmPlacemark;
import ca.mudar.mtlaucasou.model.geojson.PointsFeature;
import ca.mudar.mtlaucasou.model.LayerType;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class RealmQueries {

    /**
     * Delete data from the Realm db
     *
     * @param realm
     * @param layerType
     */
    public static void clearMapData(Realm realm, @LayerType String layerType) {
        realm.beginTransaction();

        realm.where(RealmPlacemark.class)
                .equalTo(RealmPlacemark.FIELD_LAYER_TYPE, layerType)
                .findAll()
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
                                    @MapType String mapType, @LayerType String layerType, boolean transaction) {
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
     * Get all Placemarks for requested mapType
     *
     * @param realm
     * @param mapType
     * @return
     */
    public static RealmQuery<RealmPlacemark> queryPlacemarksByMapType(Realm realm, @MapType String mapType) {
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
