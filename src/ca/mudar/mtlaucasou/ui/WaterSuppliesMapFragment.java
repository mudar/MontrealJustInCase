
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.BaseMapFragment;
import ca.mudar.mtlaucasou.utils.Const;

public class WaterSuppliesMapFragment extends BaseMapFragment {
    private static final String TAG = "WaterSuppliesMapFragment";

    public WaterSuppliesMapFragment() {
        super(Const.INDEX_ACTIVITY_WATER_SUPPLIES);
    }

    public static WaterSuppliesMapFragment newInstance() {
        WaterSuppliesMapFragment map = new WaterSuppliesMapFragment();
        return map;
    }
}
