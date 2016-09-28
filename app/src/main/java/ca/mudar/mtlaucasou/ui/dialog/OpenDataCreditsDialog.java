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

package ca.mudar.mtlaucasou.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.ui.adapter.OpenDataCreditsAdapter;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class OpenDataCreditsDialog extends DialogFragment implements
        OpenDataCreditsAdapter.CreditsListCallback {

    private static final String TAG = makeLogTag("OpenDataLinkssDialogFragment ");

    public static OpenDataCreditsDialog newInstance() {
        return new OpenDataCreditsDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);

        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog
                .Builder(getActivity(), R.style.AppTheme_Dialog)
                .setTitle(R.string.dialog_open_data_title)
                .setAdapter(new OpenDataCreditsAdapter(getContext(),
                                R.layout.about_od_credits_item,
                                this),
                        null)
                .setNegativeButton(R.string.btn_close, null);

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }

        super.onDestroyView();
    }

    /**
     * Implements OpenDataCreditsAdapter.CreditsListCallback
     * Allows closing the dialog once user has selected a link
     */
    @Override
    public void onItemSelected() {
        getDialog().cancel();
    }
}
