
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.BaseMapFragment;
import ca.mudar.mtlaucasou.utils.Const;

public class SpvmStationsMapFragment extends BaseMapFragment {
    private static final String TAG = "SpvmStationsMapFragment";

    public SpvmStationsMapFragment() {
        super(Const.INDEX_ACTIVITY_SPVM_STATIONS);
    }

    public static SpvmStationsMapFragment newInstance() {
        SpvmStationsMapFragment map = new SpvmStationsMapFragment();
        return map;
    }
}
