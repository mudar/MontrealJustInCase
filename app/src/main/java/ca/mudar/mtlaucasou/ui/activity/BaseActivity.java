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

package ca.mudar.mtlaucasou.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.aboutlibraries.LibsBuilder;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.data.UserPrefs;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.MetricsContentName;
import ca.mudar.mtlaucasou.util.IntentUtils;
import ca.mudar.mtlaucasou.util.LangUtils;
import ca.mudar.mtlaucasou.util.LogUtils;
import ca.mudar.mtlaucasou.util.MapUtils;
import ca.mudar.mtlaucasou.util.MetricsUtils;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public abstract class BaseActivity extends AppCompatActivity implements
        Toolbar.OnMenuItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = makeLogTag("BaseActivity");

    private static final String SEND_INTENT_TYPE = "text/plain";

    protected boolean isShowTitleEnabled() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LangUtils.updateUiLanguage(getApplicationContext());

        if (this instanceof MainActivity) {
            // Only MainActivity needs to register for updates about LANGUAGE prefs
            UserPrefs.getSharedPrefs(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupToolbar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Const.RequestCodes.EULA_ACCEPTED == requestCode) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this instanceof MainActivity) {
            // Only MainActivity registered for updates about LANGUAGE prefs
            try {
                UserPrefs.getSharedPrefs(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
            } catch (Exception e) {
                LogUtils.REMOTE_LOG(e);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Const.PrefsNames.LANGUAGE.equals(key)) {
            LangUtils.updateUiLanguage(getApplicationContext());
            recreate();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(SettingsActivity.newIntent(getApplicationContext()));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(AboutActivity.newIntent(getApplicationContext()));
            return true;
        } else if (id == R.id.action_share) {
            onShareItemSelected();
            return true;
        } else if (id == R.id.action_rate) {
            IntentUtils.showWebsite(this, R.string.url_playstore);
            return true;
        } else if (id == R.id.action_eula) {
            startActivity(EulaActivity.newIntent(getApplicationContext(), true));
            return true;
        } else if (id == R.id.action_about_libs) {
            onAboutLibsItemSelected();
            return true;
        }

        return false;
    }

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setOnMenuItemClickListener(this);

            //noinspection ConstantConditions
            getSupportActionBar().setDisplayShowTitleEnabled(isShowTitleEnabled());
        }
    }

    /**
     * Show the AboutLibraries acknowledgements activity
     */
    private void onAboutLibsItemSelected() {
        LangUtils.updateUiLanguage(getApplicationContext());
        new LibsBuilder()
                .withActivityTitle(getString(R.string.title_activity_about_libs))
                .withActivityTheme(R.style.AppTheme_AboutLibs)
                .withAutoDetect(false) // For Proguard
                .withFields(R.string.class.getFields()) // For Proguard
                .withLibraries(// Added manually to avoid issues with Proguard
                        "GooglePlayServices", "materialloadingprogressbar",
                        "clansfloatingactionbutton", "AboutLibraries", "materialtaptargetprompt",
                        "Crashlytics",
                        "Retrofit", "OkHttp", "Realm",
                        "gson", "appcompat_v7", "design", "recyclerview_v7", "architectureroom"
                )
                .withExcludedLibraries(
                        "AndroidIconics", "fastadapter", "okio", "support_v4"
                )
                .start(this);

        MetricsUtils.logAboutView(MetricsContentName.ABOUT_LIBS);
    }

    /**
     * Native sharing
     */
    private void onShareItemSelected() {
        final Bundle extras = new Bundle();
        extras.putString(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_intent_title));
        extras.putString(Intent.EXTRA_TEXT, getResources().getString(R.string.url_playstore));

        final Intent sendIntent = new Intent();
        sendIntent.putExtras(extras);
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType(SEND_INTENT_TYPE);
        startActivity(sendIntent);
    }

    /**
     * Show tip about using the layers filter menu
     *
     * @param type
     */
    protected void showcaseMapLayers(@MapType String type) {
        if (MapUtils.isMultiLayerMapType(type) &&
                UserPrefs.getInstance(getApplicationContext()).shouldDisplayLayersShowcase()) {
            final FloatingActionMenu fabMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);

            new MaterialTapTargetPrompt.Builder(this)
                    .setTarget(fabMenu.getMenuIconView())
                    .setTargetRenderView(fabMenu)
                    .setPrimaryText(R.string.showcase_map_layers_title)
                    .setBackgroundColour(ContextCompat.getColor(this, R.color.color_primary_dark))
                    .setSecondaryText(R.string.showcase_map_layers_desc)
                    .show();
        }
    }
}
