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

import ca.mudar.mtlaucasou.BaseListFragment;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.WaterSupplies;
import ca.mudar.mtlaucasou.utils.Const;

public class WaterSuppliesListFragment extends BaseListFragment {
    protected static final String TAG = "WaterSuppliesListFragment";

    public WaterSuppliesListFragment() {
        super(Const.INDEX_ACTIVITY_WATER_SUPPLIES, WaterSupplies.DEFAULT_SORT);
    }

    public static WaterSuppliesListFragment newInstance() {
        WaterSuppliesListFragment list = new WaterSuppliesListFragment();
        return list;
    }
}
