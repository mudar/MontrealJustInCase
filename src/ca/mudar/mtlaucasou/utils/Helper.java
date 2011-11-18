/*
 * Mobitize for Android 
 * Payment Solutions for Mobile Platforms
 * 
 * Copyright (C) 2011 S.B. Canada <info@mobitize.com>
 * 
 * This file is part of Mobitize for Android
 * 
 * @author Mudar Noufal <mn@mudar.ca>
 */

package ca.mudar.mtlaucasou.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Helper {
    private static final String TAG = "Helper";

    public static String inputStreamToString(InputStream inputStream) {
        BufferedReader r;
        String resultString = "";
        try {
            r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            resultString = total.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultString;
    }

}