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

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.model.MapType;

public class NavigUtils {

    /**
     * Get the bottomBar tabId for the selected SuggestionPlacemark, allowing to switch tabs
     * when showing the placemark.
     *
     * @param type Selected map type {fire_halls|spvm_stations|water_supplies|emergency_hostels|hospitals}
     * @return tabId
     */
    @IdRes
    public static int getTabIdByMapType(@MapType String type) {
        switch (type) {
            case MapType.FIRE_HALLS:
                return R.id.tab_fire_halls;
            case MapType.SPVM_STATIONS:
                return R.id.tab_spvm;
            case MapType.HEAT_WAVE:
                return R.id.tab_water_supplies;
            case MapType.EMERGENCY_HOSTELS:
                return R.id.tab_emergency_hostels;
            case MapType.HEALTH:
                return R.id.tab_hospitals;
        }

        return 0;
    }

    /**
     * Get the map type when user switches tabs in the bottomBar
     *
     * @param tabId the selected bottomBar tabId
     * @return Selected map type {fire_halls|spvm_stations|water_supplies|emergency_hostels|hospitals}
     */
    @MapType
    public static String getMapTypeByTabId(int tabId) {
        switch (tabId) {
            case R.id.tab_fire_halls:
                return MapType.FIRE_HALLS;
            case R.id.tab_spvm:
                return MapType.SPVM_STATIONS;
            case R.id.tab_water_supplies:
                return MapType.HEAT_WAVE;
            case R.id.tab_emergency_hostels:
                return MapType.EMERGENCY_HOSTELS;
            case R.id.tab_hospitals:
                return MapType.HEALTH;
        }

        return null;
    }

    /**
     * Get the BottomBar and SearchSuggestions icon (logos)
     *
     * @param type Selected map type {fire_halls|spvm_stations|water_supplies|emergency_hostels|hospitals}
     * @return map type resource icon
     */
    @DrawableRes
    public static int getMapTypeIcon(@MapType String type) {
        switch (type) {
            case MapType.FIRE_HALLS:
                return R.drawable.ic_fire_hall;
            case MapType.SPVM_STATIONS:
                return R.drawable.ic_spvm;
            case MapType.HEAT_WAVE:
                return R.drawable.ic_water_supplies;
            case MapType.EMERGENCY_HOSTELS:
                return R.drawable.ic_emergency_hostels;
            case MapType.HEALTH:
                return R.drawable.ic_hospitals;
        }
        return 0;
    }
}
