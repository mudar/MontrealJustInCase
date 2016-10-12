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

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * Realm doesn't allow access to objects on Worker/UI threads, so we need to convert
 * to non-realm objects.
 * This allows handling realm calls in performFiltering() and then accessing data onBindView().
 * And cleaner calls to realm.close()
 */
public class SuggestionsPlacemark implements
        Placemark,
        Comparable<SuggestionsPlacemark> {

    String name;
    LatLng latLng;
    @MapType
    String mapType;
    @LayerType
    String layerType;

    public SuggestionsPlacemark() {
        // Empty constructor
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public LatLng getLatLng() {
        return latLng;
    }

    @Override
    @MapType
    public String getMapType() {
        return mapType;
    }

    @Override
    @LayerType
    public String getLayerType() {
        return layerType;
    }

    private SuggestionsPlacemark(Builder builder) {
        this.name = builder.name;
        this.latLng = builder.latLng;
        this.mapType = builder.mapType;
        this.layerType = builder.layerType;
    }

    @Override
    public int compareTo(@NonNull SuggestionsPlacemark other) {
        return name.compareTo(other.name);
    }

    public static class Builder {

        String name;
        LatLng latLng;
        @MapType
        String mapType;
        @LayerType
        String layerType;

        public Builder() {
        }

        public Builder placemark(Placemark placemark) {
            this.name = placemark.getName();
            this.latLng = placemark.getLatLng();
            this.mapType = placemark.getMapType();
            this.layerType = placemark.getLayerType();

            return this;
        }

        public Builder name(String name) {
            this.name = name;

            return this;
        }

        public Builder latlng(LatLng latLng) {
            this.latLng = latLng;

            return this;
        }

        public Builder maptype(@MapType String mapType) {
            this.mapType = mapType;

            return this;
        }

        public Builder layertype(@LayerType String layerType) {
            this.layerType = layerType;

            return this;
        }

        public SuggestionsPlacemark build() {
            return new SuggestionsPlacemark(this);
        }

    }
}
