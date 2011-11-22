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

package ca.mudar.mtlaucasou.utils;

import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.utils.Const.UnitsDisplay;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Helper {
    private static final String TAG = "Helper";

    public static String inputStreamToString(InputStream inputStream) {
        BufferedReader r;
        String resultString = "";
        try {
            r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            resultString = total.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultString;
    }

    public static String getDistanceDisplay(Context c, float fDistanceM) {
        String sDistance;

        AppHelper appHelper = (AppHelper) c.getApplicationContext();
        Resources res = c.getResources();
        String units = appHelper.getUnits();

        if (units.equals(Const.PrefsValues.UNITS_IMP)) {
            /**
             * Imperial units system, Miles and Feet.
             */

            float fDistanceMi = fDistanceM / UnitsDisplay.METER_PER_MILE;

            if (fDistanceMi + (UnitsDisplay.ACCURACY_FEET_FAR / UnitsDisplay.FEET_PER_MILE) < 1) {
                /**
                 * Display distance in Feet if less than one mile.
                 */
                int iDistanceFt = Math.round(fDistanceMi * UnitsDisplay.FEET_PER_MILE);

                if (iDistanceFt <= UnitsDisplay.MIN_FEET) {
                    /**
                     * Display "Less than 200 ft", which is +/- equal to the GPS
                     * accuracy.
                     */
                    sDistance = res.getString(R.string.placemark_distance_imp_min);
                }
                else {
                    /**
                     * When displaying in feet, we round up by 100 ft for
                     * distances greater than 1000 ft and by 100 ft for smaller
                     * distances. Example: 1243 ft becomes 1200 and 943 ft
                     * becomes 940 ft.
                     */
                    if (iDistanceFt > 1000) {
                        iDistanceFt = Math.round(iDistanceFt / UnitsDisplay.ACCURACY_FEET_FAR)
                                * UnitsDisplay.ACCURACY_FEET_FAR;
                    }
                    else {
                        iDistanceFt = Math.round(iDistanceFt / UnitsDisplay.ACCURACY_FEET_NEAR)
                                * UnitsDisplay.ACCURACY_FEET_NEAR;
                    }
                    sDistance = String.format(res.getString(R.string.placemark_distance_imp_feet),
                            iDistanceFt);
                }
            }
            else {
                /**
                 * Display distance in Miles when greater than 1 mile.
                 */
                sDistance = String.format(res.getString(R.string.placemark_distance_imp),
                        fDistanceMi);
            }
        }
        else {
            /**
             * International Units system, Meters and Km.
             */

            if (fDistanceM <= UnitsDisplay.MIN_METERS) {
                /**
                 * Display "Less than 100 m".
                 */
                sDistance = res.getString(R.string.placemark_distance_iso_min);
            }
            else {
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
