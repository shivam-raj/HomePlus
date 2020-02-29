package com.ampereplus.homeplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Splashscreen extends AppCompatActivity {


    private static final int RC_SIGN_IN = 123;
    ConnectivityManager cm;
    FirebaseUser user;

    public boolean isOnline() {

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            Log.i("NETC",""+isConnected);
            return isConnected;
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        user=FirebaseAuth.getInstance().getCurrentUser();
        
            final List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

            cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            Boolean onlinetell=isOnline();

            if(!onlinetell) {
                Toast.makeText(Splashscreen.this, "No internet access", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setMessage("No Internet Access")
                        .setTitle("Error Connecting...")
                        .setCancelable(false)
                        .setPositiveButton("Try Again",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                }
                        )
                        .setNeutralButton("Settings",  new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .setNegativeButton("Exit",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        Splashscreen.this.finish();
                                    }
                                }
                        );
                final AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Boolean wantToCloseDialog = false;
                        wantToCloseDialog=isOnline();
                        if(wantToCloseDialog){
                            startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setAvailableProviders(providers)
                                            .build(),
                                    RC_SIGN_IN);
                            alertDialog.dismiss();
                        }
                        else{
                            Toast.makeText(Splashscreen.this, "No internet access", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Boolean wantToCloseDialog = false;

                        if(wantToCloseDialog){

                            alertDialog.dismiss();
                        }
                        else{
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }


                    }
                });
            }
            else if(onlinetell)
            {
                if(user==null) startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
                 else {
                    startActivity(new Intent(Splashscreen.this, Home.class));
                }
            }


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {

                startActivity(new Intent(Splashscreen.this,Home.class));


            } else {

                Log.i("LOGIN ERROR",""+resultCode);
                Toast.makeText(Splashscreen.this, "Could not sign in", Toast.LENGTH_SHORT).show();

            }
        }
    }

}