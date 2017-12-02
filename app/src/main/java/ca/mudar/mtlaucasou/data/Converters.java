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

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import ca.mudar.mtlaucasou.model.LongitudeLatitude;

public class Converters {
    @TypeConverter
    public ArrayList<LongitudeLatitude> fromLngLat(String value) {
        Type listType = new TypeToken<ArrayList<LongitudeLatitude>>() {
        }.getType();

        return value == null ? null : (ArrayList<LongitudeLatitude>) new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public String stringToLngLat(ArrayList<LongitudeLatitude> list) {
        if (list == null) {
            return null;
        } else {
            return new GsonBuilder().create().toJson(list);
        }
    }

    @TypeConverter
    public ArrayList<ArrayList<LongitudeLatitude>> fromLngLatList(String value) {
        Type listType = new TypeToken<ArrayList<ArrayList<LongitudeLatitude>>>() {
        }.getType();

        return value == null ? null : (ArrayList<ArrayList<LongitudeLatitude>>) new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public String stringToLngLatList(ArrayList<ArrayList<LongitudeLatitude>> list) {
        if (list == null) {
            return null;
        } else {
            return new GsonBuilder().create().toJson(list);
        }
    }
}
