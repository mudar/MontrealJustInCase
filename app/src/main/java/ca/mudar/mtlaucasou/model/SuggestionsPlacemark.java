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

    public SuggestionsPlacemark(String name, LatLng latLng, @MapType String mapType) {
        this.name = name;
        this.latLng = latLng;
        this.mapType = mapType;
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

    private SuggestionsPlacemark(Builder builder) {
        this.name = builder.name;
        this.latLng = builder.latLng;
        this.mapType = builder.mapType;
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

        public Builder(Placemark placemark) {
            this.name = placemark.getName();
            this.latLng = placemark.getLatLng();
            this.mapType = placemark.getMapType();
        }

        public SuggestionsPlacemark build() {
            return new SuggestionsPlacemark(this);
        }

    }
}
