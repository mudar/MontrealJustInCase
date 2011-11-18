
package ca.mudar.mtlaucasou.utils;

public class Const {

    public static final String APP_PREFS_NAME = "MTL_JUSTINCASE_PREFS";
    
    public static interface PrefsNames {
        final String HAS_LOADED_DATA = "prefs_has_loaded_data";
        final String LANGUAGE = "prefs_lang";
        final String UNITS_SYSTEM = "prefs_units_system";
        final String SORT_ORDER = "prefs_sort_order";
    }
    
    public static final String MAPS_DEFAULT_COORDINATES[] = {
            "45.5", "-73.666667"
    };
    public static final int MAPS_MIN_DISTANCE = 25; // Distance in KM
    
//    public static final String INTENT_EXTRA_NAME_SECTION = "section";
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
//    public static final String KEY_INSTANCE_IS_VISIBLE_MAP = "is_visible_map";
    
    public static interface KmlRemoteUrls {
        final String FIRE_HALLS = "http://depot.ville.montreal.qc.ca/casernes-pompiers/data.kml";
        final String SPVM_STATIONS = "http://depot.ville.montreal.qc.ca/carte-postes-quartier/data.kml";
        final String WATER_SUPPLIES = "http://depot.ville.montreal.qc.ca/points-eau/data.kml";
        final String EMERGENCY_HOSTELS = "http://depot.ville.montreal.qc.ca/centres-hebergement-urgence/data.kml";
    }
}
