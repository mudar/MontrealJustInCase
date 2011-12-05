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

package ca.mudar.mtlaucasou.utils;

import ca.mudar.mtlaucasou.MainActivity;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.EmergencyHostels;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.FireHalls;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.SpvmStations;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.WaterSupplies;
import ca.mudar.mtlaucasou.ui.AboutActivity;
import ca.mudar.mtlaucasou.ui.widgets.MyPreferenceActivity;
import ca.mudar.mtlaucasou.utils.Const.KmlRemoteUrls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;

public class ActivityHelper {

    private static final String TAG = "ActivityHelper";

    protected Activity mActivity;

    /**
     * Instance Creator.
     * 
     * @param activity
     * @return
     */
    public static ActivityHelper createInstance(Activity activity) {
        // System.setProperty("http.keepAlive", "false");
        return new ActivityHelper(activity);
    }

    /**
     * The Constructor.
     * 
     * @param activity
     */
    protected ActivityHelper(Activity activity) {
        mActivity = activity;
    }

    /**
     * Go to Dashboard on ActionBar home tap, clearing activity stack
     */
    public final void goHome() {
        if (mActivity instanceof MainActivity) {
            return;
        }

        final Intent intent = new Intent(mActivity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mActivity.startActivity(intent);

        if (!(mActivity instanceof MainActivity)) {
            mActivity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
        }
    }

    /**
     * Wrapper for onOptionsItemSelected(MenuItem item, int indexSection),
     * setting indexSection to -1.
     * 
     * @param item The selected menu item
     * @return boolean
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        return onOptionsItemSelected(item, -1);
    }

    /**
     * @param item The selected menu item
     * @param indexSection The current section
     * @return boolean
     */
    public boolean onOptionsItemSelected(MenuItem item, int indexSection) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case android.R.id.home:
                goHome();
                return true;
            case R.id.menu_preferences:
                intent = new Intent(mActivity, MyPreferenceActivity.class);
                mActivity.startActivity(intent);
                return true;
            case R.id.menu_about:
                intent = new Intent(mActivity, AboutActivity.class);
                mActivity.startActivity(intent);
                return true;
            case R.id.menu_link_kml:
                showAttachmentDownloadDialog(indexSection);
                return true;
        }
        return false;
    }

    /**
     * Get the Resource string title of the current section
     * 
     * @param index The current section
     * @return The Resource ID of the string
     */
    public int getActionbarTitle(int index) {
        int res = R.string.app_name;
        switch (index) {
            case Const.INDEX_ACTIVITY_FIRE_HALLS:
                res = R.string.app_label_fire_halls;
                break;
            case Const.INDEX_ACTIVITY_SPVM_STATIONS:
                res = R.string.app_label_spvm_stations;
                break;
            case Const.INDEX_ACTIVITY_WATER_SUPPLIES:
                res = R.string.app_label_water_supplies;
                break;
            case Const.INDEX_ACTIVITY_EMERGENCY_HOSTELS:
                res = R.string.app_label_emergency_hostels;
                break;
        }

        return res;
    }

    /**
     * Get the Resource icon of the current section
     * 
     * @param index The current section
     * @return The Resource ID of the drawable icon
     */
    public int getMapPlacemarkIcon(int index) {
        int res = R.string.app_name;
        switch (index) {
            case Const.INDEX_ACTIVITY_FIRE_HALLS:
                res = R.drawable.ic_map_marker_fire_hall;
                break;
            case Const.INDEX_ACTIVITY_SPVM_STATIONS:
                res = R.drawable.ic_map_marker_spvm_station;
                break;
            case Const.INDEX_ACTIVITY_WATER_SUPPLIES:
                res = R.drawable.ic_map_marker_water_supply;
                break;
            case Const.INDEX_ACTIVITY_EMERGENCY_HOSTELS:
                res = R.drawable.ic_map_marker_emergency_hostel;
                break;
        }

        return res;
    }

    /**
     * Show the explanatory confirmation dialog before openning the remote KML
     * file using GMaps.
     * 
     * @param index The current section
     */
    private void showAttachmentDownloadDialog(int index) {
        final int indexSection = index;
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);

        alert.setTitle(R.string.dialog_remote_kml_title);
        alert.setMessage(R.string.dialog_remote_kml_summary);

        alert.setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                goWebView(indexSection);
            }
        });
        alert.setNegativeButton(R.string.dialog_btn_cancel, null);
        // TODO Handle rotation by saving the activity instance
        alert.show();
    }

    /**
     * Start Google Maps activity displaying remote KML file.
     * 
     * @param index The current section
     */
    public void goWebView(int index) {
        String sUrl;
        switch (index) {
            case Const.INDEX_ACTIVITY_FIRE_HALLS:
                sUrl = KmlRemoteUrls.FIRE_HALLS;
                break;
            case Const.INDEX_ACTIVITY_SPVM_STATIONS:
                sUrl = KmlRemoteUrls.SPVM_STATIONS;
                break;
            case Const.INDEX_ACTIVITY_WATER_SUPPLIES:
                sUrl = KmlRemoteUrls.WATER_SUPPLIES;
                break;
            case Const.INDEX_ACTIVITY_EMERGENCY_HOSTELS:
                sUrl = KmlRemoteUrls.EMERGENCY_HOSTELS;
                break;
            default:
                return;
        }

        String title = mActivity.getResources().getString(R.string.dialog_title_maps_chooser);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=" + sUrl));
        mActivity.startActivity(Intent.createChooser(intent, title));
    }

    /**
     * Get the URI of the current section
     * 
     * @param index The current section
     * @return Section's URI
     */
    public Uri getContentUri(int index) {
        Uri uri;
        switch (index) {
            case Const.INDEX_ACTIVITY_FIRE_HALLS:
                uri = FireHalls.CONTENT_URI;
                break;
            case Const.INDEX_ACTIVITY_SPVM_STATIONS:
                uri = SpvmStations.CONTENT_URI;
                break;
            case Const.INDEX_ACTIVITY_WATER_SUPPLIES:
                uri = WaterSupplies.CONTENT_URI;
                break;
            case Const.INDEX_ACTIVITY_EMERGENCY_HOSTELS:
                uri = EmergencyHostels.CONTENT_URI;
                break;
            default:
                uri = Uri.parse("");
                break;
        }
        return uri;
    }
}
