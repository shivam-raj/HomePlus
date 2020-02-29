package com.ampereplus.homeplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ApplianceAdapter extends ArrayAdapter<String> {

    int resourceLayout;
    Context mContext;
    WifiManager wifiManager;

    customGroupButtonListener customListener;

    public interface customGroupButtonListener {
        public void connect_device(String SSID);
    }

    public void setCustomGroupButtonListener(customGroupButtonListener listener) {
        this.customListener = listener;
    }
    public ApplianceAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        this.resourceLayout = resource;
        this.mContext = context;
        wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public void connect_device(String SSID) {
        String networkPass = "password";
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + SSID + "\"";
        conf.preSharedKey = "\"" + networkPass + "\"";

        wifiManager.addNetwork(conf);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }
        final String p = getItem(position);
        if (p != null) {
            TextView app_label = (TextView) v.findViewById(R.id.firstTextView);
            if(app_label!=null)
                app_label.setText(p);
        }

        final TextView appli_label=(TextView)v.findViewById(R.id.firstTextView);

        final Button settings=(Button)v.findViewById(R.id.settings);
        final ImageView check=(ImageView)v.findViewById(R.id.connect_status);
        check.setVisibility(View.INVISIBLE);

            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    connect_device(appli_label.getText().toString());


                }
            });



        return v;


    }

}
