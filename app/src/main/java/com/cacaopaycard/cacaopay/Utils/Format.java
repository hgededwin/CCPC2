package com.cacaopaycard.cacaopay.Utils;

import android.util.Log;

import com.cacaopaycard.cacaopay.Constantes;

import org.json.JSONException;
import org.json.JSONObject;

public class Format {

    public static JSONObject toSintaxJSON(JSONObject response) throws JSONException {
        String sinDiag = response.toString().replaceAll("\\\\", "");
        String reformatRight = sinDiag.replaceAll("\"[{]","{");
        String reformatleftt = reformatRight.replaceAll("[}]\"","}");
        Log.e(Constantes.TAG, "reformat" + reformatleftt);
        JSONObject newResponse = new JSONObject(reformatleftt);
        return newResponse;
    }
}
