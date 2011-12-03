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

package ca.mudar.mtlaucasou;

import ca.mudar.mtlaucasou.provider.PlacemarkContract.PlacemarkColumns;
import ca.mudar.mtlaucasou.ui.widgets.PlacemarksCursorAdapter;
import ca.mudar.mtlaucasou.utils.ActivityHelper;
import ca.mudar.mtlaucasou.utils.AppHelper;
import ca.mudar.mtlaucasou.utils.Const.PrefsValues;
import ca.mudar.mtlaucasou.utils.NotifyingAsyncQueryHandler;

import com.google.android.maps.GeoPoint;

import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.view.View;
import android.widget.ListView;

public class BaseListFragment extends ListFragment implements
        NotifyingAsyncQueryHandler.AsyncQueryListener {
    protected static final String TAG = "BaseListFragment";

    protected ActivityHelper mActivityHelper;
    protected AppHelper mAppHelper;

    protected static final int QUERY_TOKEN = 0x1;

    protected NotifyingAsyncQueryHandler mHandler;
    protected PlacemarksCursorAdapter mAdapter;
    protected Cursor mCursor;
    protected OnPlacemarkSelectedListener mListener;

    static final String[] PLACEMARKS_SUMMARY_PROJECTION = new String[] {
            BaseColumns._ID,
            PlacemarkColumns.PLACEMARK_NAME,
            PlacemarkColumns.PLACEMARK_ADDRESS,
            PlacemarkColumns.PLACEMARK_GEO_LAT,
            PlacemarkColumns.PLACEMARK_GEO_LNG,
            PlacemarkColumns.PLACEMARK_DISTANCE,
    };

    /**
     * Initialized by constructor
     */
    protected int indexSection;
    protected String defaultSort;

    /**
     * BaseListActivity Constructor
     * 
     * @param indexSection Used by {@link ActivityHelper} to get the each
     *            section's content
     * @param listActivity The class called by the intent launched by the
     *            actionBar handler
     */
    public BaseListFragment(int indexSection, String defaultSort) {
        this.indexSection = indexSection;
        this.defaultSort = defaultSort;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mActivityHelper = ActivityHelper.createInstance(getActivity());
        mAppHelper = ((AppHelper) getActivity().getApplicationContext());
        
        Location myLocation = mAppHelper.getLocation();

        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);

        if (mCursor != null) {
            getActivity().stopManagingCursor(mCursor);
            mCursor = null;
        }

        setListAdapter(null);
        mHandler.cancelOperation(QUERY_TOKEN);

        /**
         * Update the projection alias to calculate the distance based on the
         * current Location.
         */
        String mSort;
        if (mAppHelper.getListSort().equals(PrefsValues.LIST_SORT_DISTANCE) && myLocation != null) {
            mSort = PlacemarkColumns.PLACEMARK_DISTANCE + " ASC ";
        }
        else {
            mSort = defaultSort;
        }

        mCursor = getActivity().getContentResolver().query(
                mActivityHelper.getContentUri(indexSection),
                PLACEMARKS_SUMMARY_PROJECTION, null, null, mSort);
        getActivity().startManagingCursor(mCursor);

        mAdapter = new PlacemarksCursorAdapter(getActivity(),
                R.layout.fragment_list_item_placemarks,
                mCursor,
                new String[] {
                        PlacemarkColumns.PLACEMARK_NAME,
                        PlacemarkColumns.PLACEMARK_ADDRESS
                }, new int[] {
                        R.id.placemark_name, R.id.placemark_address
                }, 0);

        setListAdapter(mAdapter);

        mHandler.startQuery(QUERY_TOKEN, null,
                mActivityHelper.getContentUri(indexSection),
                PLACEMARKS_SUMMARY_PROJECTION, null, null,
                mSort);
    }

    // TODO: add Refresh (distances) button 
    // @Override
    // public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // /**
    // * Manual detection of Android version: This is because of a
    // * ActionBarSherlock/compatibility package issue with the MenuInflater.
    // * Also, versions earlier than Honeycomb don't manage SHOW_AS_ACTION_*
    // * options other than ALWAYS.
    // */
    //
    // if (Const.SUPPORTS_HONEYCOMB) {
    // /**
    // * Honeycomb drawables are different (white instead of grey) because
    // * the items are in the actionbar. Order is: toggle (1), kml (2),
    // * list sort (3), postal code (4), my position (5).
    // */
    // menu.add(Menu.NONE, R.id.menu_list_sort_order, 3,
    // R.string.menu_list_sort_order)
    // .setIcon(getResources().getDrawable(R.drawable.ic_actionbar_list_sort));
    // }
    // else {
    // inflater.inflate(R.menu.menu_fragment_list, menu);
    // }
    // }

    /**
     * Container Activity must implement this interface to receive the list item
     * clicks.
     */
    public interface OnPlacemarkSelectedListener {
        public void onPlacemarkSelected(GeoPoint geoPoint);
    }

    /**
     * When item is selected, send geocoordinates to the listener which is
     * implemented by the Activity. The Activity deals with the MapFragment.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Cursor cursor = (Cursor) getListAdapter().getItem(position);

        double geoLat = cursor.getDouble(cursor
                .getColumnIndexOrThrow(PlacemarkColumns.PLACEMARK_GEO_LAT));
        double geoLng = cursor.getDouble(cursor
                .getColumnIndexOrThrow(PlacemarkColumns.PLACEMARK_GEO_LNG));

        GeoPoint geoPoint = new GeoPoint((int) (geoLat * 1E6), (int) (geoLng * 1E6));

        if (mListener != null) {
            mListener.onPlacemarkSelected(geoPoint);
        }
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (this == null) {
            return;
        }

        getActivity().stopManagingCursor(mCursor);
        mCursor = cursor;
        getActivity().startManagingCursor(mCursor);
        mAdapter.changeCursor(mCursor);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getContentResolver().registerContentObserver(
                mActivityHelper.getContentUri(indexSection),
                true, mTransactionsChangesObserver);
        if (mCursor != null) {
            mCursor.requery();
        }
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(mTransactionsChangesObserver);
    }

    /**
     * Attach a listener
     */
    @Override
    public void onAttach(SupportActivity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPlacemarkSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPlacemarkSelectedListener");
        }
    }

    protected ContentObserver mTransactionsChangesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null) {
                mAdapter.notifyDataSetChanged();
                mCursor.requery();
            }
        }
    };
}
