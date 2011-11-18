
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.BaseListFragment;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.WaterSupplies;
import ca.mudar.mtlaucasou.utils.Const;

public class WaterSuppliesListFragment extends BaseListFragment {
    protected static final String TAG = "WaterSuppliesListFragment";

    public WaterSuppliesListFragment() {
        super(Const.INDEX_ACTIVITY_WATER_SUPPLIES, WaterSupplies.DEFAULT_SORT);
    }

    public static WaterSuppliesListFragment newInstance() {
        WaterSuppliesListFragment list = new WaterSuppliesListFragment();
        return list;
    }
}
