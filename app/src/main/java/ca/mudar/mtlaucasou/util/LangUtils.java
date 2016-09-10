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
import android.content.res.Configuration;

import java.util.Locale;

import ca.mudar.mtlaucasou.data.UserPrefs;

public class LangUtils {

    /**
     * Force the configuration change to a locale different that the phone's.
     */
    public static void updateUiLanguage(Context context) {
        Locale locale = new Locale(UserPrefs.getInstance(context).getLanguage());
        Configuration config = new Configuration();
        config.locale = locale;
        Locale.setDefault(locale);
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }
}
