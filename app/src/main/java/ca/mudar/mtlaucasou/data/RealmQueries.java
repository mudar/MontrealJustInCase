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
import ca.mudar.mtlaucasou.model.Placemark;
import ca.mudar.mtlaucasou.model.geojson.PointsFeature;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class RealmQueries {

    /**
     * Save the downloaded data to the Realm db
     *
     * @param pointsFeatures
     * @param mapType
     */
    public static void cacheMapData(Realm realm, List<PointsFeature> pointsFeatures, @MapType String mapType) {
        realm.beginTransaction();
        // Loop over results, convert GeoJSON to Realm then add to db
        for (PointsFeature feature : pointsFeatures) {
            realm.copyToRealm(new Placemark.Builder(feature, mapType).build());
        }
        realm.commitTransaction();
    }

    /**
     * Get all Placemarks for requested mapType
     *
     * @param realm
     * @param mapType
     * @return
     */
    public static RealmQuery<Placemark> queryMapTypePlacemarks(Realm realm, @MapType String mapType) {
        return realm
                .where(Placemark.class)
                .equalTo(Placemark.FIELD_MAP_TYPE, mapType);
    }

    /**
     * Filter RealmResults by the visibleRegion bounding box
     *
     * @param query
     * @param bounds
     * @return
     */
    public static RealmResults<Placemark> filterPlacemarksQueryByBounds(RealmQuery<Placemark> query, LatLngBounds bounds) {
        return query
                .greaterThan(Placemark.FIELD_COORDINATES_LAT, bounds.southwest.latitude)
                .greaterThan(Placemark.FIELD_COORDINATES_LNG, bounds.southwest.longitude)
                .lessThan(Placemark.FIELD_COORDINATES_LAT, bounds.northeast.latitude)
                .lessThan(Placemark.FIELD_COORDINATES_LNG, bounds.northeast.longitude)
                .findAll();
    }
}
