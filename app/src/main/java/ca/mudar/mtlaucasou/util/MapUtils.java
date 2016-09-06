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

package ca.mudar.mtlaucasou.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.Placemark;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class MapUtils {
    private static final String TAG = makeLogTag("MapUtils");

    public static void enableMyLocation(AppCompatActivity activity, GoogleMap map) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(activity, Const.RequestCodes.LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (map != null) {
            // Access to the location has been granted to the app.
            map.setMyLocationEnabled(true);
        }
    }

    /**
     * Get the map marker icon (round buttons)
     *
     * @param type
     * @return bitmap for MarkerOptions
     */
    public static BitmapDescriptor getMarkerIcon(@MapType String type) {
        switch (type) {
            case Const.MapTypes.FIRE_HALLS:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_fire_halls);
            case Const.MapTypes.SVPM_STATIONS:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_spvm);
            case Const.MapTypes.WATER_SUPPLIES:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_water_supplies);
            case Const.MapTypes.EMERGENCY_HOSTELS:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_emergency_hostels);
        }
        return null;
    }

    /**
     * Get the app's colors for each section. Used for ProgressBar
     *
     * @param context
     * @param type
     * @return
     */
    @ColorInt
    public static int getMapTypeColor(Context context, @MapType String type) {
        @ColorRes int color;
        switch (type) {
            case Const.MapTypes.FIRE_HALLS:
                color = R.color.color_fire_halls;
                break;
            case Const.MapTypes.SVPM_STATIONS:
                color = R.color.color_svpm;
                break;
            case Const.MapTypes.WATER_SUPPLIES:
                color = R.color.color_water_supplies;
                break;
            case Const.MapTypes.EMERGENCY_HOSTELS:
                color = R.color.color_emergency_hostels;
                break;
            default:
                color = R.color.color_accent;
        }
        return ContextCompat.getColor(context, color);
    }

    /**
     * Clean the HTML description provided by the city's data. Also removes duplicate title.
     *
     * @param descHtml
     * @param name
     * @return
     */
    public static String getCleanDescription(@NonNull String descHtml, @NonNull String name) {
        if (TextUtils.isEmpty(descHtml)) {
            return null;
        }
        return Html.fromHtml(descHtml.replace(name, ""))
                .toString()
                .trim();
    }

    /**
     * Add the Realm Placemarks to the map
     *
     * @param map        the Map object
     * @param placemarks list of Placemarks
     * @return Number of markers added to the visible region
     */
    public static int addPlacemarksToMap(GoogleMap map, List<? extends Placemark> placemarks) {
        final long startTime = System.currentTimeMillis();
        if (map == null || placemarks == null) {
            return 0;
        }

        final List<MarkerOptions> markers = new ArrayList<>();
        for (Placemark placemark : placemarks) {
            final LatLng latLng = placemark.getLatLng();
            final String title = placemark.getName();
            final String desc = MapUtils.getCleanDescription(placemark.getDescription(), title);

            if (latLng != null && !TextUtils.isEmpty(title)) {
                final MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .icon(MapUtils.getMarkerIcon(placemark.getMapType()))
                        .title(title);
                if (!TextUtils.isEmpty(desc)) {
                    markerOptions.snippet(desc);
                }
                markers.add(markerOptions);
            }
        }

        // Add markers once all are ready
        for (MarkerOptions markerOptions : markers) {
            map.addMarker(markerOptions);
        }

        Log.v(TAG, String.format("Added %1$d markers. Duration: %2$dms",
                markers.size(),
                System.currentTimeMillis() - startTime));

        return markers.size();
    }

    /**
     * Get Montreal's LatLngBounds, to limit the mapview
     *
     * @return
     */
    public static LatLngBounds getDefaultBounds() {
        return new LatLngBounds(
                new LatLng(Const.MAPS_GEOCODER_LIMITS[0], Const.MAPS_GEOCODER_LIMITS[1]), // LowerLeft
                new LatLng(Const.MAPS_GEOCODER_LIMITS[2], Const.MAPS_GEOCODER_LIMITS[3])  // UpperRight
        );
    }

    /**
     * Move the camera to a Location obtained from the GeoLocater for a user search query.
     * Also adds a default Marker (pin) at the requested location.
     *
     * @param map
     * @param location
     * @param animate
     * @param cameraIdleListener
     */
    public static void moveCameraToLocation(GoogleMap map, Location location, boolean animate, final GoogleMap.OnCameraIdleListener cameraIdleListener) {
        if (map == null || location == null) {
            return;
        }
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        final MarkerOptions markerOptions = new MarkerOptions()
                .title(location.getExtras().getString(Const.BundleKeys.NAME))
                .position(latLng);
        map.addMarker(markerOptions);

        moveCameraToTarget(map, latLng, animate, cameraIdleListener);
    }

    /**
     * Move the camera to a Placemark. Mainly for SuggestionsPlacemarks selected by the user
     * from search auto-complete
     *
     * @param map
     * @param placemark
     * @param animate
     * @param cameraIdleListener
     */
    public static void moveCameraToPlacemark(GoogleMap map, final Placemark placemark, boolean animate, final GoogleMap.OnCameraIdleListener cameraIdleListener) {
        if (map == null || placemark == null) {
            return;
        }

        moveCameraToTarget(map, placemark.getLatLng(), animate, cameraIdleListener);
    }

    /**
     * Move camera to LatLng.
     *
     * @param map
     * @param target
     * @param animate
     * @param cameraIdleListener
     */
    private static void moveCameraToTarget(@NonNull GoogleMap map, @NonNull LatLng target, boolean animate, final GoogleMap.OnCameraIdleListener cameraIdleListener) {
        final CameraUpdate camera = CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(target)
                        .zoom(Const.ZOOM_IN)
                        .bearing(map.getCameraPosition().bearing)
                        .build());

        if (animate) {
            if (cameraIdleListener != null) {
                map.animateCamera(camera, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        cameraIdleListener.onCameraIdle();
                    }

                    @Override
                    public void onCancel() {
                        cameraIdleListener.onCameraIdle();
                    }
                });
            } else {
                map.animateCamera(camera);
            }
        } else {
            map.moveCamera(camera);
            if (cameraIdleListener != null) {
                cameraIdleListener.onCameraIdle();
            }
        }
    }

    /**
     * Clear previous markers, restoring the user's search Markers
     *
     * @param map
     */
    public static void clearMap(GoogleMap map) {
        map.clear();
    }
}
