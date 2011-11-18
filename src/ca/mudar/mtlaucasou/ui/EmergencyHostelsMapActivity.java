
package ca.mudar.mtlaucasou.ui;

import android.os.Bundle;

import ca.mudar.mtlaucasou.BaseMapActivity;
import ca.mudar.mtlaucasou.utils.Const;
import ca.mudar.mtlaucasou.R;

public class EmergencyHostelsMapActivity extends BaseMapActivity {
    protected static final String TAG = "EmergencyHostelsMapActivity";

    public EmergencyHostelsMapActivity() {
        super(Const.INDEX_ACTIVITY_EMERGENCY_HOSTELS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_emergency_hostels);
    }

}
