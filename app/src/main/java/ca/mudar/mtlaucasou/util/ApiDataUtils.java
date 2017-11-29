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
import ca.mudar.mtlaucasou.model.LayerType;

import static ca.mudar.mtlaucasou.Const.PrefsNames.ITEM_UPDATED_AT;
import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class ApiDataUtils {
    private static final String TAG = makeLogTag("ApiDataUtils");

    public static String getSharedPrefsKey(String id) {
        return String.format(Locale.ROOT, ITEM_UPDATED_AT, id);
    }

    /**
     * Get the local layerType from the remote dataType
     *
     * @param dataType api values to convert to layerType
     * @return
     */
    @LayerType
    public static String getPlacemarkLayerType(String dataType) {
        if (!TextUtils.isEmpty(dataType)) {
            switch (dataType) {
                case ApiValues.TYPE_PLAY_FOUNTAINS:
                    return LayerType.PLAY_FOUNTAINS;
                case ApiValues.TYPE_WADING_POOLS:
                    return LayerType.WADING_POOLS;
                case ApiValues.TYPE_BEACH:
                case ApiValues.TYPE_POOLS_EXT:
                case ApiValues.TYPE_POOLS_INT:
                    return LayerType.POOLS;
            }
        }

        return null;
    }
}
