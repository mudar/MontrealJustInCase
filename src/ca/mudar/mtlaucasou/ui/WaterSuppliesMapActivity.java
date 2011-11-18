
package ca.mudar.mtlaucasou.ui;

import android.os.Bundle;

import ca.mudar.mtlaucasou.BaseMapActivity;
import ca.mudar.mtlaucasou.utils.Const;
import ca.mudar.mtlaucasou.R;

public class WaterSuppliesMapActivity extends BaseMapActivity {
    protected static final String TAG = "WaterSuppliesMapActivity";

    public WaterSuppliesMapActivity() {
        super(Const.INDEX_ACTIVITY_WATER_SUPPLIES);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_water_supplies);
    }

}
