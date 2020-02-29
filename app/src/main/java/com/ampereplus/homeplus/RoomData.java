package com.ampereplus.homeplus;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomData {

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setApp1Name(String app1Name) {
        this.app1Name = app1Name;
    }

    public void setApp1Controller(int app1Controller) {
        this.app1Controller = app1Controller;
    }

    public void setApp2Name(String app2Name) {
        this.app2Name = app2Name;
    }

    public void setApp2Controller(int app2Controller) {
        this.app2Controller = app2Controller;
    }

    public void setApp3Name(String app3Name) {
        this.app3Name = app3Name;
    }

    public void setApp3Controller(int app3Controller) {
        this.app3Controller = app3Controller;
    }

    public void setApp4Name(String app4Name) {
        this.app4Name = app4Name;
    }

    public void setApp4Controller(int app4Controller) {
        this.app4Controller = app4Controller;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getApp1Name() {
        return app1Name;
    }

    public int getApp1Controller() {
        return app1Controller;
    }

    public String getApp2Name() {
        return app2Name;
    }

    public int getApp2Controller() {
        return app2Controller;
    }

    public String getApp3Name() {
        return app3Name;
    }

    public int getApp3Controller() {
        return app3Controller;
    }

    public String getApp4Name() {
        return app4Name;
    }

    public int getApp4Controller() {
        return app4Controller;
    }

    private String roomName;
    private String deviceID;
    private String app1Name;
    private int app1Controller;
    private String app2Name;
    private int app2Controller;
    private String app3Name;
    private int app3Controller;
    private String app4Name;
    private int app4Controller;


    public RoomData(String deviceID){
        this.deviceID=deviceID;
        this.roomName="";
        app1Name="";
        app2Name="";
        app3Name="";
        app4Name="";
        app1Controller=0;
        app2Controller=0;
        app3Controller=0;
        app4Controller=0;
    }
    public RoomData(){
        this.deviceID="";
        this.roomName="";
        app1Name="";
        app2Name="";
        app3Name="";
        app4Name="";
        app1Controller=0;
        app2Controller=0;
        app3Controller=0;
        app4Controller=0;
    }

    public RoomData(String roomName,String deviceID) {

        this.roomName=roomName;
        this.deviceID=deviceID;
        app1Name="";
        app2Name="";
        app3Name="";
        app4Name="";
        app1Controller=0;
        app2Controller=0;
        app3Controller=0;
        app4Controller=0;

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("roomName", getRoomName());
        result.put("deviceID", getDeviceID());
        result.put("app1Name", getApp1Name());
        result.put("app2Name", getApp2Name());
        result.put("app3Name", getApp3Name());
        result.put("app4Name", getApp4Name());
        result.put("app1Controller", getApp1Controller());
        result.put("app2Controller", getApp2Controller());
        result.put("app3Controller", getApp3Controller());
        result.put("app4Controller", getApp4Controller());
        return  result;
    }

    @Exclude
    public List<Integer> availableIndex(){
        List<Integer> indexes=new ArrayList<>();
        Integer a=1,b=2,c=3,d=4;
        indexes.add(a);
        indexes.add(b);
        indexes.add(c);
        indexes.add(d);





        if(!getApp1Name().equals(""))indexes.remove(a);
        if(!getApp2Name().equals(""))indexes.remove(b);
        if(!getApp3Name().equals(""))indexes.remove(c);
        if(!getApp4Name().equals(""))indexes.remove(d);

        return indexes;
    }


}
