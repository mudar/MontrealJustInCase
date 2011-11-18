
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.BaseListFragment;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.EmergencyHostels;
import ca.mudar.mtlaucasou.utils.Const;

public class EmergencyHostelsListFragment extends BaseListFragment {
    protected static final String TAG = "EmergencyHostelsListFragment";

    public EmergencyHostelsListFragment() {
        super(Const.INDEX_ACTIVITY_EMERGENCY_HOSTELS, EmergencyHostels.DEFAULT_SORT);
    }

    public static EmergencyHostelsListFragment newInstance() {
        EmergencyHostelsListFragment list = new EmergencyHostelsListFragment();
        return list;
    }
}
