
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.BaseMapFragment;
import ca.mudar.mtlaucasou.utils.Const;

public class FireHallsMapFragment extends BaseMapFragment {
    protected static final String TAG = "FireHallsMapFragment";

    public FireHallsMapFragment() {
        super(Const.INDEX_ACTIVITY_FIRE_HALLS);
    }

    public static FireHallsMapFragment newInstance() {
        FireHallsMapFragment map = new FireHallsMapFragment();
        return map;
    }
}
