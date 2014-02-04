package com.cbitlabs.geoip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private ArrayAdapter<JsonObject> historyAdaptor = null;
    private HashMap<Integer, String> historyMap;
    private int pageNum;

    public MainActivity() {
        pageNum = 0;
        historyMap = new HashMap<Integer, String>() {
            {
                put(R.id.item_ssid, "ssid");
                put(R.id.item_loc, "loc");
                put(R.id.item_created_at_human, "created_at_human");
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //start reporting geoIP in the background.
        Intent intent = new Intent(this, ReportIntentService.class);
        startService(intent);
        // create a history adapter for our list view
        if (historyAdaptor == null) {
            historyAdaptor = new ArrayAdapter<JsonObject>(this, 0) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null)
                        convertView = getLayoutInflater().inflate(R.layout.history_item, null);

                    JsonObject item = getItem(position);
                    for (Map.Entry<Integer, String> el : historyMap.entrySet()) {
                        convertView = setHistoryAdaptorText(convertView, item, el.getKey(), el.getValue());
                    }
                    return convertView;
                }
            };
        }

        // basic setup of the ListView and adapter
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(historyAdaptor);
        listView.setEmptyView(findViewById(R.id.empty_element));
        listView.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                load(false);
            }

        });
        load(false);
    }

    private View setHistoryAdaptorText(View convertView, JsonObject item, int id, String key) {
        TextView text = (TextView) convertView.findViewById(id);
        text.setText(item.get(key).toString().replaceAll("^\"|\"$", ""));
        return convertView;
    }

    // This "Future" tracks loading operations.
    // A Future is an object that manages the state of an operation
    // in progress that will have a "Future" result.
    // You can attach callbacks (setCallback) for when the result is ready,
    // or cancel() it if you no longer need the result.
    Future<JsonArray> loading;

    private void load(final boolean clear) {
        // don't attempt to load more if a load is already in progress
        if (loading != null && !loading.isDone() && !loading.isCancelled())
            return;
        if (clear) {
            pageNum = 0;
        }


        String url = Util.getHistoryUrl(Util.getUUID(this), pageNum);
        Log.i(Util.LOG_TAG, "Requesting history with url: " + url);

        pageNum++;

        // This request loads a URL as JsonArray and invokes
        // a callback on completion.
        loading = Ion.with(this, url)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        // this is called back onto the ui thread, no Activity.runOnUiThread or Handler.post necessary.
                        if (e != null) {
                            Log.i(Util.LOG_TAG, e.toString());
                            Toast.makeText(MainActivity.this, "Error loading history", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.i(Util.LOG_TAG, "Found history: " + result.toString());
//                        Toast.makeText(MainActivity.this, "Loaded " + Integer.toString(result.size()) + " history items.", Toast.LENGTH_SHORT).show();

                        if (clear) { //clear after request returns
                            historyAdaptor.clear();
                        }

                        for (int i = 0; i < result.size(); i++) {
                            historyAdaptor.add(result.get(i).getAsJsonObject());
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.i(Util.LOG_TAG, "Settings!");
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;

            case R.id.refresHistory:
                load(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}