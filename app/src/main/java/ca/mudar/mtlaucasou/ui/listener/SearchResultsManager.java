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

package ca.mudar.mtlaucasou.ui.listener;

import android.content.Context;
import android.location.Location;

import java.io.IOException;

import ca.mudar.mtlaucasou.model.Placemark;
import ca.mudar.mtlaucasou.ui.view.PlacemarksSearchView;
import ca.mudar.mtlaucasou.util.GeoUtils;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class SearchResultsManager implements
        PlacemarksSearchView.SearchViewListener {

    private static final String TAG = makeLogTag("SearchResultsManager");

    private final Context mContext;
    private final MapUpdatesListener mCallback;

    public SearchResultsManager(Context context, MapUpdatesListener callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    @Override
    public void onAddressSearchSubmit(String query) {
        try {
            final Location location = GeoUtils.findLocationByName(mContext, query);
            mCallback.moveCameraToLocation(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlacemarkSuggestionClick(Placemark placemark) {
        mCallback.moveCameraToPlacemark(placemark);
    }

    public interface MapUpdatesListener {
        void moveCameraToPlacemark(Placemark placemark);

        void moveCameraToLocation(Location location);
    }
}
