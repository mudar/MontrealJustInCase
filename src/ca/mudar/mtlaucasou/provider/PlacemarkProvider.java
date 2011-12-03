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
 * - Copied from IOSched
 * - Renamed package
 * - Replaced original content by SecurityServices
 * - Merged buildSimpleSelection() and buildExpandedSelection()
 */

package ca.mudar.mtlaucasou.provider;

import ca.mudar.mtlaucasou.provider.PlacemarkContract.ConditionedPlaces;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.EmergencyHostels;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.FireHalls;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.SpvmStations;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.WaterSupplies;
import ca.mudar.mtlaucasou.provider.PlacemarkDatabase.Tables;
import ca.mudar.mtlaucasou.services.SyncService;
import ca.mudar.mtlaucasou.utils.SelectionBuilder;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Provider that stores {@link PlacemarkContract} data. Data is usually inserted
 * by {@link SyncService}, and queried by various {@link Activity} instances.
 */
public class PlacemarkProvider extends ContentProvider {

    private PlacemarkDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int FIRE_HALLS = 110;
    private static final int FIRE_HALLS_ID = 111;

    private static final int SPVM_STATIONS = 120;
    private static final int SPVM_STATIONS_ID = 121;

    private static final int WATER_SUPPLIES = 130;
    private static final int WATER_SUPPLIES_ID = 131;

    private static final int EMERGENCY_HOSTELS = 140;
    private static final int EMERGENCY_HOSTELS_ID = 141;

    private static final int CONDITIONED_PLACES = 150;
    private static final int CONDITIONED_PLACES_ID = 151;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PlacemarkContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "fire_halls", FIRE_HALLS);
        matcher.addURI(authority, "fire_halls/*", FIRE_HALLS_ID);

        matcher.addURI(authority, "spvm_stations", SPVM_STATIONS);
        matcher.addURI(authority, "spvm_stations/*", SPVM_STATIONS_ID);

        matcher.addURI(authority, "water_supplies", WATER_SUPPLIES);
        matcher.addURI(authority, "water_supplies/*", WATER_SUPPLIES_ID);

        matcher.addURI(authority, "emergency_hostels", EMERGENCY_HOSTELS);
        matcher.addURI(authority, "emergency_hostels/*", EMERGENCY_HOSTELS_ID);

        matcher.addURI(authority, "conditioned_places", CONDITIONED_PLACES);
        matcher.addURI(authority, "conditioned_places/*", CONDITIONED_PLACES_ID);

        return matcher;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FIRE_HALLS:
                return FireHalls.CONTENT_TYPE;
            case FIRE_HALLS_ID:
                return FireHalls.CONTENT_ITEM_TYPE;
            case SPVM_STATIONS:
                return SpvmStations.CONTENT_TYPE;
            case SPVM_STATIONS_ID:
                return SpvmStations.CONTENT_ITEM_TYPE;
            case WATER_SUPPLIES:
                return WaterSupplies.CONTENT_TYPE;
            case WATER_SUPPLIES_ID:
                return WaterSupplies.CONTENT_ITEM_TYPE;
            case EMERGENCY_HOSTELS:
                return EmergencyHostels.CONTENT_TYPE;
            case EMERGENCY_HOSTELS_ID:
                return EmergencyHostels.CONTENT_ITEM_TYPE;
            case CONDITIONED_PLACES:
                return ConditionedPlaces.CONTENT_TYPE;
            case CONDITIONED_PLACES_ID:
                return ConditionedPlaces.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case FIRE_HALLS: {
                db.insertOrThrow(Tables.FIRE_HALLS, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return FireHalls.buildFireHallUri(values.getAsString(FireHalls.PLACEMARK_ID));
            }
            case SPVM_STATIONS: {
                db.insertOrThrow(Tables.SPVM_STATIONS, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return SpvmStations.buildSpvmStationUri(values
                        .getAsString(SpvmStations.PLACEMARK_ID));
            }
            case WATER_SUPPLIES: {
                db.insertOrThrow(Tables.WATER_SUPPLIES, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return WaterSupplies.buildWaterSupplyUri(values
                        .getAsString(WaterSupplies.PLACEMARK_ID));
            }
            case EMERGENCY_HOSTELS: {
                db.insertOrThrow(Tables.EMERGENCY_HOSTELS, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return EmergencyHostels.buildEmergencyHostelUri(values
                        .getAsString(EmergencyHostels.PLACEMARK_ID));
            }
            case CONDITIONED_PLACES: {
                db.insertOrThrow(Tables.CONDITIONED_PLACES, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return ConditionedPlaces.buildConditionedPlaceUri(values
                        .getAsString(ConditionedPlaces.PLACEMARK_ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mOpenHelper = new PlacemarkDatabase(context);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        final int match = sUriMatcher.match(uri);
        final SelectionBuilder builder = buildExpandedSelection(uri, match);

        Cursor c = builder.where(selection, selectionArgs).query(db, projection, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).update(db, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final int match = sUriMatcher.match(uri);
        return buildExpandedSelection(uri, match);
    }

    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case FIRE_HALLS: {
                return builder.table(Tables.FIRE_HALLS);
            }
            case FIRE_HALLS_ID: {
                final String fireHallId = FireHalls.getFireHallId(uri);
                return builder.table(Tables.FIRE_HALLS).where(FireHalls.PLACEMARK_ID + "=?",
                        fireHallId);
            }
            case SPVM_STATIONS: {
                return builder.table(Tables.SPVM_STATIONS);
            }
            case SPVM_STATIONS_ID: {
                final String spvmStationId = SpvmStations.getSpvmStationId(uri);
                return builder.table(Tables.SPVM_STATIONS).where(SpvmStations.PLACEMARK_ID + "=?",
                        spvmStationId);
            }
            case WATER_SUPPLIES: {
                return builder.table(Tables.WATER_SUPPLIES);
            }
            case WATER_SUPPLIES_ID: {
                final String waterSupplyId = WaterSupplies.getWaterSupplyId(uri);
                return builder.table(Tables.WATER_SUPPLIES).where(
                        WaterSupplies.PLACEMARK_ID + "=?", waterSupplyId);
            }
            case EMERGENCY_HOSTELS: {
                return builder.table(Tables.EMERGENCY_HOSTELS);
            }
            case EMERGENCY_HOSTELS_ID: {
                final String emergencyHostelId = EmergencyHostels.getEmergencyHostelId(uri);
                return builder.table(Tables.EMERGENCY_HOSTELS).where(
                        EmergencyHostels.PLACEMARK_ID + "=?", emergencyHostelId);
            }
            case CONDITIONED_PLACES: {
                return builder.table(Tables.CONDITIONED_PLACES);
            }
            case CONDITIONED_PLACES_ID: {
                final String conditionedPlaceId = ConditionedPlaces.getConditionedPlaceId(uri);
                return builder.table(Tables.CONDITIONED_PLACES).where(
                        ConditionedPlaces.PLACEMARK_ID + "=?", conditionedPlaceId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
}
