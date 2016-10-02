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

package ca.mudar.mtlaucasou.data;

import ca.mudar.mtlaucasou.model.RealmPlacemark;
import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class RealmSchemaMigration implements RealmMigration {
    private static final String TAG = makeLogTag("RealmSchemaMigration");

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();

        if (oldVersion == 10 && newVersion == 11) {
            /**
             * v11 added string field `dataType` to v10
             */
            final RealmObjectSchema placemarkSchema = schema.get("RealmPlacemark");
            // Add the `dataType` field, and set its value to `mapType`.
            // Placemarks in v10 didn't have mixed types yet.
            placemarkSchema
                    .addField(RealmPlacemark.FIELD_DATA_TYPE, String.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set(RealmPlacemark.FIELD_DATA_TYPE,
                                    obj.getString(RealmPlacemark.FIELD_MAP_TYPE));
                        }
                    });
            oldVersion++;
        }
    }
}
