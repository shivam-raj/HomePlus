package com.ampereplus.homeplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.SettingViewHolder> {


    customSettingListener customListener;
    public interface customSettingListener {
        public void onGroupDeleteListener(String value);
        public void onMasterControl(String deviceID,char set);
        public void onAppAdd(RoomData value);
        public void onGroupRenameListener(RoomData roomData);
    }

    public void setCustomSettingListener(customSettingListener listener) {
        this.customListener = listener;
    }


    ConnectivityManager cm;
    public boolean isOnline() {


        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        Log.i("NETC",""+isConnected);

        return isConnected;
    }

    List<SettingsClass> listSettings;
    Context context;


    public static class SettingViewHolder extends RecyclerView.ViewHolder{

        public TextView mTextView;
        public ImageView img;
        public SettingViewHolder(View v) {
            super(v);
            mTextView =(TextView)v.findViewById(R.id.listSettings);
            img=(ImageView)v.findViewById(R.id.ic_settings);
        }
    }


    public SettingAdapter(List<SettingsClass> myDataset, Context contxt) {

        listSettings=myDataset;
        context=contxt;
        cm = (ConnectivityManager)contxt.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public SettingAdapter.SettingViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_settings, parent, false);

        SettingViewHolder vh = new SettingViewHolder(v);
        return vh;
    }

    public void onBindViewHolder(final SettingViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(listSettings.get(position).getSettingText());

        Log.i("CHECK14",listSettings.get(position).getSettingText());

        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(listSettings.get(position).getRef()){
                    case "add" :  listSettings.get(position).cancel();
                        if(customListener!=null)
                        customListener.onAppAdd(listSettings.get(position).getGroup());break;
                    case "delete": if(customListener!=null)
                        customListener.onGroupDeleteListener(listSettings.get(position).getGroup().getRoomName());break;
                    case "rename":  listSettings.get(position).cancel();
                        if(customListener!=null)customListener.onGroupRenameListener(listSettings.get(position).getGroup());break;
                    case "room_on":listSettings.get(position).cancel();
                        if(isOnline()) {
                        if(customListener!=null)customListener.onMasterControl(listSettings.get(position).getGroup().getDeviceID(),'1');
                        JSON_command json=new JSON_command(context,"123",69,"off",listSettings.get(position).getGroup().getDeviceID());
                    }else
                        Toast.makeText(context,
                                "No Internet Connection",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case "room_off": listSettings.get(position).cancel();
                        if(isOnline()) {
                        if(customListener!=null)customListener.onMasterControl(listSettings.get(position).getGroup().getDeviceID(),'0');
                        JSON_command json2=new JSON_command(context,"123",69,"on",listSettings.get(position).getGroup().getDeviceID());
                    }else
                        Toast.makeText(context,
                                "No Internet Connection",
                                Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });
        holder.img.setImageResource(listSettings.get(position).getIcon());

    }

    @Override
    public int getItemCount() {
        return listSettings.size();
    }


}
