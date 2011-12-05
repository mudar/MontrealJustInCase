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

import android.app.AlarmManager;

public class Const {

    public static final String APP_PREFS_NAME = "MTL_JUSTINCASE_PREFS";

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

    public static final double MAPS_DEFAULT_COORDINATES[] = {
            45.5d, -73.666667d
    };

    public static final double MAPS_GEOCODER_LIMITS[] = {
            45.380127d, // lowerLeftLat
            -73.982620d, // lowerLeftLng
            45.720444d, // upperRightLat
            -73.466087d
    };

    /**
     * Minimum distance to center map on user location, otherwise center on
     * downtown. Units are meters.
     */
    public static final int MAPS_MIN_DISTANCE = 25000;

    public static interface UnitsDisplay {
        final float FEET_PER_MILE = 5280f;
        final float METER_PER_MILE = 1609.344f;
        final int ACCURACY_FEET_FAR = 100;
        final int ACCURACY_FEET_NEAR = 10;
        final int MIN_FEET = 200;
        final int MIN_METERS = 100;
    }

    public static final String INTENT_EXTRA_GEO_LAT = "geo_lat";
    public static final String INTENT_EXTRA_GEO_LNG = "geo_lng";
    public static final String INTENT_EXTRA_CONTENT_URI = "content_uri";
    public static final String INTENT_EXTRA_FORCE_UPDATE = "force_update";

    public static final int INDEX_ACTIVITY_FIRE_HALLS = 0x1;
    public static final int INDEX_ACTIVITY_SPVM_STATIONS = 0x2;
    public static final int INDEX_ACTIVITY_WATER_SUPPLIES = 0x3;
    public static final int INDEX_ACTIVITY_EMERGENCY_HOSTELS = 0x4;

    public static final String TAG_FRAGMENT_LIST = "tag_fragment_list";
    public static final String TAG_FRAGMENT_MAP = "tag_fragment_map";

    public static final String KEY_INSTANCE_COORDS = "map_coordinates";
    public static final String KEY_INSTANCE_ZOOM = "map_zoom";
    public static final String KEY_INSTANCE_LIST_IS_HIDDEN = "list_is_hidden";
    public static final String KEY_BUNDLE_PROGRESS_INCREMENT = "bundle_progress_increment";

    public static final String KEY_BUNDLE_SEARCH_ADDRESS = "bundle_search_address";

    public static final int BUNDLE_SEARCH_ADDRESS_SUCCESS = 0x1;
    public static final int BUNDLE_SEARCH_ADDRESS_ERROR = 0x0;

    public static final String KEY_BUNDLE_ADDRESS_LAT = "bundle_address_lat";
    public static final String KEY_BUNDLE_ADDRESS_LNG = "bundle_address_lng";

    public static final String LOCATION_PROVIDER = "my_default_provider";

    public static interface KmlRemoteUrls {
        final String FIRE_HALLS = "http://depot.ville.montreal.qc.ca/casernes-pompiers/data.kml";
        final String SPVM_STATIONS = "http://depot.ville.montreal.qc.ca/carte-postes-quartier/data.kml";
        final String WATER_SUPPLIES = "http://depot.ville.montreal.qc.ca/points-eau/data.kml";
        final String EMERGENCY_HOSTELS = "http://depot.ville.montreal.qc.ca/centres-hebergement-urgence/data.kml";
        final String CONDITIONED_PLACES = "http://depot.ville.montreal.qc.ca/lieux-publics-climatises/data.kml";
    }
    
    public static interface KmlLocalAssets {
        final String FIRE_HALLS = "casernes-pompiers.kml";
        final String SPVM_STATIONS = "carte-postes-quartier.kml";
        final String WATER_SUPPLIES = "points-eau.kml";
        final String EMERGENCY_HOSTELS = "centres-hebergement-urgence.kml";
        final String CONDITIONED_PLACES = "lieux-publics-climatises.kml";
    }
    
    public static boolean SUPPORTS_HONEYCOMB = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
    public static boolean SUPPORTS_GINGERBREAD = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
    public static boolean SUPPORTS_FROYO = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;

    /**
     * Location constants.
     * Copied from com.radioactiveyak.location_best_practices
     */
    public static String EXTRA_KEY_IN_BACKGROUND = "extra_key_in_background";
    
    // The default search radius when searching for places nearby.
    public static int DEFAULT_RADIUS = 150;
    // The maximum distance the user should travel between location updates. 
    public static int MAX_DISTANCE = DEFAULT_RADIUS/2;
    // The maximum time that should pass before the user gets a location update.
    public static long MAX_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    public static int DB_MAX_DISTANCE = MAX_DISTANCE/2;
    
    // You will generally want passive location updates to occur less frequently
    // than active updates. You need to balance location freshness with battery
    // life.
    // The location update distance for passive updates.
    public static int PASSIVE_MAX_DISTANCE = MAX_DISTANCE;
    // The location update time for passive updates
    public static long PASSIVE_MAX_TIME = AlarmManager.INTERVAL_HALF_DAY;
    // Use the GPS (fine location provider) when the Activity is visible?
    public static boolean USE_GPS_WHEN_ACTIVITY_VISIBLE = true;
    // When the user exits via the back button, do you want to disable
    // passive background updates.
    public static boolean DISABLE_PASSIVE_LOCATION_WHEN_USER_EXIT = false;

    public static String ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED = "ca.mudar.mtlaucasou.data.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED";

}
