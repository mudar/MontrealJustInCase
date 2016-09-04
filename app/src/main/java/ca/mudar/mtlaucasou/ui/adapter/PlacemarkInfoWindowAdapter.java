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

package ca.mudar.mtlaucasou.ui.adapter;

import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import ca.mudar.mtlaucasou.R;

public class PlacemarkInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mView;

    public PlacemarkInfoWindowAdapter(View view) {
        this.mView = view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        final String title = marker.getTitle();
        final String snippet = marker.getSnippet();
        final TextView vTitle = (TextView) mView.findViewById(R.id.title);
        final TextView vSnippet = ((TextView) mView.findViewById(R.id.snippet));

        if (title != null) {
            vTitle.setText(title);
        }

        if (snippet != null) {
            vSnippet.setVisibility(View.VISIBLE);
            final SpannableString snippetText = new SpannableString(snippet);
            vSnippet.setText(snippetText);
        } else {
            vSnippet.setVisibility(View.GONE);
        }
        return mView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
}
