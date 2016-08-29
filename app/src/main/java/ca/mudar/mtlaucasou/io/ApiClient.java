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

package ca.mudar.mtlaucasou.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import ca.mudar.mtlaucasou.BuildConfig;
import ca.mudar.mtlaucasou.model.geojson.PointsFeatureCollection;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static GeoApiService getService() {
        return getService(false);
    }

    @Deprecated
    public static GeoApiService getServiceLog() {
        return getService(true);
    }

    /**
     * Get the GeoJSON API service
     *
     * @param httpLogging
     * @return
     */
    private static GeoApiService getService(boolean httpLogging) {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(httpLogging ? HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE);

        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new HttpErrorInterceptor())
                .addInterceptor(interceptor)
                .build();

        final Gson gson = new GsonBuilder()
                .create();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(GeoApiService.class);
    }

    public static void getFireHalls(GeoApiService service, Callback<PointsFeatureCollection> cb) {
        service.getFireHalls()
                .enqueue(cb);
    }

    public static void getSpvmStations(GeoApiService service, Callback<PointsFeatureCollection> cb) {
        service.getSpvmStations()
                .enqueue(cb);
    }

    public static void getWaterSupplies(GeoApiService service, Callback<PointsFeatureCollection> cb) {
        service.getWaterSupplies()
                .enqueue(cb);
    }

    public static void getEmergencyHostels(GeoApiService service, Callback<PointsFeatureCollection> cb) {
        service.getEmergencyHostels()
                .enqueue(cb);
    }
}
