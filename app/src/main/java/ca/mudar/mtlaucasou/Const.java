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

package ca.mudar.mtlaucasou;

import com.google.android.gms.maps.model.LatLng;

public class Const {
    /**
     * MTL au cas ou
     */
    public static final float MONTREAL_NATURAL_NORTH_ROTATION = -34f;
    public static final float ZOOM_DEFAULT = 15;
    public static final LatLng MONTREAL_GEO_LAT_LNG = new LatLng(45.5d, -73.666667d);

    public static final double MAPS_GEOCODER_LIMITS[] = {
            45.380127d, // lowerLeftLat
            -73.982620d, // lowerLeftLng
            45.720444d, // upperRightLat
            -73.466087d
    };

    /**
     * GeoJSON OpenData API
     */
    public interface ApiPaths {
        // String BASE_URL = BuildConfig.API_BASE_URL; // "http://www.montrealaucasou.com/api/"
        String GET_FIRE_HALLS = "fire_halls.json";
        String GET_SPVM_STATIONS = "spvm_stations.json";
        String GET_WATER_SUPPLIES = "water_supplies.json";
        String GET_EMERGENCY_HOSTELS = "emergency_hostes.json";
    }

    /**
     * Minimum distance to center map on user location, otherwise center on
     * downtown. Units are meters.
     */
    public static final int MAPS_MIN_DISTANCE = 25000;

    public interface UnitsDisplay {
        float FEET_PER_MILE = 5280f;
        float METER_PER_MILE = 1609.344f;
        int ACCURACY_FEET_FAR = 100;
        int ACCURACY_FEET_NEAR = 10;
        int MIN_FEET = 200;
        int MIN_METERS = 100;
    }


    public static interface PrefsNames {
        final String HAS_LOADED_DATA = "prefs_has_loaded_data";
        final String VERSION_DATABASE = "prefs_version_database";
        final String LANGUAGE = "prefs_language";
        final String UNITS_SYSTEM = "prefs_units_system";
        final String LIST_SORT = "prefs_list_sort_by";
        final String FOLLOW_LOCATION_CHANGES = "prefs_follow_location_changes";
        final String LAST_UPDATE_TIME = "prefs_last_update_time";
        final String LAST_UPDATE_LAT = "prefs_last_update_lat";
        final String LAST_UPDATE_LNG = "prefs_last_update_lng";

    }

    public static interface PrefsValues {
        final String LANG_FR = "fr";
        final String LANG_EN = "en";
        final String UNITS_ISO = "iso";
        final String UNITS_IMP = "imp";
        final String LIST_SORT_NAME = "name";
        final String LIST_SORT_DISTANCE = "distance";
    }

    /**
     * Other constants
     */
    public static final int UNKNOWN_VALUE = -1;
    public static final long ANIM_SHORT_DURATION = 200L;
    public static final long ANIM_MEDIUM_DURATION = 400L;
    public static final long ANIM_LONG_DURATION = 500L;
}
