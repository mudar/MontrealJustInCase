package ca.mudar.mtlaucasou.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import ca.mudar.mtlaucasou.Const;

/**
 * Created by mudar on 08/09/16.
 */
public class CompatUtils {

    @TargetApi(Build.VERSION_CODES.N)
    public static Spanned fromHtml(String html) {
        if (Const.SUPPORTS_NOUGAT) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            return Html.fromHtml(html);
        }
    }
}
