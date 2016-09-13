/*
    Montréal Just in Case
    Copyright (C) 2011  Mudar Noufal <mn@mudar.ca>

    Geographic locations of public safety services. A Montréal Open Data
    project.

    This file is part of Montréal Just in Case.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.mudar.mtlaucasou.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.data.UserPrefs;

public class EulaActivity extends BaseActivity implements View.OnClickListener {
    private static final String ASSETS_URI = "file:///android_asset/";

    public static Intent newIntent(Context context, boolean hasAcceptedEula) {
        final Intent intent = new Intent(context, EulaActivity.class);

        final Bundle extras = new Bundle();
        extras.putBoolean(Const.BundleKeys.HAS_ACCEPTED_EULA, hasAcceptedEula);
        intent.putExtras(extras);

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final boolean hasAcceptedEula = getIntent().getBooleanExtra(Const.BundleKeys.HAS_ACCEPTED_EULA, false);

        setTitle(R.string.title_activity_eula);
        setContentView(R.layout.activity_eula);

        toggleEulaButton(hasAcceptedEula);

        loadWebView((WebView) findViewById(R.id.webview));

        findViewById(R.id.btn_accept_eula).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_accept_eula) {
            UserPrefs.getInstance(this).setHasAcceptedEula();
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    private void toggleEulaButton(boolean hasAcceptedEula) {
        findViewById(R.id.footer_buttons).setVisibility(hasAcceptedEula ? View.GONE : View.VISIBLE);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(hasAcceptedEula);
    }

    private void loadWebView(WebView v) {
        // Set basic style
        v.setBackgroundColor(ContextCompat.getColor(this, R.color.color_background));
        v.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // Open links in external browser
        v.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });

        // Load HTML content from assets
        v.loadUrl(ASSETS_URI + Const.LocalAssets.LICENSE);
    }
}
