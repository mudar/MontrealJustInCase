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

package ca.mudar.mtlaucasou.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.model.LayerType;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.util.MapUtils;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class UserPrefs implements
        Const.PrefsNames {
    private static final String TAG = makeLogTag("Settings");
    private static UserPrefs instance;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mPrefsEditor;

    public static UserPrefs getInstance(Context context) {
        if (instance == null) {
            instance = new UserPrefs(context);
        }
        return instance;
    }

    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(Const.APP_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void setDefaultValues(Context context) {
        PreferenceManager.setDefaultValues(context,
                Const.APP_PREFS_NAME,
                Context.MODE_PRIVATE,
                R.xml.prefs_defaults,
                false);
    }

    private UserPrefs(Context context) {
        mPrefs = getSharedPrefs(context);
    }

    @SuppressLint("CommitPrefEdits")
    private SharedPreferences.Editor edit() {
        if (mPrefsEditor == null) {
            mPrefsEditor = mPrefs.edit();
        }

        return mPrefsEditor;
    }

    public boolean hasLoadedData() {
        final boolean hasLoadedData = mPrefs.getBoolean(HAS_LOADED_DATA, false);
        if (!hasLoadedData) {
            // The answer can be true only once. We need commit() instead of apply()
            // to avoid possible delay-related issues.
            edit().putBoolean(HAS_LOADED_DATA, true)
                    .commit();
        }

        return hasLoadedData;
    }

    public boolean hasAcceptedEula(Context context) {
        return mPrefs.getBoolean(HAS_ACCEPTED_EULA, false) || hasAcceptedEulaLegacy(context);
    }

    /**
     * Legacy: EULA settings were stored in the default SharedPreferences.
     * For some reason, that seemed like a good idea at the time!
     *
     * @param context The Context
     * @return true if user had accepted EULA in version 1.0
     */
    private boolean hasAcceptedEulaLegacy(Context context) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(HAS_ACCEPTED_EULA, false);
    }

    public void setHasAcceptedEula() {
        edit().putBoolean(HAS_ACCEPTED_EULA, true)
                .commit();
    }

    public String getLanguage() {
        return mPrefs.getString(LANGUAGE, Locale.getDefault().getLanguage());
    }

    public void setLanguage(String language) {
        edit().putString(LANGUAGE, language)
                .commit();
    }

    public String getUnitsSystem() {
        return mPrefs.getString(UNITS_SYSTEM, Const.PrefsValues.UNITS_ISO);
    }

    public void setUnitsSystem(String units) {
        edit().putString(UNITS_SYSTEM, units)
                .apply();
    }

    public boolean isPermissionDeniedForEver() {
        return mPrefs.getBoolean(PERMISSION_DENIED_FOR_EVER, false);
    }

    public void setPermissionDeniedForEver(boolean denied) {
        edit().putBoolean(PERMISSION_DENIED_FOR_EVER, denied)
                .apply();
    }

    @SuppressWarnings("WrongConstant")
    @MapType
    public String getLastMapType() {
        return mPrefs.getString(LAST_MAP_TYPE, Const.MapTypes._DEFAULT);
    }

    public void setLastMapType(@MapType String mapType) {
        edit().putString(LAST_MAP_TYPE, mapType)
                .apply();
    }

    @LayerType
    public Set<String> getEnabledLayers() {
        return mPrefs.getStringSet(LAYERS_ENABLED, Const.PrefsValues.DEFAULT_LAYERS);
    }

    public boolean isLayerEnabled(@LayerType String layerType) {
        return mPrefs.getStringSet(LAYERS_ENABLED, Const.PrefsValues.DEFAULT_LAYERS)
                .contains(layerType);
    }

    public void setLayerEnabledForced(@MapType String mapType, @LayerType String layerType) {
        if (MapUtils.isMultiLayerMapType(mapType)) {
            setLayerEnabled(layerType, true, true);
        }
    }

    public void setLayerEnabled(@LayerType String layerType, boolean enabled) {
        setLayerEnabled(layerType, enabled, false);
    }

    private void setLayerEnabled(@LayerType String layerType, boolean enabled, boolean commit) {
        if (TextUtils.isEmpty(layerType)) {
            return;
        }

        // We need to make a copy of the hashset, not just get a reference
        final Set<String> enabledLayers = new HashSet<>(mPrefs.getStringSet(LAYERS_ENABLED,
                Const.PrefsValues.DEFAULT_LAYERS));

        boolean result = false;
        if (enabled && !enabledLayers.contains(layerType)) {
            result = enabledLayers.add(layerType);
        } else if (!enabled) {
            result = enabledLayers.remove(layerType);
        }

        if (result) {
            final SharedPreferences.Editor editor = edit().putStringSet(LAYERS_ENABLED, enabledLayers);
            if (commit) {
                editor.commit();
            } else {
                editor.apply();
            }
        }
    }

    /**
     * Check if the API dataset has updates for the requested dataset item
     *
     * @param key       The local dataset key
     * @param updatedAt The remote dataset date to compare to
     * @return true if updates are needed
     */
    public boolean isApiDataNewer(String key, Date updatedAt) {
        return Long.compare(mPrefs.getLong(key, Const.UNKNOWN_VALUE), updatedAt.getTime()) < 0;
    }

    public void setDataUpdatedAt(String key, Date updatedAt) {
        edit().putLong(key, updatedAt.getTime())
                .apply();
    }

    public boolean shouldDisplayLayersShowcase() {
        final boolean isFirst = mPrefs.getBoolean(SHOWCASE_LAYERS, true);

        if (isFirst) {
            edit().putBoolean(SHOWCASE_LAYERS, false)
                    .apply();
        }

        return isFirst;
    }
}
