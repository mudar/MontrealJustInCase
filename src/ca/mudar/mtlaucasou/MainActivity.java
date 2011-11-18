
package ca.mudar.mtlaucasou;

import ca.mudar.mtlaucasou.service.SyncService;
import ca.mudar.mtlaucasou.utils.ActivityHelper;
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
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    private ActivityHelper mActivityHelper;
    private SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;
    private boolean hasLoadedData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        SharedPreferences prefs = getSharedPreferences(Const.APP_PREFS_NAME, Context.MODE_PRIVATE);
        hasLoadedData = prefs.getBoolean(Const.PrefsNames.HAS_LOADED_DATA, false);
        if (!hasLoadedData) {
            Log.v(TAG, "hasLoadedData = false");
            createServiceFragment();
        }

        // TODO Add Eula content
        if (!EulaHelper.hasAcceptedEula(this)) {
            EulaHelper.showEula(false, this);
        }

        mActivityHelper = ActivityHelper.createInstance(this);

        setContentView(R.layout.activity_home);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setHomeButtonEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");

        /**
         * Starting the sync service is done onResume() for
         * SyncStatusUpdaterFragment to be ready. Otherwise, we send an empty
         * receiver to the service.
         */
        if (!hasLoadedData && (mSyncStatusUpdaterFragment != null)) {
            Log.v(TAG, "mSyncStatusUpdaterFragment != null");
            Intent intent = new Intent(Intent.ACTION_SYNC, null, getApplicationContext(),
                    SyncService.class);
            intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mSyncStatusUpdaterFragment.mReceiver);
            startService(intent);

            // TODO Move this to a sync service listener.
            SharedPreferences prefs = getSharedPreferences(Const.APP_PREFS_NAME,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putBoolean(Const.PrefsNames.HAS_LOADED_DATA, true);
            editor.commit();
            hasLoadedData = true;
        }
    }

    private void createServiceFragment() {
        Log.v(TAG, "createServiceFragment");

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
            Log.v(TAG, "onCreate");
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            mReceiver = new DetachableResultReceiver(new Handler());
            if (mReceiver == null) {
                Log.v(TAG, "mReceiver == null ");
            }
            Log.v(TAG, "setReceiver");
            mReceiver.setReceiver(this);
        }

        /** {@inheritDoc} */
        public void onReceiveResult(int resultCode, Bundle resultData) {
            Log.v(TAG, "onReceiveResult");
            MainActivity activity = (MainActivity) getSupportActivity();
            if (activity == null) {
                return;
            }
            activity.setProgressBarIndeterminateVisibility(Boolean.TRUE);

            switch (resultCode) {
                case SyncService.STATUS_RUNNING: {
                    Log.v(TAG, "SyncService.STATUS_RUNNING");
                    activity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    // mSyncing = true;
                    break;
                }
                case SyncService.STATUS_FINISHED: {
                    Log.v(TAG, "SyncService.STATUS_FINISHED");
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
                    Log.v(TAG, "SyncService.STATUS_ERROR");
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

            // activity.updateRefreshStatus(mSyncing);
        }

        // @Override
        // public void onActivityCreated(Bundle savedInstanceState) {
        // super.onActivityCreated(savedInstanceState);
        // ((MainActivity) getSupportActivity()).updateRefreshStatus(mSyncing);
        // }
    }

}
