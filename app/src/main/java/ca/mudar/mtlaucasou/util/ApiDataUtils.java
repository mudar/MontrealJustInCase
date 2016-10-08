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

import android.text.TextUtils;

import java.util.Locale;

import ca.mudar.mtlaucasou.Const.ApiValues;
import ca.mudar.mtlaucasou.Const.LayerTypes;
import ca.mudar.mtlaucasou.Const.MapTypes;
import ca.mudar.mtlaucasou.model.LayerType;
import ca.mudar.mtlaucasou.model.MapType;

import static ca.mudar.mtlaucasou.Const.PrefsNames.ITEM_UPDATED_AT;

public class ApiDataUtils {
    public static String getSharedPrefsKey(String id) {
        return String.format(Locale.ROOT, ITEM_UPDATED_AT, id);
    }

    /**
     * Get the local layerType from the remote dataType
     *
     * @param dataType api values to convert to layerType
     * @param mapType  fallback value, used for single-layer datasets
     * @return
     */
    @LayerType
    public static String getLayerType(String dataType, @MapType String mapType) {
        if (!TextUtils.isEmpty(dataType)) {
            switch (dataType) {
                case ApiValues.TYPE_PLAY_FOUNTAINS:
                    return LayerTypes.PLAY_FOUNTAINS;
                case ApiValues.TYPE_WADING_POOLS:
                    return LayerTypes.WADING_POOLS;
                case ApiValues.TYPE_POOLS_EXT:
                case ApiValues.TYPE_POOLS_INT:
                    return LayerTypes.POOLS;
                case ApiValues.TYPE_HOSPITALS:
                    return LayerTypes.HOSPITALS;
                case ApiValues.TYPE_CLSC:
                    return LayerTypes.CLSC;
            }
        }

        if (!TextUtils.isEmpty(mapType)) {
            // These 3 mapTypes have a single layer type, the same as the mapType
            switch (mapType) {
                case MapTypes.FIRE_HALLS:
                    return LayerTypes.FIRE_HALLS;
                case MapTypes.SPVM_STATIONS:
                    return LayerTypes.SPVM_STATIONS;
                case MapTypes.EMERGENCY_HOSTELS:
                    return LayerTypes.EMERGENCY_HOSTELS;
            }
        }

        return null;
    }
}
