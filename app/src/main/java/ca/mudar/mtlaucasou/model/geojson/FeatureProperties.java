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

package ca.mudar.mtlaucasou.model.geojson;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ca.mudar.mtlaucasou.Const;

public class FeatureProperties {
    @SerializedName("Name")
    private String name;
    @SerializedName("Description")
    private String description;
    @SerializedName("Address")
    private String address;
    @SerializedName("City")
    private String city;
    @SerializedName("Phone")
    private String phone;
    @SerializedName("Website")
    private String website;
    @SerializedName("Type")
    private String type;
    @SerializedName("Types")
    private List<String> types;

    public String getName() {
        return name;
    }

    /**
     * Returns the address, with city if available.
     * If not, returns the Description field.
     *
     * @return The address
     */
    public String getDescription() {
        return description;
    }

    public String getAddress() {
        if (!TextUtils.isEmpty(address)) {
            if (TextUtils.isEmpty(city)) {
                return description;
            } else {
                return address + Const.HTML_LINE_SEPARATOR + city;
            }
        } else {
            return description;
        }
    }

    public String getCity() {
        return city;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return website;
    }

    public String getType() {
        return type;
    }

    public List<String> getTypes() {
        return types;
    }
}
