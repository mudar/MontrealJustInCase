/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications:
 * - Copied from IOSched
 * - Renamed package
 * - Removed almost everything!
 */

package ca.mudar.mtlaucasou.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for interacting with {@link PlacemarkProvider}. Unless
 * otherwise noted, all time-based fields are milliseconds since epoch and can
 * be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link Uri}
 * are generated using stronger {@link String} identifiers, instead of
 * {@code int} {@link BaseColumns#_ID} values, which are prone to shuffle during
 * sync.
 */
public class PlacemarkContract {
    private static final String TAG = "PlacemarkContract";
    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that an entry
     * has never been updated, or doesn't exist yet.
     */
    public static final long UPDATED_NEVER = -2;

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that the last
     * update time is unknown, usually when inserted from a local file source.
     */
    public static final long UPDATED_UNKNOWN = -1;

    public static interface SyncColumns {
        /** Last time this entry was updated or synchronized. */
        final String UPDATED = "updated";
    }

    public static interface PlacemarkColumns {
        final String PLACEMARK_ID = "placemark_id";
        final String PLACEMARK_NAME = "placemark_name";
        final String PLACEMARK_DESCRIPTION = "placemark_description";
        final String PLACEMARK_ADDRESS = "placemark_address";
        final String PLACEMARK_GEO_LAT = "placemark_geo_lat";
        final String PLACEMARK_GEO_LNG = "placemark_geo_lng";
        final String PLACEMARK_DISTANCE = "placemark_distance";
    }

    public static final String CONTENT_AUTHORITY = "ca.mudar.mtlaucasou.data";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_FIRE_HALLS = "fire_halls";
    private static final String PATH_SPVM_STATIONS = "spvm_stations";
    private static final String PATH_WATER_SUPPLIES = "water_supplies";
    private static final String PATH_EMERGENCY_HOSTELS = "emergency_hostels";
    private static final String PATH_CONDITIONED_PLACES = "conditioned_places";

    public static class FireHalls implements PlacemarkColumns, SyncColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FIRE_HALLS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mtlaucasou.fire_hall";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mtlaucasou.fire_hall";

        public static final String DEFAULT_SORT = PlacemarkColumns.PLACEMARK_NAME + " ASC ";

        public static Uri buildFireHallUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getFireHallId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class SpvmStations implements PlacemarkColumns, SyncColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SPVM_STATIONS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mtlaucasou.spvm_station";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mtlaucasou.spvm_station";

        public static final String DEFAULT_SORT = PlacemarkColumns.PLACEMARK_NAME + " ASC ";

        public static Uri buildSpvmStationUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getSpvmStationId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class WaterSupplies implements PlacemarkColumns, SyncColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_WATER_SUPPLIES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mtlaucasou.water_supply";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mtlaucasou.water_supply";

        public static final String DEFAULT_SORT = PlacemarkColumns.PLACEMARK_NAME + " ASC ";

        public static Uri buildWaterSupplyUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getWaterSupplyId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class EmergencyHostels implements PlacemarkColumns, SyncColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_EMERGENCY_HOSTELS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mtlaucasou.emergency_hostel";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mtlaucasou.emergency_hostel";

        public static final String DEFAULT_SORT = PlacemarkColumns.PLACEMARK_NAME + " ASC ";

        public static Uri buildEmergencyHostelUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getEmergencyHostelId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class ConditionedPlaces implements PlacemarkColumns, SyncColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_CONDITIONED_PLACES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mtlaucasou.conditioned_place";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mtlaucasou.conditioned_place";

        public static final String DEFAULT_SORT = PlacemarkColumns.PLACEMARK_NAME + " ASC ";

        public static Uri buildConditionedPlaceUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getConditionedPlaceId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

}
