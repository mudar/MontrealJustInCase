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

import android.support.annotation.IdRes;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.model.MapType;

public class NavigUtils {

    @IdRes
    public static int getTabIdByMapType(@MapType String type) {
        switch (type) {
            case Const.MapTypes.FIRE_HALLS:
                return R.id.tab_fire_halls;
            case Const.MapTypes.SVPM_STATIONS:
                return R.id.tab_spvm;
            case Const.MapTypes.WATER_SUPPLIES:
                return R.id.tab_water_supplies;
            case Const.MapTypes.EMERGENCY_HOSTELS:
                return R.id.tab_emergency_hostels;
        }

        return 0;
    }

    @MapType
    public static String getMapTypeByTabId(int tabId) {
        switch (tabId) {
            case R.id.tab_fire_halls:
                return Const.MapTypes.FIRE_HALLS;
            case R.id.tab_spvm:
                return Const.MapTypes.SVPM_STATIONS;
            case R.id.tab_water_supplies:
                return Const.MapTypes.WATER_SUPPLIES;
            case R.id.tab_emergency_hostels:
                return Const.MapTypes.EMERGENCY_HOSTELS;
        }

        return null;
    }
}
