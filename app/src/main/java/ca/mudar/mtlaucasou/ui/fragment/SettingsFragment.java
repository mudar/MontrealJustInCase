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

package ca.mudar.mtlaucasou.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.Locale;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.util.PermissionUtils;

import static ca.mudar.mtlaucasou.Const.PrefsNames.LANGUAGE;
import static ca.mudar.mtlaucasou.Const.PrefsNames.PERMISSIONS;
import static ca.mudar.mtlaucasou.Const.PrefsNames.PERMISSION_DENIED_FOR_EVER;
import static ca.mudar.mtlaucasou.Const.PrefsNames.UNITS_SYSTEM;
import static ca.mudar.mtlaucasou.Const.PrefsValues.LANG_EN;
import static ca.mudar.mtlaucasou.Const.PrefsValues.LANG_FR;
import static ca.mudar.mtlaucasou.Const.PrefsValues.UNITS_IMP;
import static ca.mudar.mtlaucasou.Const.PrefsValues.UNITS_ISO;
import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    private static final String TAG = makeLogTag("SettingsFragment");

    private SharedPreferences mSharedPrefs;
    private Preference mPrefUnits;
    private Preference mPrefLanguage;
    private Preference mPrefPermissions;
    private OnConfigChangeListener mListener;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnConfigChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnConfigChangeListener");
        }
    }

    /**
     * preference-v7 support still has many issues, so this extends android.app.Fragment
     * Pre-API 24 will call onAttach(Activity) instead of onAttach(Context)
     *
     * @param activity The Activity
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnConfigChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnConfigChangeListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PreferenceManager pm = this.getPreferenceManager();
        pm.setSharedPreferencesName(Const.APP_PREFS_NAME);
        pm.setSharedPreferencesMode(Context.MODE_PRIVATE);

        mSharedPrefs = pm.getSharedPreferences();

        /**
         * Easiest way to hide a
         */
        if (mSharedPrefs.getBoolean(PERMISSION_DENIED_FOR_EVER, false)) {
            addPreferencesFromResource(R.xml.prefs_settings_permissions);
        } else {
            addPreferencesFromResource(R.xml.prefs_settings);
        }

        mPrefUnits = findPreference(UNITS_SYSTEM);
        mPrefLanguage = findPreference(LANGUAGE);
        mPrefPermissions = findPreference(PERMISSIONS);

        if (mPrefPermissions != null) {
            mPrefPermissions.setOnPreferenceClickListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setupSummaries();

        /**
         * Set up a listener whenever a key changes
         */
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        /**
         * Remove the listener onPause
         */
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Implements SharedPreferences.OnSharedPreferenceChangeListener
     * Update summary for changed prefs
     *
     * @param prefs The SharedPreferences that received the change.
     * @param key   The key of the preference that was changed, added, or removed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (UNITS_SYSTEM.equals(key)) {
            mPrefUnits.setSummary(getUnitsSummary(prefs.getString(key, UNITS_ISO)));
        } else if (LANGUAGE.equals(key)) {
            final String lg = prefs.getString(key, Locale.getDefault().getLanguage());
            mPrefLanguage.setSummary(getLanguageSummary(lg));
            mListener.onConfigurationChanged(lg);
        }
    }

    /**
     * Implements Preference.OnPreferenceClickListener
     * Show Android's app settings, to re-enable permissions previously denied forever.
     *
     * @param preference The Preference that was clicked
     * @return True if the click was handled.
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (PERMISSIONS.equals(preference.getKey())) {
            startActivity(PermissionUtils.newAppSettingsIntent(getActivity()));
            return true;
        }
        return false;
    }


    private void setupSummaries() {
        /**
         * Default units system is ISO
         */
        mPrefUnits.setSummary(getUnitsSummary(
                mSharedPrefs.getString(UNITS_SYSTEM, UNITS_ISO)));

        /**
         * The app's Default language is the phone's language. If not supported,
         * we default to English.
         */
        String lg = mSharedPrefs.getString(LANGUAGE, Locale.getDefault().getLanguage());
        if (!LANG_EN.equals(lg) && !LANG_FR.equals(lg)) {
            lg = LANG_EN;
        }
        mPrefLanguage.setSummary(getLanguageSummary(lg));
    }

    private String getUnitsSummary(String index) {
        if (UNITS_ISO.equals(index)) {
            return getResources().getString(R.string.prefs_units_iso);
        } else if (UNITS_IMP.equals(index)) {
            return getResources().getString(R.string.prefs_units_imperial);
        }

        return "";
    }

    private String getLanguageSummary(String index) {
        if (LANG_FR.equals(index)) {
            return getResources().getString(R.string.prefs_language_french);
        } else if (LANG_EN.equals(index)) {
            return getResources().getString(R.string.prefs_language_english);
        }

        return "";
    }

    public interface OnConfigChangeListener {
        void onConfigurationChanged(String lang);
    }
}
