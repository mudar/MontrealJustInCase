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

import android.app.Activity;
import android.support.annotation.NonNull;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.data.UserPrefs;
import ca.mudar.mtlaucasou.ui.activity.EulaActivity;

public class EulaUtils {
    /**
     * Show the End User License Agreement on the first app launch
     *
     * @param activity The current Activity
     * @return true if EULA activity has been started, false if skipped (already accepted)
     */
    public static boolean showEulaIfNecessary(@NonNull Activity activity) {
        final boolean hasAcceptedEula = UserPrefs.getInstance(activity)
                .hasAcceptedEula();
        if (!hasAcceptedEula) {
            activity.startActivityForResult(EulaActivity.newIntent(activity, false),
                    Const.RequestCodes.EULA_ACCEPTED);
            return true;
        }

        return false;
    }
}
