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

package ca.mudar.mtlaucasou.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.model.Placemark;
import ca.mudar.mtlaucasou.util.LogUtils;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class PlacemarkSearchAdapter extends CursorAdapter implements
        Filterable {
    private static final String TAG = makeLogTag("SearchAdapter");

    public static final int THRESHOLD = 2;

    private static final String[] CURSOR_COLUMNS = new String[]{"_id", "term"};
    private static final int TERM_CURSOR_COLUMN_POSITION = 1;

    private final ResultsFilter filter;
    private final LayoutInflater inflater;
    private final ArrayList<PlaceSuggestion> mDataset;
    private Realm realm;

    public PlacemarkSearchAdapter(Context context) {
        super(context, null, true);

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.filter = new ResultsFilter();

        this.mDataset = new ArrayList<>();
    }

    public void setDataset(List<PlaceSuggestion> data) {
        mDataset.clear();
        if (data != null) {
            mDataset.addAll(data);
        }

        final MatrixCursor matrixCursor = getCursor(mDataset);
        swapCursor(matrixCursor);
    }

    public String getSuggestion(int position) {
        try {
            if (getCursor().moveToPosition(position)) {
                return getCursor().getString(TERM_CURSOR_COLUMN_POSITION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.search_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final int position = cursor.getPosition();

        if (position < mDataset.size()) {
            final SuggestionViewHolder holder = new SuggestionViewHolder(view);
            holder.setValue(mDataset.get(position));
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    /**
     * Convert ArrayList into a MatrixCursor. Needed for the SearchView which supports Cursors only.
     *
     * @param data
     * @return
     */
    private MatrixCursor getCursor(ArrayList<PlaceSuggestion> data) {
        if (data == null || data.size() == 0) {
            return null;
        }

        final MatrixCursor matrixCursor = new MatrixCursor(CURSOR_COLUMNS);

        int i = 0;
        for (PlaceSuggestion place : data) {
            matrixCursor.addRow(new Object[]{i++, place.getName()});
        }
        return matrixCursor;
    }

    /**
     * The auto-complete ViewHolder
     */
    private class SuggestionViewHolder {
        PlaceSuggestion item;
        TextView vTitle;

        public SuggestionViewHolder(View view) {
            this.vTitle = (TextView) view.findViewById(R.id.title);
        }

        public void setValue(PlaceSuggestion placemark) {
            this.item = placemark;
            this.vTitle.setText(placemark.getName());
        }
    }

    /**
     * The search suggestions filter
     */
    public class ResultsFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            try {
                final Realm realm = Realm.getDefaultInstance();

                if (constraint != null && constraint.length() >= THRESHOLD) {
                    final RealmQuery<Placemark> query = realm
                            .where(Placemark.class)
                            .contains(Placemark.FIELD_PROPERTIES_NAME, String.valueOf(constraint), Case.INSENSITIVE);

                    results.count = (int) query.count();
                    if (query.count() > 0) {
                        final RealmResults<Placemark> placemarks = query
                                .findAll();
                        final List<PlaceSuggestion> suggestions = new ArrayList<>();
                        for (Placemark placemark : placemarks) {
                            suggestions.add(new PlaceSuggestion.Builder(placemark).build());
                        }
                        Collections.sort(suggestions);
                        results.values = suggestions;
                    } else {
                        results.values = null;
                    }
                }

                realm.close();

            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.REMOTE_LOG(e);
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            setDataset((List<PlaceSuggestion>) results.values);
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            if (resultValue instanceof MatrixCursor) {
                MatrixCursor cursor = (MatrixCursor) resultValue;
                return cursor.getString(TERM_CURSOR_COLUMN_POSITION);
            }

            return super.convertResultToString(resultValue);
        }
    }

    /**
     * Realm doesn't allow access to objects on Worker/UI threads, so we need to convert
     * to non-realm objects.
     * This allows handling realm calls in performFiltering() and then accessing data onBindView().
     * And cleaner calls to realm.close()
     */
    private static class PlaceSuggestion implements Comparable<PlaceSuggestion> {
        String name;
        LatLng latLng;
        String mapType;

        public PlaceSuggestion() {
            // Empty constructor
        }

        public String getName() {
            return name;
        }

        public LatLng getLatLng() {
            return latLng;
        }

        public String getMapType() {
            return mapType;
        }

        private PlaceSuggestion(Builder builder) {
            this.name = builder.name;
            this.latLng = builder.latLng;
            this.mapType = builder.mapType;
        }

        @Override
        public int compareTo(PlaceSuggestion other) {
            return name.compareTo(other.name);
        }

        public static class Builder {
            String name;
            LatLng latLng;
            String mapType;

            public Builder(Placemark placemark) {
                this.name = placemark.getProperties().getName();
                this.latLng = placemark.getCoordinates().getLatLng();
                this.mapType = placemark.getMapType();
            }

            public PlaceSuggestion build() {
                return new PlaceSuggestion(this);
            }
        }
    }
}
