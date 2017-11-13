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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.data.AppDatabase;
import ca.mudar.mtlaucasou.data.RealmQueries;
import ca.mudar.mtlaucasou.data.SuggestionsCursorHelper;
import ca.mudar.mtlaucasou.model.Placemark;
import ca.mudar.mtlaucasou.model.RealmPlacemark;
import ca.mudar.mtlaucasou.model.SuggestionsPlacemark;
import ca.mudar.mtlaucasou.util.LogUtils;
import ca.mudar.mtlaucasou.util.NavigUtils;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class PlacemarkSearchAdapter extends CursorAdapter implements
        Filterable {
    private static final String TAG = makeLogTag("SearchAdapter");

    private static final int THRESHOLD = 2;

    private final ResultsFilter mFilter;
    private final LayoutInflater mInflater;
    private final AppDatabase mDb;

    public PlacemarkSearchAdapter(Context context) {
        super(context, null, true);

        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mFilter = new ResultsFilter();
        this.mDb = AppDatabase.getAppDatabase(mContext.getApplicationContext());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.search_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final SuggestionViewHolder holder = new SuggestionViewHolder(view);
        holder.bind(cursor);
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private void setDataset(List<SuggestionsPlacemark> data) {
        final MatrixCursor matrixCursor = SuggestionsCursorHelper.initCursor(data);
        swapCursor(matrixCursor);
    }

    /**
     * The auto-complete ViewHolder
     */
    private class SuggestionViewHolder {
        final TextView vTitle;

        public SuggestionViewHolder(View view) {
            this.vTitle = (TextView) view.findViewById(R.id.title);
        }

        public void bind(Cursor cursor) {
            final Placemark place = SuggestionsCursorHelper.cursorObjectToPlace(cursor);

            this.vTitle.setText(place.getName());
            this.vTitle.setCompoundDrawablesWithIntrinsicBounds(
                    NavigUtils.getMapTypeIcon(place.getMapType()), 0, 0, 0);
        }
    }

    /**
     * The search suggestions filter
     */
    private class ResultsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            try {
                if (constraint != null && constraint.length() >= THRESHOLD) {
                    /*
                     * Query the database for filtered RealmPlacemarks,
                     * then convert results to SuggestionsPlacemark.
                     * Realm doesn't allow mixed use by Worker/UI threads, and this allows for
                     * safer calls to realm.close()
                     */
                    final List<RealmPlacemark> realmPlacemarks = RealmQueries
                            .queryPlacemarksByName(mDb, String.valueOf(constraint));

                    if (realmPlacemarks != null) {
                        results.count = realmPlacemarks.size();
                    }
                    if (results.count > 0) {
                        // The SuggestionsPlacemark list
                        final List<SuggestionsPlacemark> suggestions = new ArrayList<>();
                        for (RealmPlacemark realmPlacemark : realmPlacemarks) {
                            // Convert each RealmPlacemark then add it to the results list
                            suggestions.add(new SuggestionsPlacemark.Builder()
                                    .placemark(realmPlacemark)
                                    .build());
                        }
                        Collections.sort(suggestions);
                        results.values = suggestions;
                    } else {
                        results.values = null;
                    }
                }

                // Safe to close Realm now, the adapter will be using SuggestionsPlacemarks
                // to fill the MatrixCursor and bind the views.
//                realm.close();

            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.REMOTE_LOG(e);
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            setDataset((List<SuggestionsPlacemark>) results.values);
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            if (resultValue instanceof MatrixCursor) {
                return SuggestionsCursorHelper.getPlacemarkName((MatrixCursor) resultValue);
            }

            return super.convertResultToString(resultValue);
        }
    }
}
