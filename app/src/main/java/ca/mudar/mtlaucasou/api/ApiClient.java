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

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ca.mudar.mtlaucasou.BuildConfig;
import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.model.geojson.MixedPolygonGeometry;
import ca.mudar.mtlaucasou.model.geojson.MultiPolygon;
import ca.mudar.mtlaucasou.model.geojson.PointsFeatureCollection;
import ca.mudar.mtlaucasou.model.geojson.Polygon;
import ca.mudar.mtlaucasou.model.jsonapi.HelloApi;
import ca.mudar.mtlaucasou.util.LogUtils;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Response;
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
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory.
                        of(MixedPolygonGeometry.class, Const.ApiFields.TYPE).
                        registerSubtype(Polygon.class, Const.ApiValues.TYPE_POLYGON).
                        registerSubtype(MultiPolygon.class, Const.ApiValues.TYPE_MULTI_POLYGON)
                )
                .create();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(GeoApiService.class);
    }

    @Nullable
    public static Response<HelloApi> hello(GeoApiService service) {
        try {
            return service.hello()
                    .execute();
        } catch (IOException e) {
            LogUtils.REMOTE_LOG(e);
        }

        return null;
    }

    public static void hello(GeoApiService service, Callback<HelloApi> cb) {
        service.hello()
                .enqueue(cb);
    }

    @Nullable
    public static Response<PointsFeatureCollection> getPlacemarks(GeoApiService service, String url) {
        try {
            return service.getPlacemarks(url)
                    .execute();
        } catch (IOException e) {
            LogUtils.REMOTE_LOG(e);
        }

        return null;
    }

    public static void getPlacemarks(GeoApiService service, String url, Callback<PointsFeatureCollection> cb) {
        service.getPlacemarks(url)
                .enqueue(cb);
    }
}
