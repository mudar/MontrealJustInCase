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

package ca.mudar.mtlaucasou.ui;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.api.ApiClient;
import ca.mudar.mtlaucasou.api.GeoApiService;
import ca.mudar.mtlaucasou.data.RealmQueries;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.Placemark;
import ca.mudar.mtlaucasou.model.RealmPlacemark;
import ca.mudar.mtlaucasou.model.geojson.PointsFeatureCollection;
import ca.mudar.mtlaucasou.ui.adapter.PlacemarkInfoWindowAdapter;
import ca.mudar.mtlaucasou.ui.listener.LocationUpdatesManager;
import ca.mudar.mtlaucasou.ui.listener.SearchResultsManager;
import ca.mudar.mtlaucasou.ui.view.PlacemarksSearchView;
import ca.mudar.mtlaucasou.util.MapUtils;
import ca.mudar.mtlaucasou.util.NavigUtils;
import ca.mudar.mtlaucasou.util.PermissionUtils;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        SearchResultsManager.MapUpdatesListener,
        Callback<PointsFeatureCollection>,
        LocationUpdatesManager.LocationUpdatesCallbacks {

    private static final String TAG = makeLogTag("MainActivity");
    private static final long BOTTOM_BAR_ANIM_DURATION = 200L; // 200ms
    private static final long PROGRESS_BAR_ANIM_DURATION = 750L; // 750ms

    private GoogleMap vMap;
    private View vMarkerInfoWindow;
    private CircleProgressBar vProgressBar;
    private BottomBar mBottomBar;
    @MapType
    private String mMapType;
    private Realm mRealm;
    private Handler mHandler = new Handler(); // Waits for the BottomBar anim
    private LocationUpdatesManager mLocationManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        vProgressBar = (CircleProgressBar) findViewById(R.id.progressBar);

        mRealm = Realm.getDefaultInstance();

        setupToolbar();
        setupMap();
        setupBottomBar();

        setMapType(Const.MapTypes.FIRE_HALLS, 0);
    }

    protected void onStart() {
        mLocationManger.onStart();

        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        setupSearchView(menu);
        return true;
    }

    @Override
    protected void onStop() {
        mLocationManger.onStop();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Const.RequestCodes.LOCATION_SETTINGS_CHANGE_REQUEST_CODE == requestCode) {
            onLocationSettingsActivityResult(resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != Const.RequestCodes.LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            MapUtils.enableMyLocation(this, vMap);
        } else {
            // Display the missing permission error dialog when the fragments resume.
        }
    }

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupSearchView(final Menu menu) {
        // Get the toolbar menu SearchView
        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        final PlacemarksSearchView searchView = (PlacemarksSearchView) MenuItemCompat.getActionView(searchMenuItem);
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
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        vMap = googleMap;

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
    private void loadMapData(@MapType String type) {
        Log.v(TAG, "loadMapData");

        if (vMap == null) {
            return;
        }

        // First, query the Realm db for the current mapType
        final RealmResults<RealmPlacemark> realmPlacemarks = RealmQueries.queryPlacemarksByMapType(mRealm, mMapType)
                .findAll();

        if (realmPlacemarks.size() > 0) {
            // Has cached data
            MapUtils.addPlacemarksToMap(vMap, realmPlacemarks);

            new Handler().postDelayed(new Runnable() {
                /**
                 * Delay hiding the progressbar for 750ms, avoids blink-effect on fast operations
                 */
                @Override
                public void run() {
                    toggleProgressBar(false);
                }
            }, PROGRESS_BAR_ANIM_DURATION);
        } else {
            // Need to download remote API data
            downloadApiData(type);
        }
    }

    /**
     * Request the GeoJSON data from the API, received in the Callback's onResponse()
     *
     * @param type
     */
    private void downloadApiData(@MapType String type) {
        final GeoApiService apiService = ApiClient.getService();
        switch (type) {
            case Const.MapTypes.FIRE_HALLS:
                ApiClient.getFireHalls(apiService, this);
                break;
            case Const.MapTypes.SVPM_STATIONS:
                ApiClient.getSpvmStations(apiService, this);
                break;
            case Const.MapTypes.WATER_SUPPLIES:
                ApiClient.getWaterSupplies(apiService, this);
                break;
            case Const.MapTypes.EMERGENCY_HOSTELS:
                ApiClient.getEmergencyHostels(apiService, this);
                break;
        }
    }

    /**
     * Implements Callback<PointsFeatureCollection>
     * Caches the results into Realm db then calls loadMapData
     *
     * @param call
     * @param response
     */
    @Override
    public void onResponse(Call<PointsFeatureCollection> call, Response<PointsFeatureCollection> response) {
        RealmQueries.cacheMapData(mRealm, response.body().getFeatures(), mMapType);
        loadMapData(mMapType);
    }

    /**
     * Implements Callback<PointsFeatureCollection>
     *
     * @param call
     * @param t
     */
    @Override
    public void onFailure(Call<PointsFeatureCollection> call, Throwable t) {
        Log.e(TAG, "onFailure");
        t.printStackTrace();
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
     * Implements LocationUpdatesManager.LocationUpdatesCallbacks
     *
     * @param status
     * @throws IntentSender.SendIntentException
     */
    @Override
    public void requestLocationSettingsChange(Status status) throws IntentSender.SendIntentException {
        status.startResolutionForResult(
                MainActivity.this,
                Const.RequestCodes.LOCATION_SETTINGS_CHANGE_REQUEST_CODE);
    }

    /**
     * Implements LocationUpdatesManager.LocationUpdatesCallbacks
     */
    @Override
    public void onLocationSettingsActivityResult(int resultCode, Intent data) {
        mLocationManger.onLocationSettingsResult(resultCode, data);
    }
}
