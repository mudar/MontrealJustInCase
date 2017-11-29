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

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ca.mudar.mtlaucasou.ConstDb.Fields;
import ca.mudar.mtlaucasou.ConstDb.Tables;
import ca.mudar.mtlaucasou.model.geojson.Feature;

@Entity(tableName = Tables.POLYGONS,
        indices = {
                @Index(Fields.PLACEMARK_ID)
        },
        foreignKeys = @ForeignKey(
                entity = RoomPlacemark.class,
                parentColumns = Fields.ID,
                childColumns = Fields.PLACEMARK_ID,
                onDelete = ForeignKey.CASCADE
        )
)
public class RoomPolygon {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long placemarkId;
    @MapType
    private String mapType;
    @LayerType
    private String layerType;
    // TODO handle coordinates and holes as Entities
//    private List<LongitudeLatitude> coordinates;
//    private List<List<LongitudeLatitude>> holes;

    public RoomPolygon() {
        // Empty constructor
    }

    private RoomPolygon(Builder builder) {
        if (builder.placemarkId != null) {
            this.placemarkId = builder.placemarkId.hashCode();
        }
        this.mapType = builder.mapType;
        this.layerType = builder.layerType;
    }

    @NonNull
    public long getId() {
        return id;
    }

    public void setId(@NonNull long id) {
        this.id = id;
    }

    public long getPlacemarkId() {
        return placemarkId;
    }

    public void setPlacemarkId(long placemarkId) {
        this.placemarkId = placemarkId;
    }

    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    public String getLayerType() {
        return layerType;
    }

    public void setLayerType(String layerType) {
        this.layerType = layerType;
    }

    public static class Builder {
        private String placemarkId;
        @MapType
        private String mapType;
        @LayerType
        private String layerType;
        private List<LongitudeLatitude> coordinates = new ArrayList<>();
        private List<List<LongitudeLatitude>> holes = new ArrayList<>();

        public Builder(Feature mixedPolygonsFeature) {
            this.placemarkId = mixedPolygonsFeature.getId();
        }

        public Builder mapType(@MapType String mapType) {
            this.mapType = mapType;

            return this;
        }

        public Builder layerType(@LayerType String layerType) {
            this.layerType = layerType;

            return this;
        }

        /**
         * In GeoJSON, a polygon has a single outer ring and multiple inner rings for the holes
         *
         * @param coordinates
         * @return
         */
        public Builder coordinates(List<List<List<Double>>> coordinates) {
            // Outer ring
            for (List<Double> point : coordinates.get(0)) {
                this.coordinates.add(new LongitudeLatitude.Builder(point).build());
            }

            // Other rings/shapes are holes
            final int nbHoles = coordinates.size() - 1;
            if (nbHoles > 0) {
                for (int i = 1; i < nbHoles; i++) {
                    final List<LongitudeLatitude> hole = new ArrayList<>();
                    for (List<Double> point : coordinates.get(i)) {
                        hole.add(new LongitudeLatitude.Builder(point).build());
                    }
                    this.holes.add(hole);
                }
            }

            return this;
        }

        public RoomPolygon build() {
            return new RoomPolygon(this);
        }
    }
}
