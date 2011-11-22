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

package ca.mudar.mtlaucasou.ui.widgets;

import ca.mudar.mtlaucasou.provider.PlacemarkContract.PlacemarkColumns;
import ca.mudar.mtlaucasou.utils.Helper;
import ca.mudar.mtlaucasou.R;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;

import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PlacemarksCursorAdapter extends SimpleCursorAdapter {
    protected static final String TAG = "PlacemarksCursorAdapter";

    private Location mLocation;

    public PlacemarksCursorAdapter(Context context, int layout, Cursor c, String[] from,
            int[] to, int flags) {
        // TODO: flags is a deprecated argument
        super(context, layout, c, from, to);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return super.newView(context, cursor, parent);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        /**
         * Can't calculate the distance if we don't know the current location
         */
        if (mLocation == null) {
            return;
        }

        // Resources res = context.getResources();
        TextView vDistance = (TextView) view.findViewById(R.id.placemark_distance);

        Double geoLat = cursor.getDouble(cursor.getColumnIndex(PlacemarkColumns.PLACEMARK_GEO_LAT));
        Double geoLng = cursor.getDouble(cursor.getColumnIndex(PlacemarkColumns.PLACEMARK_GEO_LNG));

        float[] results = new float[1];
        android.location.Location.distanceBetween(geoLat, geoLng,
                (mLocation.getLatitude()),
                (mLocation.getLongitude()), results);

        float fDistance = (results[0]);

        // Log.v(TAG, "geoLat = "+ geoLat + "geoLng = "+ geoLng );
        String sDistance = Helper.getDistanceDisplay(context, fDistance);

        vDistance.setText(sDistance);
    }

    public void setLocation(Location mLocation) {
        this.mLocation = mLocation;
    }
}
