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

import ca.mudar.mtlaucasou.BaseMapActivity;
import ca.mudar.mtlaucasou.utils.Const;
import ca.mudar.mtlaucasou.R;

import android.os.Bundle;

public class FireHallsMapActivity extends BaseMapActivity {
    protected static final String TAG = "FireHallsMapActivity";

    public FireHallsMapActivity() {
        super(Const.INDEX_ACTIVITY_FIRE_HALLS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_fire_halls);
    }
    
    @Override 
    public void onResume() {
        /**
         * Handle UI language changes.
         */
        getSupportActionBar().setTitle(R.string.app_label_fire_halls);
        super.onResume();
    }
}
