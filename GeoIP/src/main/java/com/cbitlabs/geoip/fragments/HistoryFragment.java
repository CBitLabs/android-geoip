package com.cbitlabs.geoip.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.cbitlabs.geoip.EndlessScrollListener;
import com.cbitlabs.geoip.HistoryAdapter;
import com.cbitlabs.geoip.HistoryDetailActivity;
import com.cbitlabs.geoip.HistoryItem;
import com.cbitlabs.geoip.R;
import com.cbitlabs.geoip.ReportUtil;
import com.cbitlabs.geoip.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class HistoryFragment extends Fragment {

	private HistoryAdapter historyAdaptor = null;
	private int pageNum;

	Future<JsonArray> loading;

	public HistoryFragment() {
		pageNum = 0;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_history, null);
		historyAdaptor = new HistoryAdapter(getActivity(), 0);
		setListView(view);
		loadHistory(false);
		return view;
	}

	private void setListView(final View view) {
		ListView lv = (ListView) view.findViewById(R.id.list);
		lv.setAdapter(historyAdaptor);
		lv.setEmptyView(view.findViewById(R.id.empty_element));
		lv.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(final int page, final int totalItemsCount) {
				loadHistory(false);
			}

		});

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {

				HistoryItem historyItem = (HistoryItem) parent.getItemAtPosition(position);
				Intent intent = new Intent(view.getContext(), HistoryDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable(historyItem.SER_KEY, historyItem);
				intent.putExtras(bundle);
				startActivity(intent);
			}

		});

	}

	private void loadHistory(final boolean clear) {
		// don't attempt to load more if a load is already in progress
		if (loading != null && !loading.isDone() && !loading.isCancelled()) {
			return;
		}
		if (clear) {
			pageNum = 0;
		}

		String url = ReportUtil.getHistoryUrl(Util.getUUID(getActivity()), pageNum);
		Log.i(Util.LOG_TAG, "Requesting history with url: " + url);

		pageNum++;

		loading = Ion.with(getActivity(), url).asJsonArray().setCallback(new FutureCallback<JsonArray>() {
			@Override
			public void onCompleted(final Exception e, final JsonArray result) {
				if (e != null) {
					Log.i(Util.LOG_TAG, e.toString());
					Activity activity = getActivity();
					if (activity != null) {
						Toast.makeText(activity, "Error loading history", Toast.LENGTH_SHORT).show();
					}
					return;
				}
				Log.i(Util.LOG_TAG, "Found history: " + result.toString());
				if (clear) { // clear after request returns
					historyAdaptor.clear();
				}

				for (int i = 0; i < result.size(); i++) {
					JsonObject o = result.get(i).getAsJsonObject();
					o.addProperty("ssid", Util.cleanSSID(o.get("ssid")));
					historyAdaptor.add(new HistoryItem(o));
				}
			}
		});

		// TODO uncomment mock data
		JsonObject rating = new JsonObject();
		rating.addProperty("spam_count", 1);
		rating.addProperty("spam_freq", 2);
		rating.addProperty("bot_count", 3);
		rating.addProperty("bot_freq", 4);
		rating.addProperty("unexp_count", 5);
		rating.addProperty("unexp_freq", 6);
		rating.addProperty("raw_score", 7);
		rating.addProperty("is_infected", false);
		rating.addProperty("valid_rating", true);
		JsonObject jo = new JsonObject();
		jo.addProperty("count", 128);
		jo.addProperty("loc", "loc");
		jo.addProperty("security", "security");
		jo.addProperty("ssid", "ssid");
		jo.add("rating", rating);
		jo.addProperty("bssid", "bssid");
		jo.addProperty("remote_addr", "remote_addr");
		jo.addProperty("isEnterprise", true);
		jo.addProperty("created_at", "created_at");
		jo.addProperty("created_at_human", "create_at_human");
		jo.addProperty("datasrc", "datasrc");
		jo.addProperty("ip", "ip");
		jo.addProperty("lat", 1.0d);
		jo.addProperty("lng", 1.0d);
		jo.addProperty("uuid", "uuid");
		HistoryItem hi = new HistoryItem(jo);
		historyAdaptor.add(hi);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.history, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		switch (item.getItemId()) {
		case R.id.refreshHistory:
			loadHistory(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}