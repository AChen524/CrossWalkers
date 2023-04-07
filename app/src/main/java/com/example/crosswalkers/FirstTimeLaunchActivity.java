package com.example.crosswalkers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class FirstTimeLaunchActivity extends AppCompatActivity {
    final int CAMERA_PERMISSION_CODE = 1;
    public final static String permissionGranted = "permission granted";
    public final static String permissionDenied = "permission denied multiple times";

    public static int permissionDeniedCount =0;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(permissionGranted, false)) {
            permissionDeniedCount = 0;
            goToMain();
        }
        if (sharedPreferences.getBoolean(permissionDenied, false)) {
            goToPermissionDeniedScreen();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_launch);


        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(permissionGranted, false)) {
            permissionDeniedCount = 0;
            goToMain();
        }
        else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(permissionGranted, Boolean.FALSE);
            editor.apply();
        }

        if (sharedPreferences.getBoolean(permissionDenied, false)) {
            goToPermissionDeniedScreen();
        }
        else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(permissionDenied, Boolean.FALSE);
            editor.apply();
        }

        Button requestPermission = findViewById(R.id.continueButton);
        requestPermission.setOnClickListener(view -> {

            if (ContextCompat.checkSelfPermission(FirstTimeLaunchActivity.this,
                    android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                SharedPreferences sharedPreferences1 = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                editor.putBoolean(permissionGranted, Boolean.TRUE);
                editor.apply();
                permissionDeniedCount =0;
                goToMain();
            }
            else if (permissionDeniedCount>1){
                SharedPreferences sharedPreferences1 = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                editor.putBoolean(permissionDenied, Boolean.TRUE);
                editor.apply();
                goToPermissionDeniedScreen();
            }
            else{
                permissionDeniedCount++;
                requestCameraPermissions();
            }
        });
    }


    private void requestCameraPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.CAMERA)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This app requires access to the camera in order to function. Please allow camera access.")
                    .setPositiveButton("ok", (dialogInterface, i) -> {
                        ActivityCompat.requestPermissions(FirstTimeLaunchActivity.this,new String[]{android.Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
                        if (ContextCompat.checkSelfPermission(FirstTimeLaunchActivity.this,
                                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(FirstTimeLaunchActivity.this, "You have already granted this permission", Toast.LENGTH_SHORT).show();
                            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(permissionGranted, Boolean.TRUE);
                            editor.apply();
                            permissionDeniedCount = 0;
                            goToMain();
                        }
                        else {permissionDeniedCount++;}
                    })
                    .setNegativeButton("cancel", (dialog, i) -> {
                        permissionDeniedCount++;
                        dialog.dismiss();
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


    public void goToMain(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void goToPermissionDeniedScreen(){
        Intent intent = new Intent(this,PermissionDenied.class);
        startActivity(intent);
    }

}