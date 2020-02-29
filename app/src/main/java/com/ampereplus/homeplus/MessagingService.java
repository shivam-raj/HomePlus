package com.ampereplus.homeplus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MessagingService extends Service {



    private static final String TAG = "MessagingService";
    public static final String  MQTT_MSG = "com.ampereplus.homeplus.MQTT_MSG";
    private MqttAndroidClient mqttClient;
    String deviceId;
    Intent bi = new Intent(MQTT_MSG);
    List<String> topics;
    LinkedHashMap<String,String> dev2room;
    String dev_id;
    List<String> registered;


    @Override
    public void onCreate() {

        topics=new ArrayList<>();
        dev2room=new LinkedHashMap<>();
        registered= new ArrayList<>();

        Intent resultIntent = new Intent(this, Splashscreen.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String NOTIFICATION_CHANNEL_ID = "com.ampereplus.homeplus";
            String channelName = "Home+ Client";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_power_on)
                    .setContentIntent(resultPendingIntent)
                    .setContentTitle("Client Running")
                    .setSubText("Tap to open app")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);

        } else {

            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Client Running")
                    .setContentIntent(resultPendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(2, notification);
        }
    }
    private void setClientID() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        deviceId = wInfo.getMacAddress();
        if (deviceId == null) {
            deviceId = MqttAsyncClient.generateClientId();
        }
    }


    public void doConnect(){


        for(final String dev_id: topics){
            final MqttAndroidClient mqttClient = new MqttAndroidClient(this, "tcp://broker.hivemq.com:1883",deviceId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            //Set call back class
            connOpts.setCleanSession(false);
            connOpts.setAutomaticReconnect(true);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {


                    bi.putExtra("topic",topic);
                    bi.putExtra("message",message.toString());
                    String[] deviceId = topic.split("/");
                    bi.putExtra("groupName",dev2room.get(deviceId[0]));
                    sendBroadcast(bi);

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

            Log.i("CHECK12","REACHED");

            IMqttToken token;
            try {
                token = mqttClient.connect(connOpts);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken arg0) {
                        try {

                            String topic=dev_id+"/offline";
                            String topic2=dev_id+"/status";
                            Log.i("CHECK13","Topics : "+topic+", "+topic2);
                            Log.i("mqtt TOPIC : ",topic+""+topic2);
                            registered.add(dev_id);
                            mqttClient.subscribe( topic, 0, null, new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.i("mqtt ss", "Successfully subscribed to topic.");
                                }
                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    Log.i("mqtt ss", "Failed to subscribed to topic.");
                                }
                            });
                            mqttClient.subscribe( topic2, 0, null, new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {

                                    Log.i("mqtt ss", "Successfully subscribed to topic.");
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    Log.i("mqtt ss", "Failed to subscribed to topic.");
                                }
                            });


                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken arg0, Throwable arg1) {
                        Log.d("mqtt", arg1.toString());
                        Log.i("CHECK12","REACHED failure");
                    }
                });

            } catch (MqttException e) {
                Log.e("CHECK12",""+e.getMessage());
            }
            topics.remove(dev_id);
        }



    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Log.i("CHECK11","Reached");
        dev_id=intent.getExtras().getString("deviceID");
        String groupName=intent.getExtras().getString("groupName");
        Log.i("CHECK13",""+dev_id);

        if (!registered.contains(dev_id)){
                topics.add(dev_id);
                dev2room.put(dev_id,groupName);
                setClientID();
                doConnect();
            }

        return START_STICKY;
    }
}
