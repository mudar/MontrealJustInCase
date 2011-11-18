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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

//AppHelper mAppHelper = (AppHelper) getApplicationContext();
//Activity: ((AppHelper)getApplication()).setBalance(9.99);

public class AppHelper extends Application {
    protected static final String TAG = "AppHelper";

    private Location mLocation;
    private String mLanguage;
    private Toast mToast;

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        Log.v(TAG,
                "setLocation. Lat = " + location.getLatitude() + ". Lng = "
                        + location.getLongitude());
        this.mLocation = location;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String lang) {
        Log.v(TAG, "setLanguage = " + lang);
        this.mLanguage = lang;
    }

    public void showToastText(int res, int duration) {
        mToast.setText(res);
        mToast.setDuration(duration);
        mToast.show();
    }

    public void showToastText(String msg, int duration) {
        mToast.setText(msg);
        mToast.setDuration(duration);
        mToast.show();
    }

    // TODO: verify possible memory leakage of the following code
    private static AppHelper instance = null;

    public static AppHelper getInstance() {
        checkInstance();
        return instance;
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        SharedPreferences prefs = getSharedPreferences(Const.APP_PREFS_NAME, Context.MODE_PRIVATE);
        mLanguage = prefs.getString(Const.PrefsNames.LANGUAGE, Locale.getDefault().getLanguage());
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }
}
