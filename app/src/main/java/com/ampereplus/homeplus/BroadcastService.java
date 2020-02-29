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
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedHashMap;

import static android.os.Build.VERSION.SDK_INT;

public class BroadcastService extends Service {

    private final static String TAG = "BroadcastService";

    public static final String COUNTDOWN_BR = "com.lemonnana.blank.countdown_br";
    int ServiceId = 1;
    Intent bi = new Intent(COUNTDOWN_BR);
    LinkedHashMap<Integer,Boolean> pidActive;
    LinkedHashMap<Integer,Integer> pidServiceId;
    LinkedHashMap<Integer,String> pidChannel;
    LinkedHashMap<Integer,CountDownTimer> pidTimer;

    int time_minute=0;

    int i=0;

    @Override
    public void onCreate() {
        super.onCreate();

        pidActive=new LinkedHashMap<>();
        pidServiceId=new LinkedHashMap<>();
        pidChannel=new LinkedHashMap<>();
        pidTimer= new LinkedHashMap<>();

    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "Activity Destroyed");
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
            stopSelf();
        }
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        final int time=intent.getExtras().getInt("time");
        final int pid = intent.getExtras().getInt("pid");
        final Boolean runnning=intent.getBooleanExtra("running",false);
        pidActive.put(pid,runnning);

        ServiceId=startId;


        Log.i("COUNTDOWNTMR", "Starting timer..."+pid+" STATUS: "+runnning);


        final int child_position=intent.getExtras().getInt("child_position");
        final int group_position=intent.getExtras().getInt("group_position");
        final String group_title=intent.getExtras().getString("group_title");
        final String deviceID=intent.getExtras().getString("deviceID");
        final String childNm=intent.getExtras().getString("child_title");


        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        long timer=time*60000;

        if(runnning){

            String NOTIFICATION_CHANNEL_ID = "com.ampereplus.homeplus"+pid;
            pidChannel.put(pid,NOTIFICATION_CHANNEL_ID);
            pidServiceId.put(pid,startId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                String channelName = "Home+ Alarm Clock Service";
                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
                chan.setLightColor(Color.BLUE);
                chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);


                manager.createNotificationChannel(chan);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                Notification notification = notificationBuilder.setOngoing(true)
                        .setSmallIcon(R.drawable.ic_alarm_on)
                        .setContentTitle("Home+ clock is running in background")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Alarm is running"))
                        .setPriority(NotificationManager.IMPORTANCE_MIN)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build();
                startForeground(pid, notification);

            } else {

                Notification.Builder builder = new Notification.Builder(this)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("Alarm is running")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

                Notification notification = builder.build();

                startForeground(pid, notification);
            }

            CountDownTimer cdt= new CountDownTimer(timer, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                    Log.i(TAG,pid+ "- Countdown seconds remaining: " + millisUntilFinished / 1000);
                    if(millisUntilFinished/1000==30) Toast.makeText(BroadcastService.this,
                            childNm+" in "+group_title+" will be turned off in 30 seconds ",
                            Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "Timer finish");
                    bi.putExtra("countdown", 0);
                    bi.putExtra("child_position",child_position);
                    bi.putExtra("group_position",group_position);
                    bi.putExtra("group_title",group_title);
                    bi.putExtra("deviceID",deviceID);
                    bi.putExtra("pid",pid);
                    sendBroadcast(bi);

                    JSON_command command=new JSON_command(BroadcastService.this,"ABCD",child_position,"on",deviceID);
                    Log.i(TAG, "Timer finished and sent");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.deleteNotificationChannel(pidChannel.get(pid));
                    else manager.cancel(pid);
                    BroadcastService.this.stopSelf(pidServiceId.getOrDefault(pid,ServiceId));

                }
            };
            cdt.start();
            pidTimer.put(pid,cdt);
            Log.i(TAG, "Started timer...");
        }
        else
        {

            if(pidTimer.get(pid)!=null){
                pidTimer.get(pid).cancel();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.deleteNotificationChannel(pidChannel.get(pid));
                else manager.cancel(pid);
                BroadcastService.this.stopSelf(pidServiceId.getOrDefault(pid,ServiceId));
            }
        }

        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}

