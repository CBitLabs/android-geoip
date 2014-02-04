package com.cbitlabs.geoip;

/**
 * Created by stuart on 11/25/13.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;

import org.xbill.DNS.Address;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

import java.net.UnknownHostException;

public class DNSTask extends AsyncTask {

    private Context context;
    private JsonObject report;

    public DNSTask(Context context, JsonObject report) {
        Log.d(Util.LOG_TAG, "DNSTask Created");

        this.context = context;
        this.report = report;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d(Util.LOG_TAG, "DNSTask.doInBackground()");

        this.sendDataViaDNSLookup();
        return null;
    }

    protected void executeLookupUsingResolver(String h, Resolver r) {

        Lookup.setDefaultResolver(r);
        executeLookup(h);
    }

    protected void executeLookup(String h) {
        Log.i(Util.LOG_TAG, "Looking up:" + h);

        try {
            Address.getByName(h);
        } catch (UnknownHostException e) {
            Log.e(Util.LOG_TAG, "DNS Lookup failed for host:" + h, e);
        }
    }

    public void sendDataViaDNSLookup() {

        String info = Util.getReportAsString(report);
        String host = info.concat(".").concat(Util.DNS_SERVER);
        Log.i(Util.LOG_TAG, "Created Hostname for Lookup:" + host);

        Resolver defaultResolver = Lookup.getDefaultResolver();
        String simpleResolverUrl = Util.DNS_RESOLVER;
        executeLookupUsingResolver("d." + host, defaultResolver);

        try {
            Log.i(Util.LOG_TAG, "Setting DNS Resolver to use:" + simpleResolverUrl);
            Resolver simpleResolver = new SimpleResolver(simpleResolverUrl);
            executeLookupUsingResolver("s." + host, simpleResolver);
            Lookup.setDefaultResolver(defaultResolver);
        } catch (UnknownHostException e) {
            Log.e(Util.LOG_TAG, "A problem occurred while setting the custom resolver. UnknownHostException:" + simpleResolverUrl, e);
        }
    }
}
