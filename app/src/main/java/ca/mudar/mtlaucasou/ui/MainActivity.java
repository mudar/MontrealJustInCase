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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
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
import ca.mudar.mtlaucasou.ui.listener.SearchResultsManager;
import ca.mudar.mtlaucasou.ui.view.PlacemarksSearchView;
import ca.mudar.mtlaucasou.util.MapUtils;
import ca.mudar.mtlaucasou.util.NavigUtils;
import ca.mudar.mtlaucasou.util.PermissionUtils;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener,
        SearchResultsManager.MapUpdatesListener,
        Callback<PointsFeatureCollection> {

    private static final String TAG = makeLogTag("MainActivity");
    private static final long BOTTOM_BAR_ANIM_DURATION = 200;

    private GoogleMap vMap;
    private View vMarkerInfoWindow;
    private BottomBar mBottomBar;
    @MapType
    private String mMapType;
    private Realm mRealm;
    private Handler mHandler = new Handler(); // Waits for the BottomBar anim

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRealm = Realm.getDefaultInstance();

        setupToolbar();
        setupMap();
        setupBottomBar();

        setMapType(Const.MapTypes.FIRE_HALLS, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        setupSearchView(menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
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
                if (tabId == R.id.tab_fire_halls) {
                    setMapType(Const.MapTypes.FIRE_HALLS, BOTTOM_BAR_ANIM_DURATION);
                } else if (tabId == R.id.tab_spvm) {
                    setMapType(Const.MapTypes.SVPM_STATIONS, BOTTOM_BAR_ANIM_DURATION);
                } else if (tabId == R.id.tab_water_supplies) {
                    setMapType(Const.MapTypes.WATER_SUPPLIES, BOTTOM_BAR_ANIM_DURATION);
                } else if (tabId == R.id.tab_emergency_hostels) {
                    setMapType(Const.MapTypes.EMERGENCY_HOSTELS, BOTTOM_BAR_ANIM_DURATION);
                }
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

        vMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(Const.MONTREAL_GEO_LAT_LNG)
                        .bearing(Const.MONTREAL_NATURAL_NORTH_ROTATION)
                        .zoom(Const.ZOOM_DEFAULT)
                        .build()
                )
        );
        vMap.setLatLngBoundsForCameraTarget(MapUtils.getDefaultBounds());

        vMap.setInfoWindowAdapter(new PlacemarkInfoWindowAdapter(vMarkerInfoWindow));

        vMap.setOnCameraIdleListener(this);

        MapUtils.enableMyLocation(this, vMap);

        loadMapData(mMapType, true);
    }

    /**
     * Implements GoogleMap.OnCameraIdleListener
     */
    @Override
    public void onCameraIdle() {
        Log.v(TAG, "onCameraIdle");

        loadMapData(mMapType, true);
    }

    private void setMapType(final @MapType String type, long delay) {
        mMapType = type;

        if (vMap != null) {
            // Remove previous markers
            MapUtils.clearMap(vMap, type);

            mHandler.removeCallbacksAndMessages(null);
            // Wait for the BottomBar animation to end before loading data
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Map was already cleared, to show user something is happening!
                    loadMapData(type, false);
                }
            }, delay);
        }
    }

    /**
     * Load the cached data, or request download
     *
     * @param type     the current MapType
     * @param clearMap
     */
    private void loadMapData(@MapType String type, boolean clearMap) {
        Log.v(TAG, "loadMapData "
                + String.format("type = %s, clearMap = %s", type, clearMap));

        if (vMap == null) {
            return;
        }

        if (clearMap) {
            // Remove previous markers
            MapUtils.clearMap(vMap, type);
        }

        // First, query the Realm db for the current mapType
        final RealmQuery<RealmPlacemark> query = RealmQueries.queryPlacemarksByMapType(mRealm, mMapType);

        if (query.count() > 0) {
            // Has cached data
            final LatLngBounds bounds = vMap.getProjection().getVisibleRegion().latLngBounds;
            final RealmResults<RealmPlacemark> realmPlacemarks = RealmQueries.filterPlacemarksQueryByBounds(
                    query, bounds);

            MapUtils.addPlacemarksToMap(vMap, realmPlacemarks);
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
        loadMapData(mMapType, true);
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
        if (mMapType.equals(placemark.getMapType())) {
            // Selected placemark is of current type, data will be loaded in the activity's
            // onCameraIdle()
            cameraIdleListener = this;
        } else {
            // We need to switch mapType. Selecting the tab triggers a call to setMapType()
            // which clears map and loads data
            final int tabId = NavigUtils.getTabId(placemark.getMapType());

            cameraIdleListener = new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    mBottomBar.selectTabWithId(tabId);
                    if (mHandler != null) {
                        // Switching tabs sets delayed call to loadMapData()
                        // We remove it here to avoid duplicate data loading onCameraIdle()
                        mHandler.removeCallbacksAndMessages(null);
                    }
                }
            };
        }

        MapUtils.moveCameraToPlacemark(vMap, placemark, true, cameraIdleListener);
    }

    @Override
    public void moveCameraToLocation(Location location) {
        MapUtils.moveCameraToLocation(vMap, location, true, this);
    }

}
