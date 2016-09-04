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

    public PlacemarksSearchView(Context context) {
        this(context, null);
    }

    public PlacemarksSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.searchViewStyle);
    }

    public PlacemarksSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setupListeners();
        setQueryHint(context.getString(R.string.search_hint));
    }

    public void setMenuItem(MenuItem searchMenuItem) {
        this.mSearchMenuItem = searchMenuItem;
    }

    private void setupListeners() {
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
            MenuItemCompat.collapseActionView(mSearchMenuItem);
//                mListener.onSearchQuerySubmitted(query);
        }
        return true;
    }

    /**
     * Implements SearchView.OnQueryTextListener
     *
     * @param query
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        MenuItemCompat.collapseActionView(mSearchMenuItem);
        // User pressed submit button or clicked suggestion
//                mListener.onSearchQuerySubmitted(query);
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
}
