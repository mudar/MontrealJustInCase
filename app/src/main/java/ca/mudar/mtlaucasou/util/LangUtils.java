package ca.mudar.mtlaucasou.util;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

import ca.mudar.mtlaucasou.data.UserPrefs;

/**
 * Created by mudar on 09/09/16.
 */
public class LangUtils {

    /**
     * Force the configuration change to a locale different that the phone's.
     */
    public static void updateUiLanguage(Context context) {
        Locale locale = new Locale(UserPrefs.getInstance(context).getLanguage());
        Configuration config = new Configuration();
        config.locale = locale;
        Locale.setDefault(locale);
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }
}
