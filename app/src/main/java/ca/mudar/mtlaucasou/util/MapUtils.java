package ca.mudar.mtlaucasou.util;

import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;

/**
 * Created by mudar on 26/08/16.
 */
public class MapUtils {
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
}
