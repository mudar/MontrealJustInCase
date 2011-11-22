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

package ca.mudar.mtlaucasou.utils;

public class Const {

    public static final String APP_PREFS_NAME = "MTL_JUSTINCASE_PREFS";

    public static interface PrefsNames {
        final String HAS_LOADED_DATA = "prefs_has_loaded_data";
        final String VERSION_DATABASE = "prefs_version_database";
        final String LANGUAGE = "prefs_language";
        final String UNITS_SYSTEM = "prefs_units_system";
        final String LIST_SORT = "prefs_list_sort";
    }
    
    public static interface PrefsValues {
        final String LANG_FR = "fr";
        final String LANG_EN = "en";
        final String UNITS_ISO = "iso";
        final String UNITS_IMP = "imp";
        final String LIST_SORT_NAME = "name";
        final String LIST_SORT_DISTANCE = "distance";
    }

    public static final String MAPS_DEFAULT_COORDINATES[] = {
            "45.5", "-73.666667"
    };
    public static final int MAPS_MIN_DISTANCE = 25; // Distance in KM
    
    public static interface UnitsDisplay {
        final float FEET_PER_MILE = 5280;
        final float METER_PER_MILE = 1609.344f;
        final int ACCURACY_FEET_FAR = 100;
        final int ACCURACY_FEET_NEAR = 10;
        final int MIN_FEET = 200;
        final int MIN_METERS = 100;
    }

    // public static final String INTENT_EXTRA_NAME_SECTION = "section";
    public static final String INTENT_EXTRA_GEO_LAT = "geo_lat";
    public static final String INTENT_EXTRA_GEO_LNG = "geo_lng";

    public static final int MENU_ITEM_GROUP_ID = 0x1;
    public static final int MENU_ITEM_ORDER = 0x1;

    public static final int INDEX_ACTIVITY_FIRE_HALLS = 0x1;
    public static final int INDEX_ACTIVITY_SPVM_STATIONS = 0x2;
    public static final int INDEX_ACTIVITY_WATER_SUPPLIES = 0x3;
    public static final int INDEX_ACTIVITY_EMERGENCY_HOSTELS = 0x4;

    public static final String TAG_FRAGMENT_LIST = "tag_fragment_list";
    public static final String TAG_FRAGMENT_MAP = "tag_fragment_map";

    public static final String KEY_INSTANCE_COORDS = "map_coordinates";
    public static final String KEY_INSTANCE_ZOOM = "map_zoom";
    public static final String KEY_INSTANCE_LIST_IS_HIDDEN = "list_is_hidden";

    // public static final String KEY_INSTANCE_IS_VISIBLE_MAP =
    // "is_visible_map";

    public static interface KmlRemoteUrls {
        final String FIRE_HALLS = "http://depot.ville.montreal.qc.ca/casernes-pompiers/data.kml";
        final String SPVM_STATIONS = "http://depot.ville.montreal.qc.ca/carte-postes-quartier/data.kml";
        final String WATER_SUPPLIES = "http://depot.ville.montreal.qc.ca/points-eau/data.kml";
        final String EMERGENCY_HOSTELS = "http://depot.ville.montreal.qc.ca/centres-hebergement-urgence/data.kml";
    }
}
