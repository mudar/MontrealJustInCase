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
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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

import java.util.List;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.io.ApiClient;
import ca.mudar.mtlaucasou.io.GeoApiService;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.Placemark;
import ca.mudar.mtlaucasou.model.geojson.PointsFeature;
import ca.mudar.mtlaucasou.model.geojson.PointsFeatureCollection;
import ca.mudar.mtlaucasou.ui.adapter.PlacemarkInfoWindowAdapter;
import ca.mudar.mtlaucasou.ui.adapter.PlacemarkSearchAdapter;
import ca.mudar.mtlaucasou.util.MapUtils;
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
        Callback<PointsFeatureCollection> {

    private static final String TAG = makeLogTag("MainActivity");
    private static final long BOTTOM_BAR_ANIM_DURATION = 200;

    private GoogleMap vMap;
    private View vMarkerInfoWindow;
    private Toolbar vToolbar;
    private SearchView vSearchView;
    private PlacemarkSearchAdapter mSearchAdapter;
    @MapType
    private String mMapType;
    private Realm realm;
    private Handler mHandler = new Handler(); // Waits for the BottomBar anim

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();

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

        realm.close();
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
        vToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(vToolbar);
    }

    private void setupSearchView(final Menu menu) {
        // Get the toolbar menu SearchView
        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        vSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        mSearchAdapter = new PlacemarkSearchAdapter(this);
        vSearchView.setSuggestionsAdapter(mSearchAdapter);

        vSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                // Submit query for selected auto-complete suggestion
                vSearchView.setQuery(mSearchAdapter.getSuggestion(position), true);
                return true;
            }
        });
        vSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MenuItemCompat.collapseActionView(searchMenuItem);
                // User pressed submit button or clicked suggestion
//                mListener.onSearchQuerySubmitted(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    /**
     * Show the bottom bar navigation items
     */
    private void setupBottomBar() {
        final BottomBar bottomBar = (BottomBar) findViewById(R.id.bottom_bar);
        assert bottomBar != null;
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
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

        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
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

        vMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Log.v(TAG, "onCameraIdle");
            }
        });

        MapUtils.enableMyLocation(this, vMap);

        loadMapData(mMapType);

//        final ViewTreeObserver observer = vToolbar.getViewTreeObserver();
//        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                Log.v(TAG, "onGlobalLayout");
//
//                vMap.setPadding(0, vToolbar.getHeight(), 0, 0);
//                try {
//                    observer.removeOnGlobalLayoutListener(this);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }


    public void setMapType(final @MapType String type, long delay) {
        mMapType = type;

        if (vMap != null) {
            // Remove previous markers
            vMap.clear();

            mHandler.removeCallbacksAndMessages(null);
            // Wait for the BottomBar animation to end before loading data
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadMapData(type);
                }
            }, delay);
        }
    }

    /**
     * Load the cached data, or request download
     *
     * @param type the current MapType
     */
    private void loadMapData(@MapType String type) {
        Log.v(TAG, "loadMapData");
        if (vMap == null) {
            return;
        }

        // First, query the Realm db for the current mapType
        final LatLngBounds bounds = vMap.getProjection().getVisibleRegion().latLngBounds;
        final RealmQuery<Placemark> query = realm
                .where(Placemark.class)
                .equalTo(Placemark.FIELD_MAP_TYPE, mMapType);

        if (query.count() > 0) {
            // Has cached data
            final RealmResults<Placemark> placemarks = query
                    .greaterThan(Placemark.FIELD_COORDINATES_LAT, bounds.southwest.latitude)
                    .greaterThan(Placemark.FIELD_COORDINATES_LNG, bounds.southwest.longitude)
                    .lessThan(Placemark.FIELD_COORDINATES_LAT, bounds.northeast.latitude)
                    .lessThan(Placemark.FIELD_COORDINATES_LNG, bounds.northeast.longitude)
                    .findAll();

            final long startTime = System.currentTimeMillis();
            final int count = MapUtils.addPlacemarsToMap(vMap, mMapType, placemarks);

            Log.v(TAG, String.format("Added %1$d markers. Duration: %2$dms",
                    count,
                    System.currentTimeMillis() - startTime));
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
        Log.v(TAG, "downloadApiData "
                + String.format("type = %s", type));

        GeoApiService apiService = ApiClient.getService();
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
        cacheMapData(response.body().getFeatures(), mMapType);
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

    /**
     * Save the downloaded data to the Realm db
     *
     * @param pointsFeatures
     * @param mapType
     */
    private void cacheMapData(List<PointsFeature> pointsFeatures, @MapType String mapType) {
        realm.beginTransaction();
        // Loop over results, convert GeoJSON to Realm then add to db
        for (PointsFeature feature : pointsFeatures) {
            realm.copyToRealm(new Placemark.Builder(feature, mapType).build());
        }
        realm.commitTransaction();
    }
}
