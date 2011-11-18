
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.utils.ActivityHelper;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItem;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
            AboutFragment about = new AboutFragment();
            fm.beginTransaction().add(android.R.id.content, about).commit();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setHomeButtonEnabled(true);
        }
    }

    /**
     * Handle ActionBar and menu buttons.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ActivityHelper activityHelper = ActivityHelper.createInstance(this);
        return activityHelper.onOptionsItemSelected(item);
    }

    /**
     * AboutFragment
     */
    public static class AboutFragment extends Fragment {

        public static AboutFragment newInstance() {
            AboutFragment about = new AboutFragment();

            return about;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            if (container == null) {
                return null;
            }

            View root = inflater.inflate(R.layout.fragment_about, container, false);
            MovementMethod method = LinkMovementMethod.getInstance();
            ((TextView) root.findViewById(R.id.about_links_1)).setMovementMethod(method);
            ((TextView) root.findViewById(R.id.about_links_2)).setMovementMethod(method);
            ((TextView) root.findViewById(R.id.about_links_3)).setMovementMethod(method);
            ((TextView) root.findViewById(R.id.about_links_4)).setMovementMethod(method);
            ((TextView) root.findViewById(R.id.about_links_5)).setMovementMethod(method);
            ((TextView) root.findViewById(R.id.about_links_credits)).setMovementMethod(method);

            return root;
        }
    }
}
