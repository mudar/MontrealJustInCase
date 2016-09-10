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

import ca.mudar.mtlaucasou.model.geojson.FeatureProperties;
import io.realm.RealmObject;

public class PlacemarkProperties extends RealmObject {
    private String name;
    private String description;

    public PlacemarkProperties() {
        // Empty constructor
    }

    /**
     * Builder's constructor
     *
     * @param builder
     */
    private PlacemarkProperties(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Build a Realm PlacemarkProperties from GeoJSON FeatureProperties
     */
    public static class Builder {
        private final String name;
        private final String description;

        public Builder(FeatureProperties properties) {
            this.name = properties.getName();
            this.description = properties.getDescription();
        }

        public PlacemarkProperties build() {
            return new PlacemarkProperties(this);
        }
    }
}