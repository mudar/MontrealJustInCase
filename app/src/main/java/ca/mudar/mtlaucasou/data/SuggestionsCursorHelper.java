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

package ca.mudar.mtlaucasou.data;

import android.database.Cursor;
import android.database.MatrixCursor;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import ca.mudar.mtlaucasou.model.LayerType;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.Placemark;
import ca.mudar.mtlaucasou.model.SuggestionsPlacemark;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class SuggestionsCursorHelper {
    private static final String TAG = makeLogTag("SuggestionsCursorHelper");

    /**
     * Convert ArrayList into a MatrixCursor. Needed for the SearchView which supports Cursors only.
     *
     * @param data
     * @return
     */
    public static MatrixCursor initCursor(List<SuggestionsPlacemark> data) {
        if (data == null || data.size() == 0) {
            return null;
        }

        final MatrixCursor matrixCursor = new MatrixCursor(PlacemarksQuery.PROJECTION, data.size());

        int i = 0;
        for (SuggestionsPlacemark place : data) {
            matrixCursor.addRow(placeToCursorObject(i++, place));
        }
        return matrixCursor;
    }

    /**
     * Build a cursor row object from a Placemark
     *
     * @param id    The incremental ID
     * @param place The placemark object
     * @return
     */
    public static Object[] placeToCursorObject(int id, SuggestionsPlacemark place) {
        return new Object[]{
                id,
                place.getName(),
                place.getMapType(),
                place.getLayerType(),
                place.getLatLng().latitude,
                place.getLatLng().longitude
        };
    }

    /**
     * Move the cursor to required position and convert the cursor row at that position
     * to a Placemark object
     *
     * @param cursor
     * @param position
     * @return
     */
    public static Placemark cursorObjectToPlace(Cursor cursor, int position) {
        if (cursor.moveToPosition(position)) {
            return cursorObjectToPlace(cursor);
        } else {
            return null;
        }
    }

    /**
     * Convert the cursor row (at current position) to a Placemark object
     *
     * @param cursor
     * @return
     */
    public static Placemark cursorObjectToPlace(Cursor cursor) {
        final String name = cursor.getString(PlacemarksQuery.TITLE);
        final LatLng position = new LatLng(
                cursor.getDouble(PlacemarksQuery.LATITUDE),
                cursor.getDouble(PlacemarksQuery.LONGITUDE));
        final @MapType String mapType = cursor.getString(PlacemarksQuery.MAP_TYPE);
        final @LayerType String layerType = cursor.getString(PlacemarksQuery.LAYER_TYPE);

        return new SuggestionsPlacemark.Builder()
                .name(name)
                .latlng(position)
                .maptype(mapType)
                .layertype(layerType)
                .build();
    }

    /**
     * Get the Placemark's name from the cursor's current row
     *
     * @param cursor
     * @return
     */
    public static String getPlacemarkName(Cursor cursor) {
        return cursor.getString(PlacemarksQuery.TITLE);
    }

    public interface PlacemarksQuery {
        String[] PROJECTION = {
                "_id",
                "title",
                "mapType",
                "layerType",
                "latitude",
                "longitude",
        };

        // Column indexes, must correspond to above order
        int _ID = 0;
        int TITLE = 1;
        int MAP_TYPE = 2;
        int LAYER_TYPE = 3;
        int LATITUDE = 4;
        int LONGITUDE = 5;
    }
}
