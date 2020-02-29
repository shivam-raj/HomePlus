package com.ampereplus.homeplus;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.collection.LLRBNode;

import org.json.JSONException;
import org.json.JSONObject;


public class ExpandableListAdapter extends BaseExpandableListAdapter {

    List<Group_Unit> groupCollection;
    List<App_Unit> appCollection;
    LinkedHashMap<Integer,Boolean> active;
    private Context _context;
    private List<RoomData> _listDataHeader;
    private HashMap<RoomData, List<Home.AppClass>> _listDataChild;
    ConnectivityManager cm;
    customGroupButtonListener customListener;
    int time_min=0;


    public ExpandableListAdapter(Context context, List<RoomData> listDataHeader,
                                 HashMap<RoomData, List<Home.AppClass>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        cm = (ConnectivityManager)_context.getSystemService(Context.CONNECTIVITY_SERVICE);
        active=new LinkedHashMap<>();
        appCollection=new ArrayList<>();
        groupCollection=new ArrayList<>();
        _context.registerReceiver(mqtt, new IntentFilter(MessagingService.MQTT_MSG));

    }

    public class App_Unit implements CompoundButton.OnCheckedChangeListener,View.OnClickListener {

        String groupNm;
        int ChildPos;
        int GroupPos;
        ImageView alarm,delete;
        Switch toggle;
        Boolean toggleState=false;
        int PositionOfUnit;
        String DeviceID;
        int AlarmPID;
        Context ctx;
        String childNm;
        Boolean Running=false;
        int AppID;
        ImageView rename;
        TextView display;
        int appController;
        Boolean enabled=false;



        public App_Unit(Context context, String groupName, String Childtxt, int GroupPosition, int childPosition, ImageView alarm_ref, Switch toggle_ref, ImageView delete_app, ImageView rename,TextView text,Home.AppClass object) {
            groupNm=groupName;
            childNm=Childtxt;
            ChildPos=childPosition;
            AppID=childPosition;
            GroupPos=GroupPosition;
            alarm=alarm_ref;
            toggle=toggle_ref;
            PositionOfUnit=(GroupPosition*4)+childPosition;
            ctx=context;
            delete=delete_app;
            this.rename=rename;
            this.display=text;
            toggle.setOnCheckedChangeListener(this);
            delete.setOnClickListener(this);
            alarm.setOnClickListener(this);
            rename.setOnClickListener(this);
            Log.i("APPUNIT","NEW CREATED");
            DeviceID=object.getDeviceKey();
            appController=object.getAppController();
            Log.i("CHECK7",""+getAppController());
        }


        public App_Unit(Context context, String groupName, String Childtxt, int GroupPosition, int childPosition, ImageView alarm_ref, Switch toggle_ref,TextView text,Home.AppClass object) {
            groupNm=groupName;
            childNm=Childtxt;
            ChildPos=childPosition;
            AppID=childPosition;
            GroupPos=GroupPosition;
            alarm=alarm_ref;
            toggle=toggle_ref;
            PositionOfUnit=(GroupPosition*4)+childPosition;
            ctx=context;
            this.display=text;
            toggle.setOnCheckedChangeListener(this);
            alarm.setOnClickListener(this);
            Log.i("APPUNIT","NEW CREATED");
            DeviceID=object.getDeviceKey();
            appController=object.getAppController();
            Log.i("CHECK7",""+getAppController());
        }


        public void setEnabled(Boolean b){
            enabled=b;
        }

        public Boolean getEnabled(){
            return enabled;
        }

        public void updateTextview(String newName){
            this.display.setText(newName);
        }



        public int getAppID(){
            return AppID;
        }

