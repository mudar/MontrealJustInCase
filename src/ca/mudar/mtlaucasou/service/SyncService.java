/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications:
 * - Imported from IOSched 
 * - Changed package name
 * - Changed parsed data
 * - Removed SharedPreferences and versions management
 * - Removed the ResultReceiver
 */

package ca.mudar.mtlaucasou.service;

import ca.mudar.mtlaucasou.io.LocalExecutor;
import ca.mudar.mtlaucasou.io.RemoteExecutor;
import ca.mudar.mtlaucasou.io.RemotePlacemarksHandler;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.EmergencyHostels;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.FireHalls;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.SpvmStations;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.WaterSupplies;
import ca.mudar.mtlaucasou.utils.Const;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.format.DateUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Background {@link Service} that synchronizes data living in
 * {@link PlacemarkProvider}. Reads data from remote sources
 */
public class SyncService extends IntentService {
    private static final String TAG = "SyncService";

    public static final String EXTRA_STATUS_RECEIVER =
            "ca.mudar.mtlaucasou.extra.STATUS_RECEIVER";

    public static final int STATUS_RUNNING = 0x1;
    public static final int STATUS_ERROR = 0x2;
    public static final int STATUS_FINISHED = 0x3;

    private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;

    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";

    private LocalExecutor mLocalExecutor;
    private RemoteExecutor mRemoteExecutor;

    public SyncService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final HttpClient httpClient = getHttpClient(this);
        final ContentResolver resolver = getContentResolver();

        mLocalExecutor = new LocalExecutor(getResources(), resolver);
        mRemoteExecutor = new RemoteExecutor(httpClient, resolver);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");

        final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);
        if (receiver != null) {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
        }

        final Context context = this;

        try {
            // Bulk of sync work, performed by executing several fetches from
            // local and online sources.

            final long startLocal = System.currentTimeMillis();

            /**
             * Four assets files to load, so progress goes by 25%.
             */
            Bundle bundle = new Bundle();
            bundle.putInt(Const.KEY_BUNDLE_PROGRESS_INCREMENT, 25);

            // Parse values from local cache first, since SecurityServices copy
            // or network might be down.

            receiver.send(STATUS_RUNNING, bundle);
            mLocalExecutor.execute(context, "casernes-pompiers.kml",
                    new RemotePlacemarksHandler(FireHalls.CONTENT_URI, true));

            receiver.send(STATUS_RUNNING, bundle);
            mLocalExecutor.execute(context, "postes-spvm.kml",
                    new RemotePlacemarksHandler(SpvmStations.CONTENT_URI, true));

            receiver.send(STATUS_RUNNING, bundle);
            mLocalExecutor.execute(context, "points-eau.kml", new RemotePlacemarksHandler(
                    WaterSupplies.CONTENT_URI));

            receiver.send(STATUS_RUNNING, bundle);
            mLocalExecutor.execute(context, "centres-hebergement-urgence.kml",
                    new RemotePlacemarksHandler(EmergencyHostels.CONTENT_URI));

            Log.v(TAG, "Sync duration: " + (System.currentTimeMillis() - startLocal) + " ms");

            // TODO: update data from remote source
            // Always hit remote SecurityServices for any updates
            // final long startRemote = System.currentTimeMillis();
            // mRemoteExecutor
            // .executeGet(WORKSHEETS_URL, new
            // RemoteSecurityServicesHandler(mRemoteExecutor));
            // Log.d(TAG, "remote sync took " + (System.currentTimeMillis() -
            // startRemote) + "ms");

        } catch (Exception e) {
            Log.e(TAG, "Problem while syncing", e);

            if (receiver != null) {
                /**
                 * Pass back error to surface listener
                 */
                final Bundle bundle = new Bundle();
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }

        if (receiver != null) {
            receiver.send(STATUS_FINISHED, Bundle.EMPTY);
        }

        // Announce success to any surface listener
        // Log.v(TAG, "sync finished");
    }

    /**
     * Generate and return a {@link HttpClient} configured for general use,
     * including setting an application-specific user-agent string.
     */
    public static HttpClient getHttpClient(Context context) {
        final HttpParams params = new BasicHttpParams();

        // Use generous timeouts for slow mobile networks
        HttpConnectionParams.setConnectionTimeout(params, 20 * SECOND_IN_MILLIS);
        HttpConnectionParams.setSoTimeout(params, 20 * SECOND_IN_MILLIS);

        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpProtocolParams.setUserAgent(params, buildUserAgent(context));

        final DefaultHttpClient client = new DefaultHttpClient(params);

        client.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) {
                // Add header to accept gzip content
                if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
            }
        });

        client.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) {
                // Inflate any responses compressed with gzip
                final HttpEntity entity = response.getEntity();
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                            response.setEntity(new InflatingEntity(response.getEntity()));
                            break;
                        }
                    }
                }
            }
        });

        return client;
    }

    /**
     * Build and return a user-agent string that can identify this application
     * to remote servers. Contains the package name and version code.
     */
    private static String buildUserAgent(Context context) {
        try {
            final PackageManager manager = context.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

            // Some APIs require "(gzip)" in the user-agent string.
            return info.packageName + "/" + info.versionName
                    + " (" + info.versionCode + ") (gzip)";
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Simple {@link HttpEntityWrapper} that inflates the wrapped
     * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
     */
    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

}
