
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.BaseListFragment;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.FireHalls;
import ca.mudar.mtlaucasou.utils.Const;

public class FireHallsListFragment extends BaseListFragment {
    protected static final String TAG = "FireHallsListFragment";

    public FireHallsListFragment() {
        super(Const.INDEX_ACTIVITY_FIRE_HALLS, FireHalls.DEFAULT_SORT);
    }

    public static FireHallsListFragment newInstance() {
        FireHallsListFragment list = new FireHallsListFragment();
        return list;
    }
}
