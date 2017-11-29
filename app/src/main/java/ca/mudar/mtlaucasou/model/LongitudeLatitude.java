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

import android.arch.persistence.room.Ignore;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class LongitudeLatitude {
    private double lng;
    private double lat;

    @Ignore
    LatLng latLng;

    public LongitudeLatitude() {
        // Empty constructor
    }

    /**
     * Builder's constructor
     *
     * @param builder
     */
    private LongitudeLatitude(Builder builder) {
        this.lng = builder.lng;
        this.lat = builder.lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    /**
     * Build a Realm LongitudeLatitude from GeoJSON Point
     */
    public static class Builder {
        private final double lat;
        private final double lng;

        public Builder(List<Double> point) {
            this.lng = point.get(0);
            this.lat = point.get(1);
        }

        public LongitudeLatitude build() {
            return new LongitudeLatitude(this);
        }
    }
}
