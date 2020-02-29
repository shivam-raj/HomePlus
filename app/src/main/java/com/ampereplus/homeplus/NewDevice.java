package com.ampereplus.homeplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class NewDevice extends AppCompatActivity {
    DatabaseReference db_root,db_uid;
    FirebaseUser user;
    String post_json;
    String new_room_name;
    WifiManager wifiManager;
    SharedPreferences pref;
    LinkedHashMap<Integer,String> listDevice;

    private class PostTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String s=perform_post(".Set",post_json);
                Log.i("CONFIG SET",s);
                JSONObject obj = new JSONObject();
                try {
                    obj.put("reboot", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("CONFIG SAVE",obj.toString());
                String s2=perform_post(".Save",obj.toString());
                Log.i("CONFIG SAVE",s2);

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            Intent addroom=new Intent(NewDevice.this,Splashscreen.class);
            addroom.putExtra("new_room",new_room_name);
            startActivity(addroom);
        }
    }

    public String perform_post(String url,String post) throws IOException {

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, post);
        Request request = new Request.Builder()
                .url("http://192.168.4.1/rpc/Config"+url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_device);
        listDevice= new LinkedHashMap<>();
        user= FirebaseAuth.getInstance().getCurrentUser();
        db_root=FirebaseDatabase.getInstance().getReference();
        db_uid=db_root.child(user.getUid());
        pref = getApplicationContext().getSharedPreferences("dev_backup-1-"+Home.UID_OF_USER, MODE_PRIVATE);
        Button save=(Button)findViewById(R.id.save_room);
        TextView txt=(TextView)findViewById(R.id.dev_id);
        final String dev_id_temp=getIntent().getStringExtra("device_id");
        txt.setText(dev_id_temp);
        final EditText wifi_pass= (EditText)findViewById(R.id.wifi_pass);
        final EditText wifi_ssid= (EditText)findViewById(R.id.wifi_ssid);
        final EditText r_name= (EditText)findViewById(R.id.room_name);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Intent intent = getIntent();
        final String device_wifi=intent.getStringExtra("device_wifi");
        Bundle args = intent.getBundleExtra("BUNDLE");
        final ArrayList<String> listGroup = (ArrayList<String>) args.getSerializable("ARRAYLIST");


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String room_name= r_name.getText().toString();
                new_room_name=room_name;
                if (room_name.matches(""))
                {
                    Toast.makeText(NewDevice.this, "You did not enter a room name", Toast.LENGTH_SHORT).show();
                }
                else if(listGroup.contains(room_name)){
                    Toast.makeText(NewDevice.this, "Please enter a unique room name", Toast.LENGTH_SHORT).show();
                }
                else {

                    RoomData addnewroom=new RoomData(dev_id_temp);
                    addnewroom.setRoomName(room_name);
                    db_uid.child(dev_id_temp).setValue(addnewroom.toMap());

                    final String password= wifi_pass.getText().toString();
                    final String ssid= wifi_ssid.getText().toString();

                    String json_post="{\"config\":{\"wifi\":{\"sta1\":{\"enable\":true,\"ssid\":\""+ssid+"\",\"pass\":\""+password+"\"}}}}";
                    Log.i("JSON",json_post);

                    post_json=json_post;

                    if(!ssid.equals("")){
                        if(device_wifi.equals(wifiManager.getConnectionInfo().getSSID()))
                            new PostTask().execute();
                        else{
                            Toast.makeText(NewDevice.this, "Please connect to the device WiFi", Toast.LENGTH_SHORT).show();
                        }
                    }

                    else{
                        startActivity(new Intent(NewDevice.this,Splashscreen.class));
                    }
                }
            }
        });
    }
}
