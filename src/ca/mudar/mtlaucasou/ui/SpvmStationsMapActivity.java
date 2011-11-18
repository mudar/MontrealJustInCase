
package ca.mudar.mtlaucasou.ui;

import android.os.Bundle;

import ca.mudar.mtlaucasou.BaseMapActivity;
import ca.mudar.mtlaucasou.utils.Const;
import ca.mudar.mtlaucasou.R;

public class SpvmStationsMapActivity extends BaseMapActivity {
    protected static final String TAG = "SpvmStationsMapActivity";

    public SpvmStationsMapActivity() {
        super(Const.INDEX_ACTIVITY_SPVM_STATIONS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_spvm_stations);
    }
}
