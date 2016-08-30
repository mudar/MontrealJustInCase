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

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.model.geojson.PointsFeature;
import ca.mudar.mtlaucasou.model.geojson.PointsFeatureCollection;
import ca.mudar.mtlaucasou.model.geojson.base.GeometryFeatureCollection;

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

    public static BitmapDescriptor getMarkerIcon(Const.MapTypes type) {
        switch (type) {
            case FIRE_HALLs:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_fire_halls);
            case SVPM_STATIONS:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_spvm);
            case WATER_SUPPLIES:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_water_supplies);
            case EMERGENCY_HOSTELS:
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_emergency_hostels);
        }
        return null;
    }

    public static String getCleanDescription(@NonNull String descHtml, @NonNull String name) {
        if (TextUtils.isEmpty(descHtml)) {
            return null;
        }
        return Html.fromHtml(descHtml.replace(name, ""))
                .toString()
                .trim();
    }

    public static int addFeatureCollectionMarkers(GoogleMap map, Const.MapTypes type, GeometryFeatureCollection collection) {
        final PointsFeatureCollection featureCollection = (PointsFeatureCollection) collection;

        int total = 0;
        if (featureCollection.getFeatures() != null) {

            for (PointsFeature feature : featureCollection.getFeatures()) {
                final LatLng latLng = GeoUtils.getCoordsLatLng(feature.getGeometry().getCoordinates());
                final String title = feature.getProperties().getName();
                final String desc = MapUtils.getCleanDescription(feature.getProperties().getDescription(), title);

                if (latLng != null && !TextUtils.isEmpty(title)) {
                    final MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .icon(MapUtils.getMarkerIcon(type))
                            .title(title);
                    if (!TextUtils.isEmpty(desc)) {
                        markerOptions.snippet(desc);
                    }
                    map.addMarker(markerOptions);
                    total++;
                }
            }
        }

        return total;
    }

    public static LatLngBounds getDefaultBounds() {
        return new LatLngBounds(
                new LatLng(Const.MAPS_GEOCODER_LIMITS[0], Const.MAPS_GEOCODER_LIMITS[1]), // LowerLeft
                new LatLng(Const.MAPS_GEOCODER_LIMITS[2], Const.MAPS_GEOCODER_LIMITS[3])  // UpperRight
        );
    }
}
