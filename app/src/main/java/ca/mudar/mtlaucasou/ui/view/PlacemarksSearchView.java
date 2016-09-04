package ca.mudar.mtlaucasou.ui.view;

import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.MenuItem;

import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.data.SuggestionsCursorHelper;
import ca.mudar.mtlaucasou.model.Placemark;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

/**
 * Created by mudar on 04/09/16.
 */
public class PlacemarksSearchView extends android.support.v7.widget.SearchView implements
        SearchView.OnSuggestionListener,
        SearchView.OnQueryTextListener {
    private static final String TAG = makeLogTag("PlacemarksSearchView");

    private MenuItem mSearchMenuItem;
    private SearchViewListener mListener;

    public PlacemarksSearchView(Context context) {
        this(context, null);
    }

    public PlacemarksSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.searchViewStyle);
    }

    public PlacemarksSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setQueryHint(context.getString(R.string.search_hint));
    }

    /**
     * Required for the SearchView to collapse the ActionView
     *
     * @param searchMenuItem
     */
    public void setMenuItem(MenuItem searchMenuItem) {
        this.mSearchMenuItem = searchMenuItem;
    }

    public void setListener(SearchViewListener listener) {
        this.mListener = listener;

        // Also set the other listeners
        setOnSuggestionListener(this);
        setOnQueryTextListener(this);
    }

    /**
     * Implements SearchView.OnSuggestionListener
     *
     * @param position
     * @return always false to ignore
     */
    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    /**
     * Implements SearchView.OnSuggestionListener
     * Handles the click on an auto-complete Placemark
     *
     * @param position
     * @return always true, to skip Intent lookup
     */
    @Override
    public boolean onSuggestionClick(int position) {
        final Placemark place = SuggestionsCursorHelper
                .cursorObjectToPlace(getSuggestionsAdapter().getCursor(), position);

        if (place != null) {
            setQuery(place.getName(), false);
            collapseActionView();
            mListener.onPlacemarkSuggestionClick(place);
        }
        return true;
    }

    /**
     * Implements SearchView.OnQueryTextListener
     * Handles the submit button for a word search
     *
     * @param query
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        collapseActionView();
        mListener.onAddressSearchSubmit(query);
        return false;
    }

    /**
     * SearchView.OnQueryTextListener
     *
     * @param newText
     * @return always false, to enable running the adapter's filter
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void collapseActionView() {
        if (mSearchMenuItem != null) {
            MenuItemCompat.collapseActionView(mSearchMenuItem);
        }
    }

    public interface SearchViewListener {
        public void onAddressSearchSubmit(String query);

        public void onPlacemarkSuggestionClick(Placemark placemark);
    }
}
