package com.example.crosswalkers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class PermissionDenied extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_denied);

        Button requestPermission = findViewById(R.id.continueButton);
        requestPermission.setOnClickListener(view -> checkPermission());
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            SharedPreferences sharedPreferences1 = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences1.edit();
            editor.putBoolean(FirstTimeLaunchActivity.permissionDenied, Boolean.FALSE);
            editor.putBoolean(FirstTimeLaunchActivity.permissionGranted, Boolean.TRUE);
            editor.apply();
            FirstTimeLaunchActivity.permissionDeniedCount =0;
            goToMain();
        }
        else{
            Toast.makeText(this, "Permission has not been granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void goToMain(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}