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

package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.BaseMapFragment;
import ca.mudar.mtlaucasou.utils.Const;

public class SpvmStationsMapFragment extends BaseMapFragment {
    private static final String TAG = "SpvmStationsMapFragment";

    public SpvmStationsMapFragment() {
        super(Const.INDEX_ACTIVITY_SPVM_STATIONS);
    }

    public static SpvmStationsMapFragment newInstance() {
        SpvmStationsMapFragment map = new SpvmStationsMapFragment();
        return map;
    }
}
