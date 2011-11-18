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

import ca.mudar.mtlaucasou.provider.PlacemarkContract.PlacemarkColumns;
import ca.mudar.mtlaucasou.ui.widgets.MyItemizedOverlay;
import ca.mudar.mtlaucasou.utils.ActivityHelper;
import ca.mudar.mtlaucasou.utils.AppHelper;
import ca.mudar.mtlaucasou.utils.Const;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseMapFragment extends Fragment implements LocationListener {
    protected static final String TAG = "BaseMapFragment";

    protected static int INDEX_OVERLAY_MY_LOCATION = 0;
    protected static int INDEX_OVERLAY_PLACEMARKS = 1;

    protected static int ZOOM_DEFAULT = 14;
    protected static int ZOOM_NEAR = 17;

    protected AppHelper mAppHelper;
    protected ActivityHelper mActivityHelper;
    protected MapView mMapView;
    protected MyLocationOverlay mLocationOverlay;
    protected MapController mMapController;
    protected LocationManager mLocationManager;

    protected GeoPoint mMapCenter = null;

    /**
     * Must be initialized by constructor
     */
    protected int indexSection;

    /**
     * Fragment display of list side view
     */
    // protected int mCurrentSelectedItemIndex = 0;
    // protected boolean mIsTablet = false;

    /**
     * BaseMapActivity Constructor
     * 
     * @param indexSection Used by {@link ActivityHelper} to get the each
     *            section's content
     */
    public BaseMapFragment(int indexSection) {
        this.indexSection = indexSection;
    }

    /**
     * Create the map view and restore saved instance (if any). {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");

        /**
         * Restore map center and zoom
         */
        int savedZoom = ZOOM_DEFAULT;
        if (savedInstanceState != null) {
            Log.v(TAG, "has saved instance");
            if (savedInstanceState.containsKey(Const.KEY_INSTANCE_COORDS)) {
                int[] coords = savedInstanceState.getIntArray(Const.KEY_INSTANCE_COORDS);
                mMapCenter = new GeoPoint(coords[0], coords[1]);
            }
            if (savedInstanceState.containsKey(Const.KEY_INSTANCE_ZOOM)) {
                savedZoom = savedInstanceState.getInt(Const.KEY_INSTANCE_ZOOM);
            }
        }

        View root = inflater.inflate(R.layout.fragment_map, container, false);

        mAppHelper = (AppHelper) getActivity().getApplicationContext();

        mActivityHelper = ActivityHelper.createInstance(getActivity());

        mMapView = (MapView) root.findViewById(R.id.map_view);

        mMapView.setBuiltInZoomControls(true);

        mMapController = mMapView.getController();
        mMapController.setZoom(savedZoom);

        mLocationManager = (LocationManager) getActivity().getApplicationContext()
                .getSystemService(MapActivity.LOCATION_SERVICE);

        initMap();

        return root;
    }

    // @Override
    // public void onActivityCreated(Bundle savedInstanceState) {
    // super.onActivityCreated(savedInstanceState);
    //
    // // This is a tablet if this view exists
    // View root = getSupportActivity().findViewById(R.id.map_root_landscape);
    // mIsTablet = (root != null);
    //
    // boolean isVisible = true;
    // if (savedInstanceState != null) {
    // if (savedInstanceState.containsKey(Const.KEY_INSTANCE_IS_VISIBLE_MAP)) {
    // isVisible =
    // savedInstanceState.getBoolean(Const.KEY_INSTANCE_IS_VISIBLE_MAP);
    // }
    // // mCurrentSelectedItemIndex =
    // // savedInstanceState.getInt("currentListIndex", -1);
    // }
    // FragmentManager fm = getSupportFragmentManager();
    // FragmentTransaction ft = fm.beginTransaction();
    // if (isVisible || mIsTablet) {
    // ft.show(this);
    // }
    // else {
    // ft.hide(this);
    // }
    // ft.commit();
    // }

    /**
     * Initialize Map: centre and load placemarks
     */
    protected void initMap() {
        Log.v(TAG, "initMap");

        mLocationOverlay = new MyLocationOverlay(getActivity().getApplicationContext(), mMapView);
        mLocationOverlay.enableCompass();
        mLocationOverlay.enableMyLocation();
        // mMapView.getOverlays().add(mLocationOverlay);
        mMapView.getOverlays().add(INDEX_OVERLAY_MY_LOCATION, mLocationOverlay);

        ArrayList<MapMarker> arMapMarker = fetchMapMarkers();

        Drawable drawable = getActivity().getResources().getDrawable(
                mActivityHelper.getMapPlacemarkIcon(indexSection));

        MyItemizedOverlay mItemizedOverlay = new MyItemizedOverlay(drawable,
                mMapView);

        if (arMapMarker.size() > 0) {
            Log.v(TAG, "Adding markers to map");
            for (MapMarker marker : arMapMarker) {
                OverlayItem overlayitem = new OverlayItem(marker.geoPoint, marker.name,
                        marker.address);
                mItemizedOverlay.addOverlay(overlayitem);
            }
            mMapView.getOverlays().add(INDEX_OVERLAY_PLACEMARKS, mItemizedOverlay);
        }

        initialAnimateToPoint();
    }

    /**
     * Set new map center
     * 
     * @param mapCenter
     */
    protected void animateToPoint(GeoPoint mapCenter) {
        if (mapCenter != null) {
            mMapCenter = mapCenter;
            mMapController.animateTo(mapCenter);
        }
    }

    /**
     * Initial map center animation on detected user location. If user is more
     * than minimum-distance from the city, center the map on Downtown. Also
     * defines the zoom.
     */
    protected void initialAnimateToPoint() {

        Log.v(TAG, "initialAnimateToPoint");

        List<String> enabledProviders = mLocationManager.getProviders(true);

        String coordinates[] = Const.MAPS_DEFAULT_COORDINATES;
        final double lat = Double.parseDouble(coordinates[0]);
        final double lng = Double.parseDouble(coordinates[1]);

        if ((mMapCenter == null) && enabledProviders.contains(LocationManager.NETWORK_PROVIDER)) {
            // Display user current location
            mLocationOverlay.runOnFirstFix(new Runnable() {
                public void run() {

                    GeoPoint userLocation = mLocationOverlay.getMyLocation();

                    Location loc = new Location(LocationManager.NETWORK_PROVIDER);
                    loc.setLatitude(userLocation.getLatitudeE6() / 1E6);
                    loc.setLongitude(userLocation.getLongitudeE6() / 1E6);
                    mAppHelper.setLocation(loc);

                    float[] results = new float[1];
                    android.location.Location.distanceBetween(lat, lng,
                            (userLocation.getLatitudeE6() / 1E6),
                            (userLocation.getLongitudeE6() / 1E6), results);

                    int distance = (int) (Math.round(results[0] / 1000));

                    if (distance > Const.MAPS_MIN_DISTANCE) {
                        userLocation = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
                    }

                    mMapCenter = userLocation;
                    mMapController.animateTo(userLocation);
                }
            });
        }
        else if (mMapCenter != null) {
            mMapController.setCenter(mMapCenter);
        }
        else {
            // Center on Downtown
            GeoPoint cityCenter = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
            mMapController.setCenter(cityCenter);
        }
    }

    /**
     * The query selected columns
     */
    static final String[] MAP_MARKER_PROJECTION = new String[] {
            BaseColumns._ID,
            PlacemarkColumns.PLACEMARK_NAME,
            PlacemarkColumns.PLACEMARK_ADDRESS,
            PlacemarkColumns.PLACEMARK_GEO_LAT,
            PlacemarkColumns.PLACEMARK_GEO_LNG
    };

    /**
     * Get the list of Placemarks from the database and return them as array to
     * be added as OverlayItems in the map.
     * 
     * @return ArrayList of MarpMarkers
     */
    protected ArrayList<MapMarker> fetchMapMarkers() {
        MapMarker mMapMarker;
        ArrayList<MapMarker> alLocations = new ArrayList<MapMarker>();

        Cursor cur = getActivity().getApplicationContext().getContentResolver()
                .query(mActivityHelper.getContentUri(indexSection),
                        MAP_MARKER_PROJECTION, null,
                        null, null);
        // TODO: verify cursor close vs manage
        getActivity().startManagingCursor(cur);

        if (cur.moveToFirst()) {
            final int columnId = cur.getColumnIndex(BaseColumns._ID);
            final int columnName = cur.getColumnIndex(PlacemarkColumns.PLACEMARK_NAME);
            final int columnAddress = cur.getColumnIndex(PlacemarkColumns.PLACEMARK_ADDRESS);
            final int columnGeoLat = cur.getColumnIndex(PlacemarkColumns.PLACEMARK_GEO_LAT);
            final int columnGeoLng = cur.getColumnIndex(PlacemarkColumns.PLACEMARK_GEO_LNG);

            do {
                mMapMarker = new MapMarker(cur.getInt(columnId), cur.getString(columnName),
                        cur.getString(columnAddress), cur.getDouble(columnGeoLat),
                        cur.getDouble(columnGeoLng));
                alLocations.add(mMapMarker);

            } while (cur.moveToNext());
        }

        return alLocations;
    }

    /**
     * Handle ActionBar and menu buttons. {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mActivityHelper.onOptionsItemSelected(item, indexSection);
    }

    /**
     * Enable user location (GPS) updates on map display. {@inheritDoc}
     */
    @Override
    public void onResume() {
        Log.v(TAG, "onResume");
        mLocationOverlay.enableMyLocation();

        // if (((BaseMapActivity) getSupportActivity()).isListVisiblePortrait())
        // {
        // Log.v(TAG, "isListVisiblePortrait");
        // FragmentManager fm = getSupportFragmentManager();
        // FragmentTransaction ft = fm.beginTransaction();
        // ft.hide(this);
        // ft.commit();
        // }

        // if (mMapCenter == null) {
        // mMapController.setZoom(ZOOM_DEFAULT);
        // }
        // else {
        // mMapController.setZoom(ZOOM_NEAR);
        // mMapController.setCenter(mMapCenter);
        // }

        super.onResume();
    }

    /**
     * Disable user location (GPS) updates on map hide. {@inheritDoc}
     */
    @Override
    public void onPause() {
        mLocationOverlay.disableMyLocation();
        super.onPause();
    }

    /**
     * Disable/Enable user location (GPS) updates on map hide/display.
     * {@inheritDoc}
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            mLocationOverlay.disableMyLocation();
        }
        else {
            mLocationOverlay.enableMyLocation();
        }
        super.onHiddenChanged(hidden);
    }

    /**
     * Save map center and zoom. {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        GeoPoint center = mMapView.getMapCenter();
        int[] coords = {
                center.getLatitudeE6(), center.getLongitudeE6()
        };

        outState.putIntArray(Const.KEY_INSTANCE_COORDS, coords);
        outState.putInt(Const.KEY_INSTANCE_ZOOM, mMapView.getZoomLevel());
        // outState.putBoolean(Const.KEY_INSTANCE_IS_VISIBLE_MAP, isVisible());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        Log.e(TAG, "onLocationChanged");
        mAppHelper.setLocation(location);
    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }

    /**
     * Data structure of a Placemark/MapMarker/OverlayItem
     */
    protected static class MapMarker {
        public final int id;
        public final String name;
        public final String address;
        public final GeoPoint geoPoint;

        public MapMarker(int id, String name, String address, Double geoLat, Double geoLng) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.geoPoint = new GeoPoint((int) (geoLat * 1E6), (int) (geoLng * 1E6));
        }
    }

    /**
     * Getter for the MapCenter GeoPoint.
     * 
     * @return coordinates of the map center
     */
    public GeoPoint getMapCenter() {
        return mMapCenter;
    }

    /**
     * Setter for the MapCenter GeoPoint. Centers map on the new location and
     * displays the ViewBallooon.
     * 
     * @param mapCenter The new location
     */
    public void setMapCenter(GeoPoint mapCenter) {
        Log.v(TAG, "Geo = " + mapCenter.getLatitudeE6() + "," + mapCenter.getLongitudeE6());
        animateToPoint(mapCenter);

        Overlay overlayPlacemarks = mMapView.getOverlays().get(INDEX_OVERLAY_PLACEMARKS);
        overlayPlacemarks.onTap(mapCenter, mMapView);
    }

}
