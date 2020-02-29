package com.ampereplus.homeplus;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Home extends AppCompatActivity implements ExpandableListAdapter.customGroupButtonListener,ApplianceAdapter.customGroupButtonListener {
    FirebaseUser user;
    WifiManager wifiManager;
    private ApplianceAdapter adapter;
    private ArrayList<String> arrayList;
    DatabaseReference db_root, db_uid;
    ExpandableListAdapter explistAdapter;
    ExpandableListView expListView;
    List<String> listGroup;
    ArrayList<String> wifiList = new ArrayList<>();
    LinkedHashMap<String, List<String>> listData;
    private List<ScanResult> results;
    List<String> listDevID;
    List<String> listTaskID;
    static String UID_OF_USER;
    List<RoomData> roomDataCollection;
    LinkedHashMap<RoomData,List<AppClass>> listNewData;


    ProgressBar spinner;

    static int FIREBASE_PERSISTENCE=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        String[] PERMS_INITIAL = {
                Manifest.permission.ACCESS_FINE_LOCATION,
        };
        ActivityCompat.requestPermissions(this, PERMS_INITIAL, 127);
        if(FIREBASE_PERSISTENCE==0){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            FIREBASE_PERSISTENCE++;
        }

        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.GONE);
            }
        }, 3000);

        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);


        user = FirebaseAuth.getInstance().getCurrentUser();

        UID_OF_USER=user.getUid();



        roomDataCollection=new ArrayList<>();
        listData = new LinkedHashMap<>();
        listNewData=new LinkedHashMap<>();
        listGroup = new ArrayList<String>();
        listDevID=new ArrayList<>();
        listTaskID=new ArrayList<>();
        db_root = FirebaseDatabase.getInstance().getReference();
        db_uid = db_root.child(user.getUid());
        db_uid.keepSynced(true);
        expListView = (ExpandableListView) findViewById(R.id.listViewRoom1);
        explistAdapter = new ExpandableListAdapter(this, roomDataCollection, listNewData);
        expListView.setGroupIndicator(null);
        explistAdapter.setCustomGroupButtonListener(Home.this);
        expListView.setAdapter(explistAdapter);



        expListView.setAnimation(AnimationUtils.loadAnimation(this,R.anim.item_zoom));

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousGroup)
                    expListView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddRoom();
            }
        });
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return false;
            }
        });
        
        

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        ImageView imgprofilepic = (ImageView) findViewById(R.id.user_profile1);

        String username = user.getDisplayName();
        String personPhotoUrl = user.getPhotoUrl().toString();
        imgprofilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut();
            }
        });

        Glide.with(getApplicationContext()).load(personPhotoUrl)
                .thumbnail(0.9f)
                .crossFade()
                .transform(new CircleTransform(Home.this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgprofilepic);

        prepareList();

    }

    public class DeviceClass{
        String groupName;
        String deviceKey;

        public DeviceClass(String groupName, String deviceKey) {
            this.groupName = groupName;
            this.deviceKey = deviceKey;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getDeviceKey() {
            return deviceKey;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }
    }

    public class AppClass{
        String app_name;
        String device_Key;
        int appController;

        public AppClass(String app_name, String device_Key,int appController) {
            this.app_name = app_name;
            this.device_Key = device_Key;
            this.appController=appController;
        }

        public String getAppName() {
            return app_name;
        }

        public String getDeviceKey() {
            return device_Key;
        }

        public int getAppController() {
            return appController;
        }
    }




    public void setData(RoomData newData){
        db_uid.child(newData.getDeviceID()).setValue(newData.toMap());
    }




    public void prepareList(){


        db_uid.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String deviceIDtemp =dataSnapshot.getKey();

                db_uid.child(deviceIDtemp).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        spinner.setVisibility(View.GONE);
                        RoomData loadedRoom=dataSnapshot.getValue(RoomData.class);
                        if(loadedRoom!=null){
                            Boolean present=false;
                            RoomData localRoom=null;
                            for(RoomData check:roomDataCollection){
                                if(check.getDeviceID().equals(loadedRoom.getDeviceID())){
                                    localRoom=loadedRoom;
                                    listGroup.remove(check.getRoomName());
                                    listNewData.remove(check);
                                    roomDataCollection.remove(check);
                                    Log.i("DBCHECK"," DB REMOVED");
                                    present=true;
                                    break;
                                }
                            }

                            if(!present){
                                localRoom=loadedRoom;
                            }
                            listGroup.add(loadedRoom.getRoomName());
                            List<String> loadedApp=new ArrayList<>();
                            List<AppClass> loadedAppClass=new ArrayList<>();
                            if(!(loadedRoom.getApp1Name().equals(""))) loadedAppClass.add(new AppClass(loadedRoom.getApp1Name(),loadedRoom.getDeviceID(),loadedRoom.getApp1Controller()));
                            if(!(loadedRoom.getApp2Name().equals("")))loadedAppClass.add(new AppClass(loadedRoom.getApp2Name(),loadedRoom.getDeviceID(),loadedRoom.getApp2Controller()));
                            if(!(loadedRoom.getApp3Name().equals("")))loadedAppClass.add(new AppClass(loadedRoom.getApp3Name(),loadedRoom.getDeviceID(),loadedRoom.getApp3Controller()));
                            if(!(loadedRoom.getApp4Name().equals("")))loadedAppClass.add(new AppClass(loadedRoom.getApp4Name(),loadedRoom.getDeviceID(),loadedRoom.getApp4Controller()));
                            listNewData.put(loadedRoom,loadedAppClass);
                            explistAdapter.notifyDataSetChanged();
                            Log.i("LOADROOM",""+loadedRoom.toMap().toString());
                            roomDataCollection.add(localRoom);
                            Log.i("DBCHECK"," DB ADDED");
                            listDevID.add(localRoom.getDeviceID());
                            listTaskID.add(localRoom.getDeviceID());
                            if(isOnline()) {
                                Log.i("ONLINECHECK","VALUE : "+isOnline());
                                //mqttSubscribe(Home.this,localRoom.getDeviceID(),explistAdapter);
                                Intent i= new Intent(Home.this,MessagingService.class);
                                i.putExtra("deviceID",localRoom.getDeviceID());
                                i.putExtra("groupName",localRoom.getRoomName());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(i);
                                    Log.i("CHECK13",""+localRoom.getDeviceID());
                                    Log.i("CHECK11", "Started service");
                                } else {
                                    startService(i);
                                    Log.i("CHECK11", "Started service");
                                }
                            }
                            Log.i("DBCHECK"," DBS DATABASE: "+roomDataCollection.toString());
                            for(RoomData check:roomDataCollection){

                                Log.i("DBCHECK"," DBS : "+check.toMap().toString());
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    public void onGroupDeleteListener(String value) {

        for(RoomData abc:roomDataCollection){
            if(abc.getRoomName().equals(value)){
                db_uid.child(abc.getDeviceID()).setValue(null);
                listNewData.remove(abc);
                listGroup.remove(abc.getRoomName());
                explistAdapter.notifyDataSetChanged();
                roomDataCollection.remove(abc);
                listDevID.remove(abc.getDeviceID());
                listTaskID.remove(abc.getDeviceID());
            }
        }
    }


    @Override
    public void onChildRenameListener(String newName,String deviceID,int controller) {

        Log.i("CHECK7","Working for : "+controller);
        Log.i("CHECK8","Working for : "+listNewData.toString());
        RoomData newData=null;
        for(RoomData abc:roomDataCollection){
            if(abc.getDeviceID().equals(deviceID)){
                newData=abc;
                break;
            }
        }
        switch(controller){
            case 1:
                newData.setApp1Name(newName);
                break;
            case 2:
                newData.setApp2Name(newName);
                break;
            case 3:
                newData.setApp3Name(newName);
                break;
            case 4:
                newData.setApp4Name(newName);
                break;
            default:break;
        }
        setData(newData);
    }

    @Override
    public void onGroupRenameListener(String group_name_old, String group_name_new) {

        RoomData newData=null;

        listGroup.remove(group_name_old);


        for(RoomData abc:roomDataCollection){
            if(abc.getRoomName().equals(group_name_old)){
                newData=abc;
                roomDataCollection.remove(abc);
                listNewData.remove(abc);
                break;
            }
        }

        newData.setRoomName(group_name_new);
        setData(newData);
    }


    @Override
    public void onChildDeleteListener(String device_id,int index) {

        RoomData newData=null;
        for(RoomData abc:roomDataCollection){
            if(abc.getDeviceID().equals(device_id)){
                newData=abc;
                break;
            }
        }
        switch(index){
            case 1:
                newData.setApp1Name("");
                newData.setApp1Controller(0);
                break;
            case 2:
                newData.setApp2Name("");
                newData.setApp2Controller(0);
                break;
            case 3:
                newData.setApp3Name("");
                newData.setApp3Controller(0);
                break;
            case 4:
                newData.setApp4Name("");
                newData.setApp4Controller(0);
                break;
                default:break;
        }
        setData(newData);
    }

    @Override
    public void onButtonLongClickListener(int c_position, int g_position, String value) {

    }
    ConnectivityManager cm;
    public boolean isOnline() {


        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        Log.i("NETC",""+isConnected);

        return isConnected;
    }

    long millis_last=0,sleep=0;
    @Override
    public void onPublish(final int position, final String command, final String deviceID) {

        long millis_current = SystemClock.elapsedRealtime();
        long diff = millis_current - millis_last;
        if (diff >= 1000) {


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    JSON_command cmd = new JSON_command(getApplicationContext(), user.getUid(), position, command, deviceID);
                    Log.i("TIMEDEL", SystemClock.elapsedRealtime() + "");
                }
            }, 0);
            millis_last = millis_current;

        } else {
            sleep += 1000;
            Log.i("TIMEDEL", "Too many clicks,Sleep for : " + sleep);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    JSON_command cmd = new JSON_command(getApplicationContext(), user.getUid(), position, command, deviceID);
                    Log.i("TIMEDEL", SystemClock.elapsedRealtime() + "");
                    sleep = 0;
                }
            }, sleep);
            millis_last = millis_current;
        }
    }

    private void AddApp( final int index, final RoomData addOn, final int controller) {

        final RoomData finalCurrent = addOn;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Home.this);
        LayoutInflater inflater = Home.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.app_label_editor, null);
        final EditText editText = (EditText) dialogView.findViewById(R.id.label_field);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setMessage("Enter the name of the Appliance : ")
                .setTitle("Add Appliance")
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final String app_name = editText.getText().toString();
                                Log.i("DBCHECK","VALUE:"+app_name);
                                if (app_name.length()==0) {
                                    Toast.makeText(Home.this, "You did not enter an appliance name", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Boolean allowed=true;
                                    List<AppClass> temphold=new ArrayList<>();
                                    if (!listNewData.containsKey(addOn))allowed=true;
                                    else if (listNewData.containsKey(addOn)){
                                        temphold=listNewData.get(addOn);
                                        for (AppClass abc:temphold)
                                            if(abc.getAppName().equals(app_name))allowed=false;
                                            else allowed = true;
                                    }

                                    if (app_name.equals("")) {
                                        Toast.makeText(Home.this, "You did not enter an appliance name", Toast.LENGTH_SHORT).show();
                                    } else {

                                        List<Integer> aval=addOn.availableIndex();
                                        int finalindex=0;
                                        if(aval.contains(index))finalindex=index;
                                        else finalindex=aval.get(0);

                                        int con;
                                        if(controller!=0)con=controller;
                                        else con=finalindex;
                                        AppClass newApp=new AppClass(app_name,addOn.getDeviceID(),con);
                                        if(allowed){


                                            RoomData updatedData=addOn;
                                            switch (finalindex){
                                                case 1:updatedData.setApp1Name(app_name);
                                                    updatedData.setApp1Controller(con);
                                                    break;
                                                case 2:updatedData.setApp2Name(app_name);
                                                    updatedData.setApp2Controller(con);
                                                    break;
                                                case 3:updatedData.setApp3Name(app_name);
                                                    updatedData.setApp3Controller(con);
                                                    break;
                                                case 4:updatedData.setApp4Name(app_name);
                                                    updatedData.setApp4Controller(con);
                                                    break;
                                                default:break;
                                            }
                                            setData(updatedData);
                                            listGroup.remove(addOn.getRoomName());
                                            listNewData.remove(addOn);
                                            Log.i("CHECK24",listNewData.toString());
                                            Log.i("CHECK24",roomDataCollection.toString());
                                            explistAdapter.notifyDataSetChanged();
//                                            Log.i("CHECK9",temphold.get(0).getAppName()+":"+temphold.get(1).getAppName()+":"+temphold.get(2).getAppName());
                                        }
                                        else{
                                            Toast.makeText(Home.this, "Kindly enter a unique appliance name for ease of access", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }
                                }


                            })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }
                );
        AlertDialog alertDialog = dialogBuilder.create();
        if (alertDialog.isShowing()) alertDialog.dismiss();
        alertDialog.show();
    }

    @Override
    public void onAppAdd(RoomData value) {

        List<AppClass> apps=listNewData.get(value);
        if(apps.size()<=3) AddApp(apps.size()+1,value,0);
        else{
            Toast.makeText(getApplicationContext(),
                    "Limit Reached",
                    Toast.LENGTH_LONG).show();

        }

    }


    public void AddRoom() {
        arrayList = new ArrayList<>();
        adapter = new ApplianceAdapter(getApplicationContext(), R.layout.room_row, arrayList);
        adapter.setCustomGroupButtonListener(this);
        setupWifi(wifiList);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Home.this);
        LayoutInflater inflater = Home.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_label_editor, null);
        ListView wifiList = (ListView) dialogView.findViewById(R.id.wifi_result);
        wifiList.setAdapter(adapter);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setMessage("Connect to the WiFi of the device: ")
                .setTitle("Add Device")
                .setCancelable(false)
                .setPositiveButton("Configure",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {


                            }
                        }
                )
                .setNeutralButton("Add", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }
                );

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                wantToCloseDialog=wifiManager.getConnectionInfo().getSSID().contains("AmperePlus_");

                if(wantToCloseDialog){
                    Boolean[] myTaskParams = {true};
                    NetTask nw = new NetTask();
                    nw.execute(myTaskParams);
                    alertDialog.dismiss();
                }
                else{
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }


            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                wantToCloseDialog=wifiManager.getConnectionInfo().getSSID().contains("AmperePlus_");

                if(wantToCloseDialog){
                    Boolean[] myTaskParams = {false};
                    NetTask nw = new NetTask();
                    nw.execute(myTaskParams);
                    alertDialog.dismiss();
                }
                else{
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }
        });


    }


    private void setupWifi(ArrayList<String> wifi_list) {

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }


    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            for (ScanResult scanResult : results) {
                String result_wifi = scanResult.SSID;
                if (result_wifi.toLowerCase().contains("ampereplus")) {
                    if (!arrayList.contains(result_wifi)) {
                        arrayList.add(scanResult.SSID);
                        Log.i("WIFI RECEIVER", scanResult.SSID);
                    }
                }

                adapter.notifyDataSetChanged();
            }
        }

        ;
    };



    public String JSON_search(String config, Boolean add,String currentSSID) throws JSONException {

        Boolean skip=add;
        String devTemp="";
        Log.i("APPI",listDevID.toString());
        JSONObject config_json = new JSONObject(config);
        Log.i("CONFIG OBJECT", config_json.toString());
        if (config_json.has("device")) {
            String s = config_json.optString("device");
            JSONObject config_json_device = new JSONObject(s);
            if (config_json_device.has("id")) {
                String device_id = config_json_device.optString("id");
                Log.i("CONFIG VALUE", device_id);
                devTemp=device_id;
                if(listDevID.contains(device_id)){
                    Handler handler =  new Handler(getApplicationContext().getMainLooper());
                    handler.post( new Runnable(){
                        public void run(){
                            Toast.makeText(getApplicationContext(), "Device Already Added",Toast.LENGTH_LONG).show();
                        }
                    });
                    add=false;
                    skip=false;
                }
                if (skip) {

                    Intent i = new Intent(Home.this, NewDevice.class);
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST",(Serializable)listGroup);
                    i.putExtra("device_id", device_id);
                    i.putExtra("device_wifi",currentSSID);
                    i.putExtra("BUNDLE",args);
                    startActivity(i);

                }

            }
        }
        return devTemp;
    }
    
    private class NetTask extends AsyncTask<Boolean, Void, Void> {

        boolean getonly=true;
        boolean got;
        String configMain="";
        String currentSSID="";
        @Override
        protected Void doInBackground(Boolean... add) {
            Boolean skip = add[0];
            getonly =skip;
            String config="";
            try {
                config=perform_get();
                if(config!=null)got=true;
                currentSSID=wifiManager.getConnectionInfo().getSSID();
                configMain=config;
                Log.i("CONFIG", config);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                String nouse=JSON_search(config, skip,currentSSID);
                Log.i("CONFIG JSON", "Reached");
            } catch (Exception e) {
                Log.i("CONFIG", "JSON EXCEPTION");
                got=false;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            String devTemp="";


                try {
                    devTemp=JSON_search(configMain, false,currentSSID);
                    Log.i("CONFIG JSON", "Reached");
                } catch (Exception e) {
                    Log.i("CONFIG", "JSON EXCEPTION");
                    got=false;
                    e.printStackTrace();
                }

            if(!got) Toast.makeText(getApplicationContext(), "Kindly Disable Mobile Data", Toast.LENGTH_SHORT).show();
            if(got && !getonly){

                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Home.this);
                LayoutInflater inflater = Home.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.app_label_editor, null);
                final EditText editText = (EditText) dialogView.findViewById(R.id.label_field);
                dialogBuilder.setView(dialogView);
                final String finalDevTemp = devTemp;

                dialogBuilder.setMessage("Name the room : ")
                        .setTitle("Add Device")
                        .setCancelable(false)
                        .setPositiveButton("Add",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        RoomData newRoom=new RoomData(editText.getText().toString(),finalDevTemp);
                                        if(!newRoom.getDeviceID().equals(""))setData(newRoom);
                                        Log.i("CHECK5",newRoom.toMap().toString());



                                    }
                                }
                        )
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                }
                        );
                final AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();

            }
        }
    }




    public String perform_get() throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://192.168.4.1/rpc/Config.Get")
                .build();

        Log.i("GET56", "Request Built");
        com.squareup.okhttp.Response response = client.newCall(request).execute();
        return response.body().string();
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

    public void SignOut() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Home.this);
        dialogBuilder.setMessage("Are you sure you want to logout ?")
                .setTitle("Sign Out")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                AuthUI.getInstance()
                                        .signOut(Home.this)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // user is now signed out
                                                startActivity(new Intent(Home.this, Splashscreen.class));
                                                finish();
                                            }
                                        });
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }
                );
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        if (alertDialog.isShowing()) alertDialog.dismiss();
        alertDialog.show();
    }


    @Override
    public void onBackPressed() {
        if (user!=null) {

        } else {
            super.onBackPressed();
        }
    }


    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;


            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override
        public String getId() {
            return getClass().getName();
        }
    }
}
