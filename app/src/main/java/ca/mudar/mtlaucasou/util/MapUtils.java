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

package ca.mudar.mtlaucasou.util;

import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;

public class MapUtils {
    public static BitmapDescriptor getMarkerIcon(Const.MapTypes type) {
        switch (type) {
            case FIRE_HALLs:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_fire_halls);
            case SVPM_STATIONS:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_spvm);
            case WATER_SUPPLIES:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_water_supplies);
            case EMERGENCY_HOSTELS:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_emergency_hostels);
        }
        return null;
    }

    public static String getCleanDescription(@NonNull String descHtml, @NonNull String name) {
        if (TextUtils.isEmpty(descHtml)) {
            return null;
        }
        return Html.fromHtml(descHtml.replace(name, ""))
                .toString()
                .trim();
    }
}
