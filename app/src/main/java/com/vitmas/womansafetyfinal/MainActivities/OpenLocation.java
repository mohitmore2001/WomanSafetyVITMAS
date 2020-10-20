package com.vitmas.womansafetyfinal.MainActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.vitmas.womansafetyfinal.LocationsActivities.MapsActivity;
import com.vitmas.womansafetyfinal.R;

public class OpenLocation extends AppCompatActivity {
    private Button buttonPermissions,openMaps;
    private Context mContext=OpenLocation.this;
    private int count=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_location);
        buttonPermissions=(Button) findViewById(R.id.permissionbutton);
        openMaps=(Button) findViewById(R.id.openmaps) ;
        buttonPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permission();
            }
        });
        openMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OpenLocation.this, MapsActivity.class));
            }
        });
    }
    private void permission(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

// Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
// Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
// On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                buttonPermissions.setVisibility(View.INVISIBLE);
                openMaps.setVisibility(View.VISIBLE);
            }
        });
// on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
// Showing Alert Message
        alertDialog.show();
    }
}