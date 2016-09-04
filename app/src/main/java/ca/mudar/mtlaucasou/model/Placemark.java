package ca.mudar.mtlaucasou.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mudar on 04/09/16.
 */
public interface Placemark {
    String getName();

    String getDescription();

    LatLng getLatLng();

    @MapType
    String getMapType();
}
