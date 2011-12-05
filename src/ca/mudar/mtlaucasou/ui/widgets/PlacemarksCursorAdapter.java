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

import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.PlacemarkColumns;
import ca.mudar.mtlaucasou.utils.Helper;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PlacemarksCursorAdapter extends SimpleCursorAdapter {
    protected static final String TAG = "PlacemarksCursorAdapter";

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

        int distance = cursor.getInt(cursor
                .getColumnIndexOrThrow(PlacemarkColumns.PLACEMARK_DISTANCE));
        String sDistance = (distance > 0 ? Helper.getDistanceDisplay(context, distance) : "");

        ((TextView) view.findViewById(R.id.placemark_distance)).setText(sDistance);
    }
}
