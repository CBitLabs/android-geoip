package com.cbitlabs.geoipcollector;

/**
 * Created by stuart on 11/25/13.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.util.Map;

public class ReportingTask extends AsyncTask {

    private Context context;

    public ReportingTask(Context context) {
        Log.d(Util.TAG, "DataTask Created");

        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d(Util.TAG, "DataTask.doInBackground()");

        this.postReport();
        return null;
    }

    public void postReport() {
        Map<String, String> infoReport = Util.getReportInfo(this.context);
        JsonObject json = new JsonObject();
        for (Map.Entry<String, String> entry : infoReport.entrySet()){
            json.addProperty(entry.getKey(), entry.getValue());
        }
        Log.i(Util.TAG, "Posting json " + json.toString());
        Ion.with(context, Util.getReportServerUrl())
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject res) {
                        if (e != null) {
                            Log.i(Util.TAG, e.toString());
                        }
                        if (res != null) {
                            Log.i(Util.TAG, "Recieved " + res.toString());
                        }
                    }
                });
    }
}

