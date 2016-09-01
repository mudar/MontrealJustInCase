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
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.MtlAuCasOuApp;
import ca.mudar.mtlaucasou.R;

public class GeoUtils {
    public static LatLng getCoordsLatLng(List<Double> coordinates) {
        try {
            return new LatLng(coordinates.get(1), coordinates.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Location findLocatioFromName(Context c, String name) throws IOException {
        Geocoder geocoder = new Geocoder(c);
        List<Address> adr;

        adr = geocoder.getFromLocationName(name, 10, Const.MAPS_GEOCODER_LIMITS[0],
                Const.MAPS_GEOCODER_LIMITS[1], Const.MAPS_GEOCODER_LIMITS[2],
                Const.MAPS_GEOCODER_LIMITS[3]);
        if (!adr.isEmpty()) {
            Address address = adr.get(0);
            if (((int) address.getLatitude() != 0) && ((int) address.getLongitude() != 0)) {
                Location location = new Location("mylocation");
                location.setLatitude(address.getLatitude());
                location.setLongitude(address.getLongitude());

                return location;
            }
        }

        return null;
    }

    /**
     * Get distance in Metric or Imperial units. Display changes depending on
     * the value: different approximationss in ft when > 1000. Very short
     * distances are not displayed to avoid problems with Location accuracy.
     *
     * @param c
     * @param fDistanceM The distance in Meters.
     * @return String Display the distance.
     */
    public static String getDistanceDisplay(Context c, float fDistanceM) {
        String sDistance;

        MtlAuCasOuApp app = (MtlAuCasOuApp) c.getApplicationContext();
        Resources res = c.getResources();
        String units = app.getUnits();

        if (units.equals(Const.PrefsValues.UNITS_IMP)) {
            /**
             * Imperial units system, Miles and Feet.
             */

            float fDistanceMi = fDistanceM / Const.UnitsDisplay.METER_PER_MILE;

            if (fDistanceMi + (Const.UnitsDisplay.ACCURACY_FEET_FAR / Const.UnitsDisplay.FEET_PER_MILE) < 1) {
                /**
                 * Display distance in Feet if less than one mile.
                 */
                int iDistanceFt = Math.round(fDistanceMi * Const.UnitsDisplay.FEET_PER_MILE);

                if (iDistanceFt <= Const.UnitsDisplay.MIN_FEET) {
                    /**
                     * Display "Less than 200 ft", which is +/- equal to the GPS
                     * accuracy.
                     */
                    sDistance = res.getString(R.string.placemark_distance_imp_min);
                } else {
                    /**
                     * When displaying in feet, we round up by 100 ft for
                     * distances greater than 1000 ft and by 100 ft for smaller
                     * distances. Example: 1243 ft becomes 1200 and 943 ft
                     * becomes 940 ft.
                     */
                    if (iDistanceFt > 1000) {
                        iDistanceFt = Math.round(iDistanceFt / Const.UnitsDisplay.ACCURACY_FEET_FAR)
                                * Const.UnitsDisplay.ACCURACY_FEET_FAR;
                    } else {
                        iDistanceFt = Math.round(iDistanceFt / Const.UnitsDisplay.ACCURACY_FEET_NEAR)
                                * Const.UnitsDisplay.ACCURACY_FEET_NEAR;
                    }
                    sDistance = String.format(res.getString(R.string.placemark_distance_imp_feet),
                            iDistanceFt);
                }
            } else {
                /**
                 * Display distance in Miles when greater than 1 mile.
                 */
                sDistance = String.format(res.getString(R.string.placemark_distance_imp),
                        fDistanceMi);
            }
        } else {
            /**
             * International Units system, Meters and Km.
             */

            if (fDistanceM <= Const.UnitsDisplay.MIN_METERS) {
                /**
                 * Display "Less than 100 m".
                 */
                sDistance = res.getString(R.string.placemark_distance_iso_min);
            } else {
                /**
                 * No need to have a constant for 1 Km = 1000 M
                 */
                float fDistanceKm = (fDistanceM / 1000);
                sDistance = String
                        .format(res.getString(R.string.placemark_distance_iso), fDistanceKm);
            }
        }

        return sDistance;
    }

}
