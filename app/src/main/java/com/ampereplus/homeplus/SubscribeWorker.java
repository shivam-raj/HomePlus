package com.ampereplus.homeplus;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SubscribeWorker extends Worker {
    public SubscribeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }



    @NonNull
    @Override
    public Result doWork() {





        return null;
    }
}
