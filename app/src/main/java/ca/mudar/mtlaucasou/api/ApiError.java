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

package ca.mudar.mtlaucasou.api;

import android.content.res.Resources;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import ca.mudar.mtlaucasou.Const;
import ca.mudar.mtlaucasou.R;

public class ApiError extends IOException {
    private final static String TAG = "ApiError";
    private final static int HOST_NOT_FOUND = 4040;

    private int code;
    private String message;

    public static ApiError getInstance(IOException e) {
        if (e instanceof UnknownHostException) {
            return new ApiError(HOST_NOT_FOUND, null);
        } else if (e instanceof ApiError) {
            return (ApiError) e;
        } else {
            return new ApiError(Const.UNKNOWN_VALUE, e.getMessage());
        }
    }

    public ApiError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public void showSnackbar(View v) {
        try {
            final Resources res = v.getResources();
            String msg;
            if (message != null && !message.isEmpty()) {
                msg = String.format(res.getString(R.string.snackbar_api_error_message),
                        message.toLowerCase(),
                        code);
            } else {
                if (isHostNotFound()) {
                    msg = String.format(res.getString(R.string.snackbar_host_unknown_error_message),
                            code);
                } else {
                    msg = String.format(res.getString(R.string.snackbar_api_error_code),
                            code);
                }
            }
            Snackbar.make(v,
                    msg,
                    Snackbar.LENGTH_SHORT)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isUnauthorized() {
        return this.code == HttpURLConnection.HTTP_UNAUTHORIZED;
    }

    public boolean isNotFound() {
        return this.code == HttpURLConnection.HTTP_NOT_FOUND;
    }

    public boolean isConflict() {
        return this.code == HttpURLConnection.HTTP_CONFLICT;
    }

    public boolean isHostNotFound() {
        return this.code == HOST_NOT_FOUND;
    }
}
