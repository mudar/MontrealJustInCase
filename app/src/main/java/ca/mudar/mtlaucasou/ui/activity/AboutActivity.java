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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.ui.dialog.OpenDataCreditsDialog;
import ca.mudar.mtlaucasou.util.IntentUtils;

public class AboutActivity extends BaseActivity implements
        View.OnClickListener {

    public static Intent newIntent(Context context) {
        return new Intent(context, AboutActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setTitle(R.string.title_activity_about);
        setContentView(R.layout.activity_about);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);

        return true;
    }

    @Override
    protected boolean isShowTitleEnabled() {
        return false;
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();

        if (id == R.id.about_open_data) {
            OpenDataCreditsDialog
                    .newInstance()
                    .show(getSupportFragmentManager(), Const.FragmentTags.DIALOG_OD_CREDITS);
        } else if (id == R.id.about_source_code) {
            IntentUtils.showWebsite(this, R.string.url_github);
        } else if (id == R.id.about_credits_dev) {
            IntentUtils.showWebsite(this, R.string.url_mudar_ca);
        } else if (id == R.id.about_montreal_ouvert) {
            IntentUtils.showWebsite(this, R.string.url_montreal_ouvert);
        }
    }
}
