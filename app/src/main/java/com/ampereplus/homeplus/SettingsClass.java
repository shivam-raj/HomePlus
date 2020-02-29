package com.ampereplus.homeplus;

import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

class SettingsClass {

    public int getIcon() {
        return icon;
    }

    public String getSettingText() {
        return settingText;
    }

    int icon;
    String settingText;

    String ref;
    AlertDialog bd;
    RoomData groupRef;

    public SettingsClass(int icon, String settingText, RoomData grpName, String ref, AlertDialog bd) {
        this.icon = icon;
        this.settingText = settingText;
        this.groupRef=grpName;
        this.ref=ref;
        this.bd=bd;
    }

    public void cancel(){
        bd.dismiss();
    }

    public RoomData getGroup() {
        return groupRef;
    }

    public String getRef() {
        return ref;
    }
}
