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

package ca.mudar.mtlaucasou;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import ca.mudar.mtlaucasou.data.UserPrefs;
import ca.mudar.mtlaucasou.service.SyncService;
import ca.mudar.mtlaucasou.util.LangUtils;
import io.fabric.sdk.android.Fabric;

public class MtlAuCasOuApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        setupCrashlytics();
        setupRoom();
        setupLeakCanary();

        LangUtils.updateUiLanguage(this);

        UserPrefs.setDefaultValues(this);
    }

    private void setupCrashlytics() {
        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }
    }

    private void setupRoom() {
//        Realm.init(this);
//        RealmConfiguration config = new RealmConfiguration.Builder()
//                .name(Const.DATABASE_NAME)
//                .schemaVersion(Const.DATABASE_VERSION)
//                .build();
//        try {
//            Realm.migrateRealm(config, new RealmSchemaMigration());
//        } catch (FileNotFoundException ignored) {
//            // If the Realm file doesn't exist, just ignore.
//        }
//        Realm.setDefaultConfiguration(config);

        startService(SyncService.newIntent(this));
    }

    private void setupLeakCanary() {
//        LeakCanary.install(this);
    }
}
