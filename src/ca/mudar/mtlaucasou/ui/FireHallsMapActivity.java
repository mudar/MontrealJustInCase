
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.BaseMapActivity;
import ca.mudar.mtlaucasou.utils.Const;
import ca.mudar.mtlaucasou.R;

import android.os.Bundle;

public class FireHallsMapActivity extends BaseMapActivity {
    protected static final String TAG = "FireHallsMapActivity";

    public FireHallsMapActivity() {
        super(Const.INDEX_ACTIVITY_FIRE_HALLS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_fire_halls);
    }
}
