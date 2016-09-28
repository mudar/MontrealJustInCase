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

package ca.mudar.mtlaucasou.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import ca.mudar.mtlaucasou.R;

public class OpenDataLink {

    @StringRes
    private int title;
    @StringRes
    private int url;
    @DrawableRes
    private int icon;

    public OpenDataLink() {
        // Ignore, required since we have a custom constructor
    }

    private OpenDataLink(Builder builder) {
        this.title = builder.title;
        this.url = builder.url;
        this.icon = builder.icon;
    }

    @StringRes
    public int getTitle() {
        return title;
    }

    @StringRes
    public int getUrl() {
        return url;
    }

    @DrawableRes
    public int getIcon() {
        return icon;
    }

    public static class Builder {

        @StringRes
        private int title;
        @StringRes
        private int url;
        @DrawableRes
        private int icon;

        public Builder fromPosition(int position) {
            switch (position) {
                case 0:
                    this.title = R.string.about_od_fire_halls_title;
                    this.icon = R.drawable.ic_fire_hall;
                    this.url = R.string.about_od_fire_halls_url;
                    break;
                case 1:
                    this.title = R.string.about_od_spvm_stations_title;
                    this.icon = R.drawable.ic_spvm;
                    this.url = R.string.about_od_spvm_stations_url;
                    break;
                case 2:
                    this.title = R.string.about_od_water_supplies_title;
                    this.icon = R.drawable.ic_water_supplies;
                    this.url = R.string.about_od_water_supplies_url;
                    break;
                case 3:
                    this.title = R.string.about_od_emergency_hostels_title;
                    this.icon = R.drawable.ic_emergency_hostels;
                    this.url = R.string.about_od_emergency_hostels_url;
                    break;
//                case 5:
//                    this.title = R.string.about_od_hospitals_title;
//                    this.icon = R.drawable.ic_hospitals;
//                    this.url = R.string.about_od_hospitals_url;
//                    break;
                case 4:
                    // NOTE this should always be last, adjust indexes accordingly
                    this.title = R.string.about_od_license_title;
                    this.icon = R.drawable.ic_creative_commons;
                    this.url = R.string.about_od_license_url;
                    break;
            }

            return this;
        }

//        public Builder title(@StringRes int title) {
//            this.title = title;
//
//            return this;
//        }

//        public Builder url(@StringRes int url) {
//            this.url = url;
//
//            return this;
//        }

//        public Builder icon(@DrawableRes int icon) {
//            this.icon = icon;
//
//            return this;
//        }

        public OpenDataLink build() {
            return new OpenDataLink(this);
        }
    }
}
