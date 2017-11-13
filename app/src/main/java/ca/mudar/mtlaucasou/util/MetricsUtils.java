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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import ca.mudar.mtlaucasou.BuildConfig;
import ca.mudar.mtlaucasou.Const.MetricsContentTypes;
import ca.mudar.mtlaucasou.model.MetricsContentName;
import ca.mudar.mtlaucasou.model.MetricsContentType;

public class MetricsUtils {

    public static void logMapView(@MetricsContentName String mapType) {
        logView(mapType, MetricsContentTypes.MAP);
    }

    public static void logSettingsView() {
        logView(MetricsContentName.SETTINGS, MetricsContentTypes.SETTINGS);
    }

    public static void logAboutView(@MetricsContentName String name) {
        logView(name, MetricsContentTypes.ABOUT);
    }

    private static void logView(@MetricsContentName String contentName, @MetricsContentType String contentType) {
        if (BuildConfig.USE_CRASHLYTICS) {
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName(contentName)
                    .putContentType(contentType));
        }
    }
}
