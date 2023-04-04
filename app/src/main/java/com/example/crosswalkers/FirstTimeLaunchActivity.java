package com.example.crosswalkers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class FirstTimeLaunchActivity extends AppCompatActivity {
    private int CAMERA_PERMISSION_CODE = 1;
    private String permissionGranted = "permission granted";

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(permissionGranted, false)==true) {
            goToMain();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_launch);


        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(permissionGranted, false)==true) {
            goToMain();
        }
        else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(permissionGranted, Boolean.FALSE);
            editor.apply();
        }

        Button requestPermission = findViewById(R.id.continueButton);
        requestPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(FirstTimeLaunchActivity.this,
                        android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(permissionGranted, Boolean.TRUE);
                    editor.apply();
                    goToMain();
                }
                else {
                    requestCameraPermissions();
                }
            }
        });
    }

    public void goToMain(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    private void requestCameraPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.CAMERA)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This app requires access to the camera in order to function. Please allow camera access.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(FirstTimeLaunchActivity.this,new String[]{android.Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
                            if (ContextCompat.checkSelfPermission(FirstTimeLaunchActivity.this,
                                    android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(FirstTimeLaunchActivity.this, "You have already granted this permission", Toast.LENGTH_SHORT).show();
                                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(permissionGranted, Boolean.TRUE);
                                editor.apply();
                                goToMain();
                            }
                            else {
                                requestCameraPermissions();
                            }
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int i){
                        dialog.dismiss();
                        }
                 })
                    .create().show();
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);//why needed?
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(FirstTimeLaunchActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(permissionGranted, Boolean.TRUE);
                editor.apply();
            }
        } else {
            Toast.makeText(FirstTimeLaunchActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

}