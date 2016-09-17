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
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.List;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.data.RealmQueries;
import ca.mudar.mtlaucasou.data.UserPrefs;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.Placemark;
import ca.mudar.mtlaucasou.model.RealmPlacemark;
import ca.mudar.mtlaucasou.ui.adapter.PlacemarkInfoWindowAdapter;
import ca.mudar.mtlaucasou.ui.listener.LocationUpdatesManager;
import ca.mudar.mtlaucasou.ui.listener.SearchResultsManager;
import ca.mudar.mtlaucasou.ui.view.PlacemarksSearchView;
import ca.mudar.mtlaucasou.util.EulaUtils;
import ca.mudar.mtlaucasou.util.MapUtils;
import ca.mudar.mtlaucasou.util.NavigUtils;
import ca.mudar.mtlaucasou.util.PermissionUtils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements
        OnMapReadyCallback,
        SearchResultsManager.MapUpdatesListener,
        LocationUpdatesManager.LocationUpdatesCallbacks {

    private static final String TAG = makeLogTag("MainActivity");
    private static final long BOTTOM_BAR_ANIM_DURATION = 200L; // 200ms
    private static final long PROGRESS_BAR_ANIM_DURATION = 750L; // 750ms

    private GoogleMap vMap;
    private View vMarkerInfoWindow;
    private View mSnackbarParent;
    private CircleProgressBar vProgressBar;
    private FloatingActionButton mMyLocationFAB;
    private BottomBar mBottomBar;
    @MapType
    private String mMapType;
    private Realm mRealm;
    private RealmChangeListener mRealmListener;
    private Handler mHandler = new Handler(); // Waits for the BottomBar anim
    private LocationUpdatesManager mLocationManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Avoid showing EULA twice on orientation change
            EulaUtils.showEulaIfNecessary(this);
        }

        setTitle(R.string.title_activity_main);
        setContentView(R.layout.activity_main);

        vProgressBar = (CircleProgressBar) findViewById(R.id.progressBar);
        mSnackbarParent = findViewById(R.id.map_wrapper);

        mRealm = Realm.getDefaultInstance();

        setupMap();
        setupFAB();
        setupBottomBar();

        setMapType(Const.MapTypes.FIRE_HALLS, 0);
    }

    protected void onStart() {
        super.onStart();

        mLocationManger.onStart();

        toggleMyLocationButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        setupSearchView(menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        mLocationManger.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove all change listeners to avoid leaks
        mRealm.removeAllChangeListeners();
        mRealm.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Const.RequestCodes.LOCATION_SETTINGS_CHANGE == requestCode) {
            onLocationSettingsActivityResult(resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Const.RequestCodes.LOCATION_PERMISSION) {
            if (PermissionUtils.checkLocationPermission(this)) {
                UserPrefs.getInstance(this).setPermissionDeniedForEver(true);

                mMyLocationFAB.show();
                MapUtils.enableMyLocation(this, vMap);
                mLocationManger.onLocationPermissionGranted();
            } else {
                PermissionUtils.showLocationRationaleOrSurrender(this, mSnackbarParent);

                mMyLocationFAB.hide();
                mMyLocationFAB.setVisibility(View.GONE);
            }
        }
    }

    private void setupSearchView(final Menu menu) {
        // Get the toolbar menu SearchView
        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        final PlacemarksSearchView searchView =
                (PlacemarksSearchView) MenuItemCompat.getActionView(searchMenuItem);

        searchView.setSearchMenuItem(searchMenuItem);
        searchView.setListener(new SearchResultsManager(MainActivity.this, this));
    }

    /**
     * Show the bottom bar navigation items
     */
    private void setupBottomBar() {
        mBottomBar = (BottomBar) findViewById(R.id.bottom_bar);
        assert mBottomBar != null;
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes final int tabId) {
                setMapType(NavigUtils.getMapTypeByTabId(tabId), BOTTOM_BAR_ANIM_DURATION);
            }
        });

        mBottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                if (vMap != null) {
                    vMap.animateCamera(CameraUpdateFactory.zoomTo(Const.ZOOM_OUT));
                }
            }
        });
    }

    private void setupFAB() {
        mMyLocationFAB = (FloatingActionButton) findViewById(R.id.fab);
        mMyLocationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionUtils.checkLocationPermission(MainActivity.this)) {
                    MapUtils.moveCameraToLocation(vMap, mLocationManger.getUserLocation(), true, null);
                } else {
                    PermissionUtils.requestLocationPermission(MainActivity.this);
                }
            }
        });
    }

    /**
     * Obtain the SupportMapFragment and get notified when the map is ready to be used.
     */
    private void setupMap() {
        mLocationManger = new LocationUpdatesManager(MainActivity.this, this);

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        vMarkerInfoWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null, false);
    }

    /**
     * Manipulate the map once available.
     *
     * @param googleMap The GoogleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        vMap = googleMap;
        vMap.getUiSettings().setMyLocationButtonEnabled(false);

        vMap.setInfoWindowAdapter(new PlacemarkInfoWindowAdapter(vMarkerInfoWindow));

        MapUtils.moveCameraToInitialLocation(vMap, null);
        MapUtils.enableMyLocation(this, vMap);

        mLocationManger.setGoogleMap(vMap);

        loadMapData(mMapType);
    }

    private void setMapType(final @MapType String type, long delay) {
        mMapType = type;

        toggleProgressBar(true);

        if (vMap != null) {
            // Remove previous markers
            MapUtils.clearMap(vMap);

            mHandler.removeCallbacksAndMessages(null);
            // Wait for the BottomBar animation to end before loading data
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Map was already cleared, to show user something is happening!
                    loadMapData(type);
                }
            }, delay);
        }
    }

    /**
     * Load the cached data, or request download
     *
     * @param type
     */
    private void loadMapData(@MapType final String type) {
        if (vMap == null) {
            return;
        }

        if (mRealmListener != null) {
            // Remove previously added RealmChangeListener
            mRealm.removeChangeListener(mRealmListener);
            mRealmListener = null;
        }

        // First, query the Realm db for the current mapType
        final RealmResults<RealmPlacemark> realmPlacemarks = RealmQueries
                .queryPlacemarksByMapType(mRealm, type)
                .findAll();

        if (realmPlacemarks.size() > 0) {
            // Has cached data
            final List<Marker> markers = MapUtils.addPlacemarksToMap(vMap, realmPlacemarks);

            new Handler().postDelayed(new Runnable() {
                /**
                 * Delay hiding the progressbar for 750ms, avoids blink-effect on fast operations.
                 * And allows findAndShowNearestMarker() to wait for the real center in case
                 * of camera animation.
                 */
                @Override
                public void run() {
                    toggleProgressBar(false);
                    MapUtils.findAndShowNearestMarker(vMap, markers, mSnackbarParent);
                }
            }, PROGRESS_BAR_ANIM_DURATION);
        } else {
            // Add a change listener for empty data only, to avoid showing empty maps.
            // Remote updates will be showed on tab changes. This is not an issue for our app
            // because of the low frequency/value of remote data updates.
            mRealmListener = new RealmChangeListener() {
                @Override
                public void onChange(Object element) {
                    loadMapData(type);
                }
            };
            mRealm.addChangeListener(mRealmListener);
        }
    }

    @Override
    public void moveCameraToPlacemark(Placemark placemark) {
        GoogleMap.OnCameraIdleListener cameraIdleListener;
        if (!mMapType.equals(placemark.getMapType())) {
            final int tabId = NavigUtils.getTabIdByMapType(placemark.getMapType());

            cameraIdleListener = new GoogleMap.OnCameraIdleListener() {
                /**
                 * We need to switch mapType after the camera animation. Selecting the tab
                 * triggers a call to setMapType() which clears map and loads data
                 */
                @Override
                public void onCameraIdle() {
                    mBottomBar.selectTabWithId(tabId);
                }
            };
        } else {
            // Selected placemark is of current type: ignore
            cameraIdleListener = null;
        }

        MapUtils.moveCameraToPlacemark(vMap, placemark, true, cameraIdleListener);
    }

    @Override
    public void moveCameraToLocation(Location location) {
        MapUtils.moveCameraToLocation(vMap, location, true, null);
    }

    private void toggleProgressBar(boolean visible) {
        if (visible) {
            vProgressBar.setColorSchemeColors(MapUtils.getMapTypeColor(this, mMapType));
            vProgressBar.setVisibility(View.VISIBLE);
        } else {
            vProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Verify if user has changed his mind about denying permission forever.
     * This method uses setVisibility() instead of show/hide methods.
     */
    private void toggleMyLocationButton() {
        if (PermissionUtils.checkPermissionWasDeniedForEver(this)) {
            mMyLocationFAB.hide();
        } else {
            mMyLocationFAB.show();
        }
    }

    /**
     * Implements LocationUpdatesManager.LocationUpdatesCallbacks
     *
     * @param status
     * @throws IntentSender.SendIntentException
     */
    @Override
    public void requestLocationSettingsChange(Status status)
            throws IntentSender.SendIntentException {
        status.startResolutionForResult(
                MainActivity.this,
                Const.RequestCodes.LOCATION_SETTINGS_CHANGE);
    }

    /**
     * Implements LocationUpdatesManager.LocationUpdatesCallbacks
     */
    @Override
    public void onLocationSettingsActivityResult(int resultCode, Intent data) {
        mLocationManger.onLocationSettingsResult(resultCode, data);
    }
}
