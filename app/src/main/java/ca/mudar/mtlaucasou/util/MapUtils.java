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

import android.content.Context;
import android.location.Location;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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
        if (map != null && PermissionUtils.checkLocationPermission(activity)) {
            map.setMyLocationEnabled(true);
        }
    }

    /**
     * Get the map marker icon (round buttons)
     *
     * @param type Selected map type {fire_halls|spvm_stations|water_supplies|emergency_hostels|health}
     * @return bitmap for MarkerOptions
     */
    public static BitmapDescriptor getMarkerIcon(@MapType String type) {
        switch (type) {
            case Const.MapTypes.FIRE_HALLS:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_fire_halls);
            case Const.MapTypes.SPVM_STATIONS:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_spvm);
            case Const.MapTypes.HEAT_WAVE:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_water_supplies);
            case Const.MapTypes.EMERGENCY_HOSTELS:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_emergency_hostels);
            case Const.MapTypes.HEALTH:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_hospitals);
        }
        return null;
    }

    /**
     * Get the app's colors for each section. Used for ProgressBar
     *
     * @param context Context to resolve resources
     * @param type Selected map type {fire_halls|spvm_stations|water_supplies|emergency_hostels|health}
     * @return the section's color
     */
    @ColorInt
    public static int getMapTypeColor(Context context, @MapType String type) {
        @ColorRes int color;
        switch (type) {
            case Const.MapTypes.FIRE_HALLS:
                color = R.color.color_fire_halls;
                break;
            case Const.MapTypes.SPVM_STATIONS:
                color = R.color.color_spvm;
                break;
            case Const.MapTypes.HEAT_WAVE:
                color = R.color.color_heat_wave;
                break;
            case Const.MapTypes.EMERGENCY_HOSTELS:
                color = R.color.color_emergency_hostels;
                break;
            case Const.MapTypes.HEALTH:
                color = R.color.color_health;
                break;
            default:
                color = R.color.color_primary;
        }
        return ContextCompat.getColor(context, color);
    }

    /**
     * Clean the HTML description provided by the city's data. Also removes duplicate title.
     *
     * @param descHtml placemark properties description
     * @param name     the placemark, to remove it from the description
     * @return Displayable description
     */
    public static String getCleanDescription(@NonNull String descHtml, @NonNull String name) {
        if (TextUtils.isEmpty(descHtml)) {
            return null;
        }
        return CompatUtils.fromHtml(descHtml.replace(name, ""))
                .toString()
                .trim();
    }

    /**
     * Add the Realm Placemarks to the map
     *
     * @param map        the Map object
     * @param placemarks list of Placemarks
     * @return List of markers added to the map
     */
    public static List<Marker> addPlacemarksToMap(GoogleMap map, List<? extends Placemark> placemarks) {
        final long startTime = System.currentTimeMillis();
        if (map == null || placemarks == null) {
            return null;
        }

        final List<MarkerOptions> markerOptionsList = new ArrayList<>();
        for (Placemark placemark : placemarks) {
            final LatLng position = placemark.getLatLng();
            final String title = placemark.getName();
            final String desc = MapUtils.getCleanDescription(placemark.getDescription(), title);

            if (position != null && !TextUtils.isEmpty(title)) {
                final MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .icon(MapUtils.getMarkerIcon(placemark.getMapType()))
                        .title(title);
                if (!TextUtils.isEmpty(desc)) {
                    markerOptions.snippet(desc);
                }
                markerOptionsList.add(markerOptions);
            }
        }

        // Add markers once all are ready
        final List<Marker> results = new ArrayList<>();
        for (MarkerOptions markerOptions : markerOptionsList) {
            results.add(map.addMarker(markerOptions));
        }

        Log.v(TAG, String.format("Added %1$d markers. Duration: %2$dms",
                markerOptionsList.size(),
                System.currentTimeMillis() - startTime));

        return results;
    }

    /**
     * Get the marker nearest to map center
     *
     * @param map          The GoogleMap
     * @param markers      List of markers contained by the map
     * @param snackbarView Anchor view used to show the snackbar message
     */
    public static void findAndShowNearestMarker(final GoogleMap map, List<Marker> markers, @Nullable View snackbarView) {
        if (map == null || markers == null) {
            return;
        }

        final LatLng mapCenter = map.getCameraPosition().target;
        final LatLngBounds visibleRegion = map.getProjection().getVisibleRegion().latLngBounds;

        // Find the nearest marker
        boolean hasVisibleMarker = false;
        Marker nearestMarker = null;
        float shortestDistance = 0f; // Compute distance of each marker once only
        for (Marker marker : markers) {
            final float distance = GeoUtils.distanceBetween(marker.getPosition(), mapCenter);

            if ((nearestMarker == null) || (Float.compare(distance, shortestDistance) < 0)) {
                nearestMarker = marker;
                shortestDistance = distance;
            }

            if (!hasVisibleMarker) {
                hasVisibleMarker = visibleRegion.contains(marker.getPosition());
            }
        }

        assert nearestMarker != null;
        if (hasVisibleMarker) {
            // Show the infoWindow of the nearest maker
            nearestMarker.showInfoWindow();
        } else if (snackbarView != null) {
            // Check if the map shows any markers, Otherwise, show a zoom-out message to show the nearest
            final LatLngBounds newVisibleRegion = visibleRegion.including(nearestMarker.getPosition());

            Snackbar.make(snackbarView, R.string.snackbar_empty_map_visible_region, Snackbar.LENGTH_LONG)
                    .setAction(R.string.btn_zoom_out, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final int padding = view.getResources()
                                    .getDimensionPixelSize(R.dimen.map_new_bounds_padding);

                            final CameraUpdate camera = CameraUpdateFactory
                                    .newLatLngBounds(newVisibleRegion, padding);
                            map.animateCamera(camera);
                        }
                    })
                    .show();
        }
    }

    /**
     * Get Montreal's LatLngBounds, to limit the mapview
     *
     * @return Bounds to limits the camera movement
     */
    public static LatLngBounds getDefaultBounds() {
        return new LatLngBounds(
                new LatLng(Const.MAPS_GEOCODER_LIMITS[0], Const.MAPS_GEOCODER_LIMITS[1]), // LowerLeft
                new LatLng(Const.MAPS_GEOCODER_LIMITS[2], Const.MAPS_GEOCODER_LIMITS[3])  // UpperRight
        );
    }

    /**
     * When first showing the map, move the camera to the user's location or to Montreal coordinates
     *
     * @param map      The GoogleMap
     * @param location User location. Null value defaults to Montreal coordinates
     */
    public static void moveCameraToInitialLocation(GoogleMap map, @Nullable Location location) {
        final LatLng target = (location == null) ? Const.MONTREAL_GEO_LAT_LNG :
                GeoUtils.locationToLatLng(location);

        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(target)
                        .bearing(Const.MONTREAL_NATURAL_NORTH_ROTATION)
                        .zoom(Const.ZOOM_DEFAULT)
                        .build()
                )
        );
        map.setLatLngBoundsForCameraTarget(MapUtils.getDefaultBounds());
    }

    /**
     * Move the camera to a Location obtained from the GeoLocator for a user search query.
     * Also adds a default Marker (pin) at the requested location.
     *
     * @param map      The GoogleMap
     * @param location The selection location to show at center
     * @param animate  With/out animation
     */
    public static void moveCameraToLocation(GoogleMap map, Location location, boolean animate) {
        if (map == null || location == null) {
            return;
        }
        final LatLng position = GeoUtils.locationToLatLng(location);

        if (location.getExtras() != null) {
            // Add a marker only if the location has a title
            final MarkerOptions markerOptions = new MarkerOptions()
                    .title(location.getExtras().getString(Const.BundleKeys.NAME))
                    .position(position);
            map.addMarker(markerOptions);
        }

        moveCameraToTarget(map, position, animate, null);
    }

    /**
     * Move the camera to a Placemark. Mainly for SuggestionsPlacemarks selected by the user
     * from search auto-complete
     *
     * @param map                The GoogleMap
     * @param placemark          The placemark to show at center
     * @param animate            With/out animation
     * @param cameraIdleListener Listener to be called after moving the camera,
     *                           useful to switch tabs AFTER the anim (to avoid hiccups)
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
     * @param map                The GoogleMap
     * @param target             The coordinates of the new center
     * @param animate            With/out animation
     * @param cameraIdleListener Listener to be called after moving the camera,
     *                           useful to switch tabs AFTER the anim (to avoid hiccups)
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
     * @param map The GoogleMap
     */
    public static void clearMap(GoogleMap map) {
        map.clear();
    }

}
