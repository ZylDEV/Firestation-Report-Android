package com.example.halomerah;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class camera2 extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private PreviewView previewView;
    private Button captureButton;
    private ImageCapture imageCapture;
    private boolean isBackCamera = true;
    private GestureDetector gestureDetector;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        previewView = findViewById(R.id.viewFinder);
        captureButton = findViewById(R.id.captureButton);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                isBackCamera = !isBackCamera;
                switchCamera();
                return true;
            }
        });

        previewView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Gagal mengakses kamera", e);
                Toast.makeText(this, "Gagal mengakses kamera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));

        captureButton.setOnClickListener(v -> capturePhoto());
    }

    private void switchCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Gagal beralih kamera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector;
        try {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(isBackCamera ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT)
                    .build();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Kamera tidak tersedia", Toast.LENGTH_SHORT).show();
            isBackCamera = true; // fallback ke kamera belakang
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
        }

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private void capturePhoto() {
        File photoFile = new File(
                getExternalFilesDir(null),
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        Intent resultIntent = new Intent(camera2.this, akun.class);
                        resultIntent.putExtra("photoUri", savedUri.toString());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Gagal mengambil foto", exception);
                        Toast.makeText(camera2.this, "Gagal mengambil foto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap getBitmapFromFile(File photoFile) {
        try (FileInputStream fis = new FileInputStream(photoFile)) {
            return BitmapFactory.decodeStream(fis);
        } catch (IOException e) {
            Log.e(TAG, "Gagal membaca file gambar", e);
            return null;
        }
    }
}
