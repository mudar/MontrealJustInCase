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

package ca.mudar.mtlaucasou.model;

import ca.mudar.mtlaucasou.model.geojson.PointsFeature;
import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;

@RealmClass
public class Placemark implements RealmModel {
    @Ignore
    public static final String FIELD_MAP_TYPE = "mapType";
    @Ignore
    public static final String FIELD_COORDINATES = "coordinates";
    @Ignore
    public static final String FIELD_COORDINATES_LAT = "coordinates.lat";
    @Ignore
    public static final String FIELD_COORDINATES_LNG = "coordinates.lng";
    @Ignore
    public static final String FIELD_PROPERTIES_NAME = "properties.name";

    private String id;
    @MapType
    @Index
    private String mapType;
    private PlacemarkProperties properties;
    private LongitudeLatitude coordinates;

    public Placemark() {
        // Empty constructor
    }

    /**
     * Builder's constructor
     *
     * @param builder
     */
    private Placemark(Builder builder) {
        this.id = builder.id;
        this.mapType = builder.mapType;
        this.properties = builder.properties;
        this.coordinates = builder.coordinates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    public PlacemarkProperties getProperties() {
        return properties;
    }

    public void setProperties(PlacemarkProperties properties) {
        this.properties = properties;
    }

    public LongitudeLatitude getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LongitudeLatitude coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Build a Realm Placemark from GeoJSON PointsFeature
     */
    public static class Builder {
        private String id;
        @MapType
        private String mapType;
        private PlacemarkProperties properties;
        private LongitudeLatitude coordinates;

        public Builder(PointsFeature pointsFeature, @MapType String mapType) {
            this.id = pointsFeature.getId();
            this.mapType = mapType;

            this.properties = new PlacemarkProperties.Builder(pointsFeature.getProperties())
                    .build();
            this.coordinates = new LongitudeLatitude.Builder(pointsFeature.getGeometry())
                    .build();
        }

        public Placemark build() {
            return new Placemark(this);
        }
    }
}
