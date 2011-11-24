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

import com.google.android.maps.GeoPoint;

import ca.mudar.mtlaucasou.BaseListFragment.OnPlacemarkSelectedListener;
import ca.mudar.mtlaucasou.BaseMapFragment.OnMyLocationChangedListener;
import ca.mudar.mtlaucasou.utils.ActivityHelper;
import ca.mudar.mtlaucasou.utils.AppHelper;
import ca.mudar.mtlaucasou.utils.ConnectionHelper;
import ca.mudar.mtlaucasou.utils.Const;
import ca.mudar.mtlaucasou.utils.Helper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentMapActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuInflater;
import android.support.v4.view.MenuItem;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class BaseMapActivity extends FragmentMapActivity implements OnPlacemarkSelectedListener,
        OnMyLocationChangedListener,
        Runnable {
    protected static final String TAG = "BaseMapActivity";

    /**
     * Used to save/restore display of the fragmentList onResume and when
     * rotating device. If list is shown in portrait, Xlarge-land layouts
     * default to show both map and list. This also toggles the icon used for
     * R.id.actionbar_toggle_list.
     */
    protected boolean isHiddenList;

    protected MenuItem btnActionbarToggleList;
    private String postalCode;
    private ProgressDialog pd;

    /**
     * Must be initialized by constructor.
     */
    protected int indexSection;

    public BaseMapActivity(int indexSection) {
        this.indexSection = indexSection;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AppHelper) getApplicationContext()).updateUiLanguage();

        /**
         * By default, show map and hide list.
         */
        isHiddenList = true;
        if ((savedInstanceState != null)
                && savedInstanceState.containsKey(Const.KEY_INSTANCE_LIST_IS_HIDDEN)) {
            /**
             * For visible/hidden fragments and actionbar icon
             */
            isHiddenList = savedInstanceState.getBoolean(Const.KEY_INSTANCE_LIST_IS_HIDDEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setHomeButtonEnabled(true);
        }
    }

    /**
     * Default (and restore) of hidden/visible fragments. {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        if (!ConnectionHelper.hasConnection(this)) {
            ConnectionHelper.showDialogNoConnection(this);
        }

        View root = findViewById(R.id.map_root_landscape);
        boolean isTablet = (root != null);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment fragmentMap = fm.findFragmentByTag(Const.TAG_FRAGMENT_MAP);
        Fragment fragmentList = fm.findFragmentByTag(Const.TAG_FRAGMENT_LIST);

        // TODO Bug: onResume after device (Nook!) shutdown or memory problems.
        // Temporary solution is the use of ft.show() in the following lines.
        /**
         * By default, both fragments are shown. No need to use
         * FragmentTransaction.show().
         */
        if (isTablet) {
            if (isHiddenList) {
                /**
                 * List was hidden, we'll hide it again.
                 */
                ft.show(fragmentMap);
                ft.hide(fragmentList);
                // isHiddenList = true;
            }
            else {
                /**
                 * List was not hidden, nothing to do here.
                 */
                ft.show(fragmentMap);
                ft.show(fragmentList);
                // isHiddenList = false;
            }
        }
        else if (!isTablet) {
            if (isHiddenList) {
                /**
                 * List was hidden, we'll hide it again.
                 */
                ft.show(fragmentMap);
                ft.hide(fragmentList);
                // isHiddenList = true;
            }
            else if (!isHiddenList) {
                /**
                 * List was not hidden. Hide the map since this is a portrait
                 * layout.
                 */
                ft.hide(fragmentMap);
                ft.show(fragmentList);
                // isHiddenList = false;
            }
        }

        ft.commit();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /**
         * This is because of a ActionBarSherlock/compatibility package issue
         * with the MenuInflater. Also, versions earlier than Honeycomb
         * understand only SHOW_AS_ACTION_ALWAYS.
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            MenuInflater inflater = (MenuInflater) getMenuInflater();
            inflater.inflate(R.menu.menu_map, menu);
        }
        else {
            menu.add(Menu.NONE, R.id.actionbar_toggle_list, 1,
                    R.string.menu_view_list)
                    .setIcon(getResources().getDrawable(R.drawable.ic_actionbar_view_list))
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            /**
             * The Honeycomb drawables are different (white instead of grey)
             * because the items are in the actionbar.
             */
            // TODO: verify menu_item_order!
            menu.add(Menu.NONE, R.id.menu_link_kml, 2,
                    R.string.menu_link_kml)
                    .setIcon(getResources().getDrawable(R.drawable.ic_actionbar_directions));

            menu.add(Menu.NONE, R.id.menu_map_find_from_name, 3,
                    R.string.menu_map_find_from_name)
                    .setIcon(getResources().getDrawable(R.drawable.ic_actionbar_search));

            menu.add(Menu.NONE, R.id.menu_map_mylocation, 4,
                    R.string.menu_map_mylocation)
                    .setIcon(getResources().getDrawable(R.drawable.ic_actionbar_mylocation));
        }
        btnActionbarToggleList = menu.getItem(0);

        if (!isHiddenList) {
            // TODO Remove this and rely on invalidateOptionsMenu() when
            // supported by Compatibility library.
            /**
             * Activity/Fragments Lifecycle issues. The clean solution would be
             * to detect orientation and fragmentList.isVisible() here to decide
             * which button should be displayed in the actionbar.
             */
            if (btnActionbarToggleList != null) {
                btnActionbarToggleList.setIcon(getResources().getDrawable(
                        R.drawable.ic_actionbar_view_map));
            }
        }

        if (((AppHelper) getApplicationContext()).getLocation() == null) {
            /**
             * Disable the My Location button since user location was not found
             * yet.
             */
            menu.findItem(R.id.menu_map_mylocation).setEnabled(false);
        }

        return true;
    }

    /**
     * Save the list isHidden() status. {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragmentList = fm.findFragmentByTag(Const.TAG_FRAGMENT_LIST);
        if (fragmentList != null) {
            /**
             * For visible/hidden fragments and actionbar icon.
             */
            outState.putBoolean(Const.KEY_INSTANCE_LIST_IS_HIDDEN, fragmentList.isHidden());
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Toggle display of both fragments, depending on landscape/portrait
     * layouts. Also toggle the actionbar button icon. {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        FragmentManager fm = getSupportFragmentManager();
        BaseMapFragment fragmentMap = (BaseMapFragment) fm
                .findFragmentByTag(Const.TAG_FRAGMENT_MAP);
        Fragment fragmentList = fm.findFragmentByTag(Const.TAG_FRAGMENT_LIST);

        if (item.getItemId() == R.id.actionbar_toggle_list) {

            View root = findViewById(R.id.map_root_landscape);
            boolean isTablet = (root != null);

            FragmentTransaction ft = fm.beginTransaction();

            if ((fragmentMap == null) || (fragmentList == null)) {
                return false;
            }

            if (fragmentList.isVisible()) {
                /**
                 * List is visible: hide it.
                 */
                ft.hide(fragmentList);
                isHiddenList = true;
                if (!isTablet) {
                    /**
                     * In portrait layout, we also have to show the hidden map.
                     */
                    ft.show(fragmentMap);
                }

                /**
                 * List is now hidden: Set the actionbar button to view_list.
                 */
                item.setIcon(getResources().getDrawable(R.drawable.ic_actionbar_view_list));
                item.setTitle(R.string.menu_view_list);
            }
            else {
                /**
                 * List is not visible: show it.
                 */
                ft.show(fragmentList);
                isHiddenList = false;
                if (!isTablet) {
                    /**
                     * In portrait layout, we also have to hide the visible map.
                     */
                    ft.hide(fragmentMap);
                }

                /**
                 * Map is now hidden: Set the actionbar button to view_map.
                 */
                item.setIcon(getResources().getDrawable(R.drawable.ic_actionbar_view_map));
                item.setTitle(R.string.menu_view_map);
            }
            ft.commit();

            return true;
        }
        else if (item.getItemId() == R.id.menu_map_mylocation) {
            /**
             * Center map on user location.
             */

            fragmentMap.setMapCenterOnLocation(((AppHelper) getApplicationContext()).getLocation());

            return true;
        }
        else if (item.getItemId() == R.id.menu_map_find_from_name) {
            /**
             * Search location by postal code (or address) and center map on
             * location if found) by Geocode.
             */
            showPostalCodeDialog();
            return true;
        }
        else {
            ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);

            return mActivityHelper.onOptionsItemSelected(item, indexSection)
                    || super.onOptionsItemSelected(item);
        }
    }

    /**
     * Implement the fragmentList listener interface, to pass the selected item
     * to the map. This will center map and display balloon. Actionbar button is
     * toggled to view_map.
     */
    @Override
    public void onPlacemarkSelected(GeoPoint geoPoint) {

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        BaseMapFragment fragmentMap = (BaseMapFragment) fm
                .findFragmentByTag(Const.TAG_FRAGMENT_MAP);
        Fragment fragmentList = fm.findFragmentByTag(Const.TAG_FRAGMENT_LIST);

        if (fragmentMap.isHidden()) {
            /**
             * Toggle hide/show of the fragments and the Actionbar button
             */
            ft.hide(fragmentList);
            ft.show(fragmentMap);
            ft.commit();

            if (btnActionbarToggleList != null) {
                btnActionbarToggleList.setIcon(getResources().getDrawable(
                        R.drawable.ic_actionbar_view_list));
            }

        }

        fragmentMap.setMapCenter(geoPoint);
    }

    /**
     * Implement the fragmentMap listener interface, to get the user's location,
     * send it to the AppHelper and enable the My Location item in the menu.
     */
    @Override
    public void OnMyLocationChanged(final GeoPoint geoPoint) {
        /**
         * Following code allows the background listener to modify the UI's
         * menu.
         */
        runOnUiThread(new Runnable() {
            public void run() {
                ((AppHelper) getApplicationContext()).setLocation(Helper
                        .geoPointToLocation(geoPoint));
                invalidateOptionsMenu();
            }
        });

    }

    /**
     * Show dialog to type postal code for Geocode search.
     */
    private void showPostalCodeDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.dialog_postal_code_title);
        alert.setMessage(R.string.dialog_postal_code_summary);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        alert.setView(input);

        alert.setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                /**
                 * Store value and show processing dialog. Geocode search will
                 * be done in a thread.
                 */
                postalCode = input.getText().toString();
                showDialogProcessing();
            }
        });
        alert.setNegativeButton(R.string.dialog_btn_cancel, null);

        alert.show();
    }

    /**
     * Show the Processing dialog and start the Geocode search thread.
     */
    public void showDialogProcessing() {

        Resources res = getResources();
        String message = res.getString(R.string.toast_postal_code_processing);

        pd = ProgressDialog.show(this, "", message, true);

        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * This runnable thread gets the Geocode search value in the background
     * (front activity is the Processing dialog). Results are sent to the
     * handler.
     */
    @Override
    public void run() {

        Location loc = null;
        try {
            /**
             * Geocode search. Takes time and not very reliable!
             */
            loc = Helper.findLocatioFromName(getApplicationContext(), postalCode);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();

        if (loc == null) {
            /**
             * Send error message to handler.
             */
            b.putInt(Const.KEY_BUNDLE_SEARCH_ADDRESS, Const.BUNDLE_SEARCH_ADDRESS_ERROR);
        }
        else {
            /**
             * Send success message to handler with the found geocoordinates.
             */
            b.putInt(Const.KEY_BUNDLE_SEARCH_ADDRESS, Const.BUNDLE_SEARCH_ADDRESS_SUCCESS);
            b.putDouble(Const.KEY_BUNDLE_ADDRESS_LAT, loc.getLatitude());
            b.putDouble(Const.KEY_BUNDLE_ADDRESS_LNG, loc.getLongitude());
        }
        msg.setData(b);

        handler.sendMessage(msg);
    }

    /**
     * Handle the runnable thread results. This closes the processing dialog
     * then centers map on found location or displays error message.
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pd.dismiss();
            Bundle b = msg.getData();

            if (b.getInt(Const.KEY_BUNDLE_SEARCH_ADDRESS) == Const.BUNDLE_SEARCH_ADDRESS_SUCCESS) {
                /**
                 * Address is found, center map on location.
                 */
                Location loc = new Location(Const.LOCATION_PROVIDER);
                loc.setLatitude(b.getDouble(Const.KEY_BUNDLE_ADDRESS_LAT));
                loc.setLongitude(b.getDouble(Const.KEY_BUNDLE_ADDRESS_LNG));

                FragmentManager fm = getSupportFragmentManager();
                ((BaseMapFragment) fm.findFragmentByTag(Const.TAG_FRAGMENT_MAP))
                        .setMapCenterOnLocation(loc);
            } else {
                /**
                 * Address not found! Display error message.
                 */
                String errorMsg = String.format(getResources().getString(
                        R.string.toast_search_error,
                        postalCode));
                ((AppHelper) getApplicationContext()).showToastText(errorMsg, Toast.LENGTH_LONG);
            }
        }
    };

}
