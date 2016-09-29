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

package ca.mudar.mtlaucasou.api;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.model.geojson.PointsFeatureCollection;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface GeoApiService {
    String CONTENT_TYPE = "Content-type: application/json";

    @Headers({CONTENT_TYPE})
    @GET(Const.ApiPaths.GET_FIRE_HALLS)
    Call<PointsFeatureCollection> getFireHalls();

    @Headers({CONTENT_TYPE})
    @GET(Const.ApiPaths.GET_SPVM_STATIONS)
    Call<PointsFeatureCollection> getSpvmStations();

    @Headers({CONTENT_TYPE})
    @GET(Const.ApiPaths.GET_WATER_SUPPLIES)
    Call<PointsFeatureCollection> getWaterSupplies();

    @Headers({CONTENT_TYPE})
    @GET(Const.ApiPaths.GET_AIR_CONDITIONING)
    Call<PointsFeatureCollection> getAirConditioning();

    @Headers({CONTENT_TYPE})
    @GET(Const.ApiPaths.GET_EMERGENCY_HOSTELS)
    Call<PointsFeatureCollection> getEmergencyHostels();

    @Headers({CONTENT_TYPE})
    @GET(Const.ApiPaths.GET_HOSPITALS)
    Call<PointsFeatureCollection> getHospitals();
}
