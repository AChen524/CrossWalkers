package com.example.crosswalkers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.crosswalkers.ml.Model;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private TextView confidenceText;

    private MediaPlayer ding;
    private MediaPlayer wait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ding = MediaPlayer.create(MainActivity.this, R.raw.walk);
        wait = MediaPlayer.create(MainActivity.this, R.raw.wait);
        previewView = findViewById(R.id.previewView);
        confidenceText = findViewById(R.id.confidenceText);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());

    }

    private Executor getExecutor(){
        return ContextCompat.getMainExecutor(this);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider){
        cameraProvider.unbindAll();

        //Camera Selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        //Preview use case
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //Image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        //Camera controller
        LifecycleCameraController controller = new LifecycleCameraController(this);
        controller.unbind();
        controller.bindToLifecycle(this);
        previewView.setController(controller);

        // Camera control features
        controller.setLinearZoom(0.8F);
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        Log.d("TAG", "analyze: gpt the frame at: " + image.getImageInfo().getTimestamp());

        final Bitmap bitmap = previewView.getBitmap();
        image.close();

        if (bitmap == null)
            return;

        classifyImage(bitmap);
    }

    @SuppressLint("SetTextI18n")
    private void classifyImage(Bitmap bitmap){
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(bitmap);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(image);

            List<Category> probability = outputs.getProbabilityAsCategoryList();
            Log.d("ML",probability.get(0) + " " + probability.get(1));

              //confidenceText.setText("Walk: " + Math.round(probability.get(1).getScore()*100) + "%" + " Don't walk: " + Math.round(probability.get(0).getScore()*100) + "%");

                if (probability.get(0).getScore()>0.75){
                   confidenceText.setText("Crosswalk detected: \n do not cross " + Math.round(probability.get(0).getScore()*100) + "%");
                   wait.start();
                }
                else if (probability.get(1).getScore()>0.90){
                    confidenceText.setText("Crosswalk detected: \n it is safe to cross " + Math.round(probability.get(1).getScore()*100) + "%");
                    ding.start();
                }
                else {
                    confidenceText.setText("Scanning… No crosswalks detected.");
                }

                // Releases model resources if no longer used.
            model.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void openHelpPage(View v){
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }
}
