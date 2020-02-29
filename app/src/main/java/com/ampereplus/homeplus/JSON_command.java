package com.ampereplus.homeplus;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

public class JSON_command {

    JSONObject obj;
    JSONObject[] objAll;

    public void Publish(Context context, String uid, JSONObject object, final String dev_id) {
        final String LOGTAG = "mqtt : ";
        Log.i(LOGTAG, "MQTT Start");

        MemoryPersistence memPer = new MemoryPersistence();

        MqttAsyncClient client=null;
        try {
            client = new MqttAsyncClient("tcp://broker.hivemq.com:1883",uid, memPer);
            Log.i("CHECK15",""+client.toString());
        } catch (MqttException e) {
            Log.i("CHECK15",""+e.getMessage());
        }

        try {
            final MqttAsyncClient finalClient = client;
            finalClient.connect(null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken mqttToken) {
                    Log.i(LOGTAG, "Client connected");
                    Log.i(LOGTAG, "Topics="+mqttToken.getTopics());

                    MqttMessage message = new MqttMessage(obj.toString().getBytes());
                    message.setQos(0);
                    message.setRetained(false);

                    try {
                        finalClient.publish("ampere/"+dev_id, message);
                        Log.i(LOGTAG, "Message published");
                        Log.i(LOGTAG+" Topic", "ampere/"+dev_id);
                        finalClient.disconnect();
                        Log.i(LOGTAG, "client disconnected");

                    } catch (MqttPersistenceException e) {
                        e.printStackTrace();

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken arg0, Throwable arg1) {

                    Log.i(LOGTAG, "Client connection failed: "+arg1.getMessage());

                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public JSON_command(Context context,String uid,int value,String cmd,String dev_id)
    {
        String device_name=new String();
        if(value==1)device_name="device1";
        else if(value==2)device_name="device2";
        else if(value==3)device_name="device3";
        else if(value==4)device_name="device4";
        else if(value==69)device_name="device5";
        obj = new JSONObject();
        try {
            obj.put("device", device_name);
            obj.put("cmd", cmd);

        } catch (JSONException e) {
           e.printStackTrace();
        }

        Log.i("mqtt JSON",obj.toString());
        Publish(context,uid,obj,dev_id);
    }

}
