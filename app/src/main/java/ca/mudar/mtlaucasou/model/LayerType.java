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

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({
        LayerType.FIRE_HALLS,
        LayerType.SPVM_STATIONS,
        LayerType.EMERGENCY_HOSTELS,
        // Heat wave x4
        LayerType.AIR_CONDITIONING,
        LayerType.POOLS,
        LayerType.WADING_POOLS,
        LayerType.PLAY_FOUNTAINS,
        LayerType._HEAT_WAVE_MIXED,
        // Health x2
        LayerType.HOSPITALS,
        LayerType.CLSC
})
public @interface LayerType {
    String FIRE_HALLS = "fire_halls";
    String SPVM_STATIONS = "spvm_stations";
    String EMERGENCY_HOSTELS = "emergency_hostels";
    // Heat wave x4
    String AIR_CONDITIONING = "air_conditioning";
    String POOLS = "pools";
    String WADING_POOLS = "wading_pools";
    String PLAY_FOUNTAINS = "play_fountains";
    String _HEAT_WAVE_MIXED = "water_supplies";
    // Health x2
    String HOSPITALS = "hospitals";
    String CLSC = "clsc";
}
