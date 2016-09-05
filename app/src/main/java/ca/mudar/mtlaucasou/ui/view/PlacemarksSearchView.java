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

package ca.mudar.mtlaucasou.ui.view;

import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.MenuItem;

import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.data.SuggestionsCursorHelper;
import ca.mudar.mtlaucasou.model.Placemark;
import ca.mudar.mtlaucasou.ui.adapter.PlacemarkSearchAdapter;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class PlacemarksSearchView extends android.support.v7.widget.SearchView implements
        SearchView.OnSuggestionListener,
        SearchView.OnQueryTextListener {
    private static final String TAG = makeLogTag("PlacemarksSearchView");

    private MenuItem mSearchMenuItem;
    private SearchViewListener mListener;

    public PlacemarksSearchView(Context context) {
        this(context, null);
    }

    public PlacemarksSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.searchViewStyle);
    }

    public PlacemarksSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setQueryHint(context.getString(R.string.search_hint));
        setSuggestionsAdapter(new PlacemarkSearchAdapter(context));
    }

    /**
     * Required for the SearchView to collapse the ActionView
     *
     * @param menuItem
     */
    public void setSearchMenuItem(MenuItem menuItem) {
        this.mSearchMenuItem = menuItem;
    }

    public void setListener(SearchViewListener listener) {
        this.mListener = listener;

        // Also set the other listeners
        setOnSuggestionListener(this);
        setOnQueryTextListener(this);
    }

    /**
     * Implements SearchView.OnSuggestionListener
     *
     * @param position
     * @return always false to ignore
     */
    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    /**
     * Implements SearchView.OnSuggestionListener
     * Handles the click on an auto-complete Placemark
     *
     * @param position
     * @return always true, to skip Intent lookup
     */
    @Override
    public boolean onSuggestionClick(int position) {
        final Placemark place = SuggestionsCursorHelper
                .cursorObjectToPlace(getSuggestionsAdapter().getCursor(), position);

        if (place != null) {
            setQuery(place.getName(), false);
            collapseActionView();
            mListener.onPlacemarkSuggestionClick(place);
        }
        return true;
    }

    /**
     * Implements SearchView.OnQueryTextListener
     * Handles the submit button for a word search
     *
     * @param query
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        collapseActionView();
        mListener.onAddressSearchSubmit(query);
        return false;
    }

    /**
     * SearchView.OnQueryTextListener
     *
     * @param newText
     * @return always false, to enable running the adapter's filter
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void collapseActionView() {
        if (mSearchMenuItem != null) {
            MenuItemCompat.collapseActionView(mSearchMenuItem);
        }
    }

    public interface SearchViewListener {
        void onAddressSearchSubmit(String query);

        void onPlacemarkSuggestionClick(Placemark placemark);
    }
}
