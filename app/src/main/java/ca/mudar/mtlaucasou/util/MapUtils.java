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
import android.content.pm.PackageManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.model.MapType;
import ca.mudar.mtlaucasou.model.Placemark;

public class MapUtils {

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

    @DrawableRes
    public static int getMapTypeIcon(@MapType String type) {
        switch (type) {
            case Const.MapTypes.FIRE_HALLS:
                return R.drawable.ic_fire_hall;
            case Const.MapTypes.SVPM_STATIONS:
                return R.drawable.ic_spvm;
            case Const.MapTypes.WATER_SUPPLIES:
                return R.drawable.ic_water_supplies;
            case Const.MapTypes.EMERGENCY_HOSTELS:
                return R.drawable.ic_emergency_hostels;
        }
        return 0;
    }

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
     * @param type       mapType to get the right marker icon
     * @param placemarks list of Placemarks
     * @return Number of markers added to the visible region
     */
    public static int addPlacemarsToMap(GoogleMap map, @MapType String type, List<Placemark> placemarks) {
        if (map == null || placemarks == null) {
            return 0;
        }

        final List<MarkerOptions> markers = new ArrayList<>();
        for (Placemark placemark : placemarks) {
            final LatLng latLng = placemark.getCoordinates().getLatLng();
            final String title = placemark.getProperties().getName();
            final String desc = MapUtils.getCleanDescription(placemark.getProperties().getDescription(), title);

            if (latLng != null && !TextUtils.isEmpty(title)) {
                final MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .icon(MapUtils.getMarkerIcon(type))
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

        return markers.size();
    }

    public static LatLngBounds getDefaultBounds() {
        return new LatLngBounds(
                new LatLng(Const.MAPS_GEOCODER_LIMITS[0], Const.MAPS_GEOCODER_LIMITS[1]), // LowerLeft
                new LatLng(Const.MAPS_GEOCODER_LIMITS[2], Const.MAPS_GEOCODER_LIMITS[3])  // UpperRight
        );
    }
}
