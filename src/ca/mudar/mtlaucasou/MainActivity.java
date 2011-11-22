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

import ca.mudar.mtlaucasou.provider.PlacemarkDatabase;
import ca.mudar.mtlaucasou.service.SyncService;
import ca.mudar.mtlaucasou.utils.ActivityHelper;
import ca.mudar.mtlaucasou.utils.AppHelper;
import ca.mudar.mtlaucasou.utils.Const;
import ca.mudar.mtlaucasou.utils.DetachableResultReceiver;
import ca.mudar.mtlaucasou.utils.EulaHelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuInflater;
import android.support.v4.view.MenuItem;
import android.support.v4.view.Window;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    private ActivityHelper mActivityHelper;
    private AppHelper mAppHelper;
    private SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;
    private boolean hasLoadedData;
    private String lang;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        /**
         * SharedPreferences are used to verify determine if syncService is
         * required for initial launch or on database upgrade.
         */
        SharedPreferences prefs = getSharedPreferences(Const.APP_PREFS_NAME,
                Context.MODE_PRIVATE);
        hasLoadedData = prefs.getBoolean(Const.PrefsNames.HAS_LOADED_DATA, false);
        int dbVersionPrefs = prefs.getInt(Const.PrefsNames.VERSION_DATABASE, -1);

        if (!hasLoadedData || PlacemarkDatabase.getDatabaseVersion() > dbVersionPrefs) {
            hasLoadedData = false;
            createServiceFragment();
        }

        /**
         * Display the GPLv3 licence
         */
        if (!EulaHelper.hasAcceptedEula(this)) {
            EulaHelper.showEula(false, this);
        }

        /**
         * Get the ActivityHelper
         */
        mActivityHelper = ActivityHelper.createInstance(this);
        mAppHelper = (AppHelper) getApplicationContext();

        lang = mAppHelper.getLanguage();

        mAppHelper.updateUiLanguage();
        setContentView(R.layout.activity_home);

        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        /**
         * Android ICS has support for setHomeButtonEnabled() to disable tap on
         * actionbar logo on dashboard.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setHomeButtonEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * Update the interface language
         */
        getSupportActionBar().setTitle(R.string.app_name);
        if (!lang.equals(mAppHelper.getLanguage())) {
            lang = mAppHelper.getLanguage();
            this.onConfigurationChanged();
        }

        /**
         * Starting the sync service is done onResume() for
         * SyncStatusUpdaterFragment to be ready. Otherwise, we send an empty
         * receiver to the service.
         */
        if (!hasLoadedData && (mSyncStatusUpdaterFragment != null)) {
            Intent intent = new Intent(Intent.ACTION_SYNC, null, getApplicationContext(),
                    SyncService.class);
            intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mSyncStatusUpdaterFragment.mReceiver);
            startService(intent);

            // TODO Move this to a sync service listener.
            SharedPreferences prefs = getSharedPreferences(Const.APP_PREFS_NAME,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putBoolean(Const.PrefsNames.HAS_LOADED_DATA, true);
            editor.putInt(Const.PrefsNames.VERSION_DATABASE, PlacemarkDatabase.getDatabaseVersion());
            editor.commit();
            hasLoadedData = true;
        }
    }

    private void createServiceFragment() {
        FragmentManager fm = getSupportFragmentManager();

        mSyncStatusUpdaterFragment = (SyncStatusUpdaterFragment) fm
                .findFragmentByTag(SyncStatusUpdaterFragment.TAG);
        if (mSyncStatusUpdaterFragment == null) {
            mSyncStatusUpdaterFragment = new SyncStatusUpdaterFragment();
            fm.beginTransaction().add(mSyncStatusUpdaterFragment, SyncStatusUpdaterFragment.TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /**
         * This is because of a ActionBarSherlock/compatibility package with the
         * MenuInflater. Also, versions earlier than Honeycomb can only handle
         * SHOW_AS_ACTION_ALWAYS
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            MenuInflater inflater = (MenuInflater) getMenuInflater();
            inflater.inflate(R.menu.menu_main, menu);
        }
        else {
            menu.add(Const.MENU_ITEM_GROUP_ID, R.id.menu_about,
                    Const.MENU_ITEM_ORDER,
                    R.string.menu_about)
                    .setIcon(getResources().getDrawable(R.drawable.ic_actionbar_info_details))
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            /**
             * We also use a different icon for >= Honeycomb
             */
            menu.add(Const.MENU_ITEM_GROUP_ID, R.id.menu_preferences,
                    Const.MENU_ITEM_ORDER,
                    R.string.menu_preferences)
                    .setIcon(getResources().getDrawable(R.drawable.ic_actionbar_preferences))
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mActivityHelper.onOptionsItemSelected(item);
    }

    /**
     * Update the interface language, independently from the phone's UI
     * language. This does not override the parent function because the Manifest
     * does not include configChanges.
     */
    private void onConfigurationChanged() {
        View root = findViewById(android.R.id.content).getRootView();

        ((Button) root.findViewById(R.id.home_btn_fire_halls))
                .setText(R.string.btn_fire_halls);
        ((Button) root.findViewById(R.id.home_btn_spvm_stations))
                .setText(R.string.btn_spvm_stations);
        ((Button) root.findViewById(R.id.home_btn_water_supplies))
                .setText(R.string.btn_water_supplies);
        ((Button) root.findViewById(R.id.home_btn_emergency_hostels))
                .setText(R.string.btn_emergency_hostels);

        invalidateOptionsMenu();
    }

    // @Override
    // public void onAttachedToWindow() {
    // // TODO: verify if this does any difference since it uses
    // android.view.Window
    // super.onAttachedToWindow();
    // android.view.Window window = (android.view.Window) getWindow();
    // window.setFormat(PixelFormat.RGBA_8888);
    // }

    // private void updateRefreshStatus(boolean refreshing) {
    // mActivityHelper.setRefreshActionButtonState(refreshing);
    // }

    public static class SyncStatusUpdaterFragment extends Fragment implements
            DetachableResultReceiver.Receiver {
        public static final String TAG = SyncStatusUpdaterFragment.class.getName();

        // private boolean mSyncing = false;
        private DetachableResultReceiver mReceiver;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            mReceiver = new DetachableResultReceiver(new Handler());
            mReceiver.setReceiver(this);
        }

        /** {@inheritDoc} */
        public void onReceiveResult(int resultCode, Bundle resultData) {
            MainActivity activity = (MainActivity) getSupportActivity();
            if (activity == null) {
                return;
            }
            activity.setProgressBarIndeterminateVisibility(Boolean.TRUE);

            switch (resultCode) {
                case SyncService.STATUS_RUNNING: {
                    // Log.v(TAG, "SyncService.STATUS_RUNNING");
                    activity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    // mSyncing = true;
                    break;
                }
                case SyncService.STATUS_FINISHED: {
                    // Log.v(TAG, "SyncService.STATUS_FINISHED");
                    activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    // mSyncing = false;
                    // TODO put this in an activity listener
                    if (EulaHelper.hasAcceptedEula(getSupportActivity().getApplicationContext()))
                    {
                        Toast.makeText(activity, R.string.toast_sync_finished, Toast.LENGTH_SHORT)
                                .show();
                    }

                    break;
                }
                case SyncService.STATUS_ERROR: {
                    // Log.v(TAG, "SyncService.STATUS_ERROR");
                    activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    /**
                     * Error happened down in SyncService, show as toast.
                     */
                    // mSyncing = false;
                    final String errorText = getString(R.string.toast_sync_error,
                            resultData.getString(Intent.EXTRA_TEXT));
                    Toast.makeText(activity, errorText, Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }

        // @Override
        // public void onActivityCreated(Bundle savedInstanceState) {
        // super.onActivityCreated(savedInstanceState);
        // ((MainActivity) getSupportActivity()).updateRefreshStatus(mSyncing);
        // }
    }

}
