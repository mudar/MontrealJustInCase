
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.BaseMapFragment;
import ca.mudar.mtlaucasou.utils.Const;

public class EmergencyHostelsMapFragment extends BaseMapFragment {
    private static final String TAG = "EmergencyHostelsMapFragment";

    public EmergencyHostelsMapFragment() {
        super(Const.INDEX_ACTIVITY_EMERGENCY_HOSTELS);
    }

    public static WaterSuppliesMapFragment newInstance() {
        WaterSuppliesMapFragment map = new WaterSuppliesMapFragment();
        return map;
    }
}
