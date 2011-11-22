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
import ca.mudar.mtlaucasou.utils.ActivityHelper;
import ca.mudar.mtlaucasou.utils.AppHelper;
import ca.mudar.mtlaucasou.utils.ConnectionHelper;
import ca.mudar.mtlaucasou.utils.Const;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentMapActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuInflater;
import android.support.v4.view.MenuItem;
import android.view.View;

public class BaseMapActivity extends FragmentMapActivity implements OnPlacemarkSelectedListener {
    protected static final String TAG = "BaseMapActivity";

    /**
     * Used to save/restore display of the fragmentList onResume and when
     * rotating device. If list is shown in portrait, Xlarge-land layouts
     * default to show both map and list. This also toggles the icon used for
     * R.id.actionbar_toggle_list.
     */
    protected boolean isHiddenList;
    protected MenuItem btnActionbarToggleList;

    /**
     * Must be Initialized by constructor.
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
         * This is because of a ActionBarSherlock/compatibility package with the
         * MenuInflater. Also, versions earlier than Honeycomb understand only
         * SHOW_AS_ACTION_ALWAYS.
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            MenuInflater inflater = (MenuInflater) getMenuInflater();
            inflater.inflate(R.menu.menu_map, menu);
        }
        else {
            menu.add(Const.MENU_ITEM_GROUP_ID, R.id.actionbar_toggle_list, Const.MENU_ITEM_ORDER,
                    R.string.menu_view_list)
                    .setIcon(getResources().getDrawable(R.drawable.ic_actionbar_view_list))
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            /**
             * The Honeycomb for R.id.menu_link_kml is different (white instead
             * of grey) because it's in the actionbar.
             */
            menu.add(Const.MENU_ITEM_GROUP_ID, R.id.menu_link_kml, Const.MENU_ITEM_ORDER,
                    R.string.menu_link_kml)
                    .setIcon(getResources().getDrawable(R.drawable.ic_actionbar_attachment));
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

        if (item.getItemId() == R.id.actionbar_toggle_list) {

            View root = findViewById(R.id.map_root_landscape);
            boolean isTablet = (root != null);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment fragmentMap = fm.findFragmentByTag(Const.TAG_FRAGMENT_MAP);
            Fragment fragmentList = fm.findFragmentByTag(Const.TAG_FRAGMENT_LIST);

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

}
