
package ca.mudar.mtlaucasou.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

//AppHelper mAppHelper = (AppHelper) getApplicationContext();
//Activity: ((AppHelper)getApplication()).setBalance(9.99);

public class AppHelper extends Application {
    protected static final String TAG = "AppHelper";

    private Location mLocation;
    private String mLanguage;
    private Toast mToast;

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        Log.v(TAG,
                "setLocation. Lat = " + location.getLatitude() + ". Lng = "
                        + location.getLongitude());
        this.mLocation = location;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String lang) {
        Log.v(TAG, "setLanguage = " + lang);
        this.mLanguage = lang;
    }

    public void showToastText(int res, int duration) {
        mToast.setText(res);
        mToast.setDuration(duration);
        mToast.show();
    }

    public void showToastText(String msg, int duration) {
        mToast.setText(msg);
        mToast.setDuration(duration);
        mToast.show();
    }

    // TODO: verify possible memory leakage of the following code
    private static AppHelper instance = null;

    public static AppHelper getInstance() {
        checkInstance();
        return instance;
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        SharedPreferences prefs = getSharedPreferences(Const.APP_PREFS_NAME, Context.MODE_PRIVATE);
        mLanguage = prefs.getString(Const.PrefsNames.LANGUAGE, Locale.getDefault().getLanguage());
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }
}
