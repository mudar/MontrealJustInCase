
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.BaseListFragment;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.SpvmStations;
import ca.mudar.mtlaucasou.utils.Const;

public class SpvmStationsListFragment extends BaseListFragment {
    protected static final String TAG = "SpvmStationsListFragment";

    public SpvmStationsListFragment() {
        super(Const.INDEX_ACTIVITY_SPVM_STATIONS, SpvmStations.DEFAULT_SORT);
    }
    
    public static SpvmStationsListFragment newInstance() {
        SpvmStationsListFragment list = new SpvmStationsListFragment();
        return list;
    }
}