        public void setRunning(Boolean b) {
            if(b)alarm.setImageResource(R.drawable.ic_alarm_on);
            else alarm.setImageResource(R.drawable.ic_alarm_off);
            Running=b;
        }
        public Boolean getRunning(){
            return Running;
        }
        public String getDeviceID(){
            return DeviceID;
        }
        public Boolean checkDeviceID(String test){
            if(test.equals(DeviceID))return true;
            else return false;
        }
        public Boolean checkGroupName(String test) {
            if(test.equals(groupNm))return true;
            else return false;
        }
        public ImageView getAlarm(){
            return alarm;
        }
        public void setAlarm(){
                    boolean b = checkImageResource(_context,alarm,R.drawable.ic_alarm_on);
                    final Boolean[] alarm_set = {b};
                    if(!alarm_set[0])
                    {
                        final Calendar mcurrentTime = Calendar.getInstance();
                        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                        final int minute = mcurrentTime.get(Calendar.MINUTE);
                        TimePickerDialog mTimePicker;
                        mTimePicker = new TimePickerDialog(ctx, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                                Calendar check= Calendar.getInstance();
                                check.set(Calendar.HOUR_OF_DAY, selectedHour);
                                check.set(Calendar.MINUTE,selectedMinute);
                                if(check.getTimeInMillis() > mcurrentTime.getTimeInMillis()) {
                                    time_min = ((selectedHour - hour) * 60) + (selectedMinute - minute);
                                    Log.i("CHECK20","TIME SET : "+time_min);

                                }
                                else if(check.getTimeInMillis() < mcurrentTime.getTimeInMillis()){
                                    time_min = (((selectedHour+24) - hour) * 60) + (selectedMinute - minute);
                                }
                                else {
                                    time_min=0;
                                }
                                if(time_min!=0) {
                                    Toast.makeText(ctx,
                                            childNm+" will be turned off in " + time_min + " minutes", Toast.LENGTH_SHORT).show();

                                    int pid = new Random().nextInt(10000) + 20;
                                    active.put(pid, true);
                                    AlarmPID=pid;
                                    setRunning(true);
                                    Log.i("COUNTDOWN", "SET for : " + time_min);
                                    Intent myService = new Intent(ctx, BroadcastService.class);
                                    myService.putExtra("child_position", getAppController());
                                    myService.putExtra("child_title", childNm);
                                    myService.putExtra("group_position",GroupPos);
                                    Log.i("CHECK10",getDeviceID());
                                    myService.putExtra("group_title",getGroupNm());
                                    myService.putExtra("deviceID", getDeviceID());
                                    myService.putExtra("time", time_min);
                                    myService.putExtra("pid", pid);
                                    myService.putExtra("running", true);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        ctx.startForegroundService(myService);
                                        Log.i("SERVICE START", "Started service");
                                    } else {
                                        ctx.startService(myService);
                                        Log.i("SERVICE START", "Started service");
                                    }
                                    ctx.registerReceiver(br, new IntentFilter(BroadcastService.COUNTDOWN_BR));
                                    alarm_set[0] = true;
                                }
                                else{
                                    Toast.makeText(ctx,
                                            "Invalid Time Picked",
                                            Toast.LENGTH_SHORT).show();

                                }

                            }
                        }, hour, minute, true);
                        mTimePicker.setTitle("Select Time");
                        mTimePicker.show();
                    }
                    else {
                        alarm_set[0]=false;
                        setRunning(false);
                        active.remove(AlarmPID);
                        active.put(AlarmPID,false);
                        Log.i("COUNTDOWN",AlarmPID+" DEACTIVATED");
                        Intent myService = new Intent(ctx, BroadcastService.class);
                        myService.putExtra("child_position", getAppController());
                        myService.putExtra("group_position",GroupPos);
                        Log.i("CHECK10",getDeviceID());
                        myService.putExtra("deviceID", getDeviceID());
                        myService.putExtra("time", time_min);
                        myService.putExtra("pid", AlarmPID);
                        myService.putExtra("running", false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ctx.startForegroundService(myService);
                            Log.i("SERVICE START", "Started service");
                        } else {
                            ctx.startService(myService);
                            Log.i("SERVICE START", "Started service");
                        }
                    }

        }
        public void cancelAlarm(){
            setRunning(false);
            active.remove(AlarmPID);
            active.put(AlarmPID,false);
            Log.i("COUNTDOWN",AlarmPID+" DEACTIVATED");
            Intent myService = new Intent(ctx, BroadcastService.class);
            myService.putExtra("child_position", getAppController());
            myService.putExtra("group_position",GroupPos);
            Log.i("CHECK10",getDeviceID());
            myService.putExtra("deviceID", getDeviceID());
            myService.putExtra("time", time_min);
            myService.putExtra("pid", AlarmPID);
            myService.putExtra("running", false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(myService);
                Log.i("SERVICE START", "Started service");
            } else {
                ctx.startService(myService);
                Log.i("SERVICE START", "Started service");
            }
        }
        public Switch getToggle(){
            return toggle;
        }
        public Boolean getToggleState() {
            return toggleState;
        }
        public void setToggle(char n){
            if(n=='0'){
                toggleState=false;
                toggle.setChecked(false);
            }
            if(n=='1') {
                toggleState=true;
                toggle.setChecked(true);
            }
        }

        public void setToggle(int n){
            if(n==0){

                toggle.setChecked(false);
            }
            if(n==1) {

                toggle.setChecked(true);
            }
        }

        public void setToggle(Boolean ischecked) {
            if(isOnline())
            {
                if(ischecked!=toggleState)
                if (customListener != null) {
                    String command=null;
                    if(toggle.isChecked())command="off";
                    else command="on";
                    customListener.onPublish(getAppController(),command,getDeviceID());
                    toggleState=toggle.isChecked();

                }
            }
            else
            {
                Toast.makeText(_context,
                        "No Internet Connection",
                        Toast.LENGTH_SHORT).show();
                Log.i("FLAG CHECK",!toggle.isChecked()+"");
                toggle.setChecked(!toggle.isChecked());
                toggleState=toggle.isChecked();
            }
        }
        public int getPosition(){
            return (GroupPos*4)+ChildPos;
        }
        public int getChildPos(){
            return ChildPos;
        }
        public int getGroupPos(){
            return GroupPos;
        }
        public void onDelete(){
            if (customListener != null) {
                customListener.onChildDeleteListener(DeviceID,getAppController());
                Toast.makeText(_context,
                        childNm+" Deleted ",
                        Toast.LENGTH_SHORT).show();
                appCollection.remove(this);
            }
        }
        public void setAppID(int i){
            AppID=i;
        }
        public void setChildNm(String name){
            childNm=name;
            this.updateTextview(name);


                if (customListener != null) {
                    customListener.onChildRenameListener(name,getDeviceID(),getAppController());
                    Toast.makeText(_context,
                            childNm+" Renamed ",
                            Toast.LENGTH_SHORT).show();
                }


        }
        public String getGroupNm()
        {
            return groupNm;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            this.setToggle(isChecked);
        }

        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.app_delete)
                this.onDelete();
            if(v.getId()==R.id.app_alarm)
                this.setAlarm();
            if(v.getId()==R.id.app_rename)
                this.rename();
        }

        private void rename() {
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(_context);
            LayoutInflater inflater  = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.app_label_editor, null);
            final EditText editText = (EditText) dialogView.findViewById(R.id.label_field);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setMessage("Enter the new name of the Appliance : ")
                    .setTitle("Rename Appliance")
                    .setCancelable(false)
                    .setPositiveButton("Save",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    final String app_name = editText.getText().toString();
                                    if(app_name.equals("")){
                                        Toast.makeText(_context,
                                                "You did not enter a valid name",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        App_Unit.this.setChildNm(app_name);
                                    }



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
            if (alertDialog.isShowing()) alertDialog.dismiss();
            alertDialog.show();
        }

        public int getAppController() {
            return appController;
        }
    }

    public class Group_Unit implements SettingAdapter.customSettingListener {
        String groupNm;
        String DeviceID;
        Context ctx;
        RoomData roomData;
        boolean status;
        Animation animation;
        ImageView status_icon;
        View groupView;


        public Group_Unit(Context context,RoomData rm,Boolean status,ImageView status2,View convertView) {
            ctx=context;
            roomData=rm;
            this.status=status;
            DeviceID=rm.getDeviceID();
            groupNm=rm.getRoomName();
            groupView=convertView;
            status_icon=status2;
            animation=AnimationUtils.loadAnimation(_context,R.anim.item_animation_from_right);
            animation.setDuration(200);
            if(status)status_icon.setImageResource(R.drawable.ic_device_online);
            else
                status_icon.setImageResource(R.drawable.ic_device_offline);

        }
        public String getGroupName() {
            return groupNm;
        }




        @Override
        public void onGroupDeleteListener(String value) {

            Toast.makeText(_context,
                    value+" Deleted",
                    Toast.LENGTH_SHORT).show();
            if (customListener != null) {
                customListener.onGroupDeleteListener(value);
            }
        }

        @Override
        public void onMasterControl(String deviceID, char set) {

            for(App_Unit abc:appCollection){
                if(abc.getDeviceID().equals(deviceID))abc.setToggle(set);
            }

        }

        @Override
        public void onAppAdd(RoomData value) {

            if (customListener != null) {
                customListener.onAppAdd(value);
                Log.i("AppAdd",value.toMap().toString());
            }

        }

        @Override
        public void onGroupRenameListener(final RoomData rm) {

            if (customListener != null) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(_context);
                LayoutInflater inflater = (LayoutInflater) _context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.app_label_editor, null);
                final EditText editText = (EditText) dialogView.findViewById(R.id.label_field);
                dialogBuilder.setView(dialogView);

                dialogBuilder.setMessage("Enter the new name of the room : ")
                        .setTitle("Rename Room")
                        .setCancelable(false)
                        .setPositiveButton("Save",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        final String grp_name = editText.getText().toString();
                                        customListener.onGroupRenameListener(rm.getRoomName(), grp_name);
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
        }

        public String getDeviceID() {
            return DeviceID;
        }

        public boolean getStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
            if(status){
                groupView.setBackgroundColor(Color.DKGRAY);
                status_icon.setImageResource(R.drawable.ic_device_online);
            }
            else{
                groupView.setBackgroundColor(Color.GRAY);
                status_icon.setImageResource(R.drawable.ic_device_offline);
            }

            status_icon.startAnimation(animation);
        }
    }

    public boolean isOnline() {


        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        Log.i("NETC",""+isConnected);

        return isConnected;
    }


    public interface customGroupButtonListener {
        public void onGroupDeleteListener(String value);
        public void onChildDeleteListener(String dev_id,int ChildPos);
        public void onButtonLongClickListener(int c_position,int g_position,String value);
        public void onAppAdd(RoomData value);
        public void onPublish(int position,String command,String deviceID);
        public void onChildRenameListener(String app_name,String ID,int controller);
        public void onGroupRenameListener(String group_name_old,String group_name_new);
    }

    public void setCustomGroupButtonListener(customGroupButtonListener listener) {
        this.customListener = listener;
    }

    @Override
    public Home.AppClass getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent);
        }
    };
    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_slide_right);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }
    private BroadcastReceiver mqtt = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MQTT","MQTT RECEIVER : "+ intent.getStringExtra("message")+" at topic : "+intent.getStringExtra("topic"));

            String topic=intent.getStringExtra("topic");

            String groupName=intent.getStringExtra("groupName");

            JSONObject status= null;
            try {
                status = new JSONObject(intent.getStringExtra("message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(topic.contains("offline"))
                if (status.has("offline")) {
                    Boolean dev_sta = Boolean.parseBoolean(status.optString("offline"));
                    String dev_id=topic.replace("/offline","");

                    Log.i("TESTING","Boolean Value : "+dev_sta);

                    if(dev_sta){


                        Toast.makeText(_context,
                                groupName+" Offline",
                                Toast.LENGTH_SHORT).show();
                        for(Group_Unit abc:groupCollection){
                            if(abc.getGroupName().equals(groupName))abc.setStatus(false);
                        }
                    }
                    if(!dev_sta){

                        Toast.makeText(_context,
                                groupName+" Online",
                                Toast.LENGTH_SHORT).show();
                        for(Group_Unit abc:groupCollection){
                            if(abc.getGroupName().equals(groupName))abc.setStatus(true);
                        }
                    }

                }
            if(topic.contains("status"))
                if (status.has("status")) {
                    String msg=status.optString("status");
                    String dev_id=topic.replace("/status","");
                    for (App_Unit temp:appCollection) {
                        Log.i("mqtt TEST","Checking"+ appCollection.size());
                        if(temp.checkDeviceID(dev_id)&&temp.getAppController()==1)temp.setToggle(msg.charAt(0));
                        if(temp.checkDeviceID(dev_id)&&temp.getAppController()==2)temp.setToggle(msg.charAt(1));
                        if(temp.checkDeviceID(dev_id)&&temp.getAppController()==3)temp.setToggle(msg.charAt(2));
                        if(temp.checkDeviceID(dev_id)&&temp.getAppController()==4)temp.setToggle(msg.charAt(3));
                    }

                    Log.i("TESTING","Received: "+msg+"   INDEX 1: "+msg.charAt(0));

                }

        }
    };


    private void updateGUI(Intent intent) {
        long millisUntilFinished = intent.getLongExtra("countdown", 0);
        if (millisUntilFinished==0 ){
            final int child_position=intent.getExtras().getInt("child_position");
            final String deviceID=intent.getExtras().getString("deviceID");
            int pid= intent.getExtras().getInt("pid");
            Log.i("COUNTDOWN","PID OVER FOR : "+pid);
            Log.i("CHECK10","Values : "+child_position+" : "+deviceID);
            if (active.getOrDefault(pid,false)){
                if(isOnline())
                {
                        App_Unit current=null;
                        for (App_Unit temp:appCollection) {
                            if(temp.getDeviceID().equals(deviceID) && temp.getAppController()==child_position)
                                current=temp;
                        }
                        current.setRunning(false);
                        current.setToggle('0');
                }
                else
                {
                    Toast.makeText(_context,
                            "No Internet Connection",
                            Toast.LENGTH_SHORT).show();
                }
            }

        }
        else{
            Log.i("COUNTDOWN","Running");
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static boolean checkImageResource(Context ctx, ImageView imageView,
                                             int imageResource) {
        boolean result = false;

        if (ctx != null && imageView != null && imageView.getDrawable() != null) {
            Drawable.ConstantState constantState;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                constantState = ctx.getResources()
                        .getDrawable(imageResource, ctx.getTheme())
                        .getConstantState();
            } else {
                constantState = ctx.getResources().getDrawable(imageResource)
                        .getConstantState();
            }

            if (imageView.getDrawable().getConstantState() == constantState) {
                result = true;
            }
        }

        return result;
    }
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {


        final RoomData currentGroup=getGroup(groupPosition);
        final String groupTitle = (String) getGroup(groupPosition).getRoomName();
        final String childText = getChild(groupPosition, childPosition).getAppName();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.appliance_row, null);
        }

        Animation animApp=AnimationUtils.loadAnimation(_context,R.anim.item_animation_fall_down);
        Switch app_switch = (Switch) convertView.findViewById(R.id.appSwitch);

       ImageView app_alarm = (ImageView) convertView.findViewById(R.id.app_alarm2);

        TextView txtListChild = (TextView) convertView.findViewById(R.id.appTextView);
        txtListChild.setText(childText);
        txtListChild.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (customListener != null) {
                    customListener.onButtonLongClickListener(childPosition,groupPosition,groupTitle);
                }
                return true;
            }
        });
        boolean RUN=false;
        final boolean[] enabled = {false};
        boolean checked=false;
        App_Unit thisView = null;
        for (App_Unit abc:appCollection){
            if(abc.getDeviceID().equals(currentGroup.getDeviceID())&& abc.getChildPos()==childPosition){
                RUN=abc.getRunning();
                enabled[0] =abc.getEnabled();
                checked=abc.getToggleState();
                appCollection.remove(abc);
                abc=null;
                break;
            }
        }
        ImageView settings = (ImageView) convertView.findViewById(R.id.app_settings);
        thisView=new App_Unit(_context,groupTitle,childText,groupPosition,childPosition,app_alarm,app_switch,txtListChild,getChild(groupPosition, childPosition));
        thisView.setEnabled(enabled[0]);
        appCollection.add(thisView);
        Log.i("CHECK10","Values : "+thisView.getAppController()+" : "+thisView.getDeviceID());
        thisView.setRunning(RUN);
        if(enabled[0])app_alarm.setVisibility(View.VISIBLE);
        else app_alarm.setVisibility(View.INVISIBLE);
        char togg;
        if(checked)togg='1';
        else togg='0';
        thisView.setToggle(togg);


        animApp.setStartOffset(50*thisView.getAppController());
        txtListChild.setAnimation(animApp);
        final App_Unit finalThisView = thisView;

        app_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalThisView.setAlarm();
            }
        });
        animApp.setStartOffset(20*finalThisView.getAppController());
        settings.setAnimation(animApp);
        settings.setColorFilter(Color.RED);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strList;
                if(!enabled[0])strList="Enable Alarm";
                else strList="Disable Alarm";
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(_context,R.style.CustomDialog);
                builderSingle.setIcon(R.drawable.ic_settings);
                builderSingle.setTitle("Setting ("+"Controller : "+finalThisView.getAppController()+")");
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(_context, android.R.layout.select_dialog_item);
                arrayAdapter.add("Rename");
                arrayAdapter.add(strList);
                arrayAdapter.add("Delete");
                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                       switch(strName){
                           case "Rename": finalThisView.rename();break;
                           case "Delete": finalThisView.onDelete();break;
                           case "Enable Alarm": enabled[0] =true;finalThisView.setEnabled(true);finalThisView.getAlarm().setVisibility(View.VISIBLE);break;
                           case "Disable Alarm": enabled[0] =false;finalThisView.setEnabled(false);finalThisView.getAlarm().setVisibility(View.INVISIBLE);
                                 finalThisView.cancelAlarm();
                           break;
                       }
                    }
                });
                builderSingle.show();

            }
        });



        Animation anim=AnimationUtils.loadAnimation(_context,R.anim.item_animation_from_right);
        anim.setStartOffset(thisView.getAppController()*100);
        convertView.setAnimation(anim);

        animApp.setStartOffset(50*finalThisView.getAppController());
        app_switch.setAnimation(animApp);

        return convertView;

    }


    @Override
    public int getChildrenCount(int groupPosition) {
     int i=0;
        try {
       i = this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
   }
   catch (Exception e){
       e.printStackTrace();
   }
   return i;
   }

    @Override
    public RoomData getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final RoomData currentGroup=getGroup(groupPosition);
        final String headerTitle = (String) getGroup(groupPosition).getRoomName();
        boolean device_status=false;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
        if(isExpanded){
            convertView.setBackgroundColor(Color.GRAY);
            convertView.setMinimumHeight(500);
            convertView.setAnimation(AnimationUtils.loadAnimation(_context,R.anim.item_zoom));

        }


        else if(!isExpanded)
        {
            convertView.startAnimation(AnimationUtils.loadAnimation(_context,R.anim.item_animation_fall_down));
            convertView.setMinimumHeight(50);
            convertView.setBackgroundColor(Color.WHITE);


        }
        final TextView lblListHeader = (TextView) convertView.findViewById(R.id.ListItem5);
        lblListHeader.setTypeface(Typeface.SANS_SERIF,Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        ImageView status=(ImageView)convertView.findViewById(R.id.device_status2);


        Group_Unit thisGroup=null;

        for (Group_Unit abc:groupCollection){
            if(abc.getDeviceID().equals(currentGroup.getDeviceID())){

                device_status=abc.getStatus();
                groupCollection.remove(abc);
                break;
            }
        }



        final ImageView settings=(ImageView)convertView.findViewById(R.id.device_settings);
        thisGroup=new Group_Unit(_context,currentGroup,device_status,status,convertView,isExpanded);
        groupCollection.add(thisGroup);

        final Group_Unit finalThisGroup = thisGroup;

        if(device_status&&isExpanded){
            convertView.setBackgroundColor(Color.BLACK);
            settings.setColorFilter(Color.WHITE);
            lblListHeader.setTextColor(Color.WHITE);
        }

        if(device_status&&!isExpanded){
            convertView.setBackgroundColor(Color.WHITE);
            settings.setColorFilter(Color.BLACK);
            lblListHeader.setTextColor(Color.BLACK);
        }
        if(!device_status&&isExpanded){
            convertView.setBackgroundColor(Color.GRAY);
            settings.setColorFilter(Color.BLACK);
            lblListHeader.setTextColor(Color.BLACK);
        }
        if(!device_status&&!isExpanded){
            convertView.setBackgroundColor(Color.WHITE);
            settings.setColorFilter(Color.BLACK);
            lblListHeader.setTextColor(Color.BLACK);
        }
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(_context);
                LayoutInflater inflater  = (LayoutInflater) _context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.layout_settings, null);
                RecyclerView mlist=(RecyclerView)dialogView.findViewById(R.id.my_recycler_view);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(_context);
                mlist.setLayoutManager(mLayoutManager);
                List<SettingsClass> list=new ArrayList<>();

                SettingAdapter settingAdapter= new SettingAdapter(list,_context);
                settingAdapter.setCustomSettingListener(finalThisGroup);
                Log.i("CHECK21",list.toString());
                mlist.setAdapter(settingAdapter);
                mlist.setHasFixedSize(true);
                settingAdapter.notifyDataSetChanged();
                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle("Settings for "+currentGroup.getRoomName())
                             .setIcon(R.drawable.ic_settings)
                             .setCancelable(true);
                dialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final AlertDialog alertDialog = dialogBuilder.create();
                list.add(new SettingsClass(R.drawable.ic_add,"Add App",currentGroup,"add",alertDialog));
                list.add(new SettingsClass(R.drawable.ic_rename,"Rename",currentGroup,"rename",alertDialog));
                list.add(new SettingsClass(R.drawable.ic_flash_on,"Room ON",currentGroup,"room_on",alertDialog));
                list.add(new SettingsClass(R.drawable.ic_flash_off,"Room OFF",currentGroup,"room_off",alertDialog));
                list.add(new SettingsClass(R.drawable.del,"Delete",currentGroup,"delete",alertDialog));
                runLayoutAnimation(mlist);
                alertDialog.show();
            }
        });


        if(isExpanded) settings.setAnimation(AnimationUtils.loadAnimation(_context,R.anim.item_rotate));

        else settings.setAnimation(null);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}

