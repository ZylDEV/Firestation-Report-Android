package com.example.halomerah;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

public class camera extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private PreviewView previewView;
    private Button captureButton;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    private boolean isFrontCamera = false;  // Track the current camera (false = back, true = front)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.viewFinder);
        captureButton = findViewById(R.id.captureButton);

        Log.d(TAG, "onCreate: Memulai CameraActivity");

        // Camera provider setup
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                Log.d(TAG, "cameraProviderFuture.addListener: Menunggu cameraProvider");

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Log.d(TAG, "cameraProviderFuture.addListener: CameraProvider siap");
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Gagal mengakses kamera", e);
                Toast.makeText(this, "Gagal mengakses kamera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));

        captureButton.setOnClickListener(v -> capturePhoto());

        // Detecting double click to switch between front and back camera
        previewView.setOnTouchListener(new View.OnTouchListener() {
            long lastClickTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    long currentClickTime = System.currentTimeMillis();
                    if (currentClickTime - lastClickTime < 300) {
                        switchCamera();  // Switch camera on double click
                    }
                    lastClickTime = currentClickTime;
                }
                return true;
            }
        });
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Log.d(TAG, "bindCameraUseCases: Menyiapkan kamera");

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(isFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        // Unbind all before rebinding
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

        Log.d(TAG, "bindCameraUseCases: Kamera siap digunakan");
    }

    private void switchCamera() {
        // Toggle camera lens facing
        isFrontCamera = !isFrontCamera;
        Log.d(TAG, "switchCamera: Kamera berubah, depan = " + isFrontCamera);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);  // Rebind with the new camera selection
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Gagal mengakses kamera", e);
                Toast.makeText(this, "Gagal mengakses kamera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        Log.d(TAG, "capturePhoto: Memulai pengambilan foto");

        File photoFile = new File(
                getExternalFilesDir(null),
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg"
        );

        Log.d(TAG, "capturePhoto: Menyimpan foto ke file " + photoFile.getAbsolutePath());

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        Log.d(TAG, "onImageSaved: Foto disimpan di: " + savedUri);
                        Toast.makeText(camera.this, "Foto disimpan", Toast.LENGTH_SHORT).show();

                        // Kirimkan URI foto ke Maps Activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("photoUri", savedUri.toString());  // Mengirim URI sebagai String
                        Log.d(TAG, "onImageSaved: URI yang dikirimkan: " + savedUri.toString());
                        setResult(RESULT_OK, resultIntent);  // Menyimpan hasil untuk Activity yang memanggil
                        finish(); // Kembali ke Activity Maps
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Gagal mengambil foto", exception);
                        Toast.makeText(camera.this, "Gagal mengambil foto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap getBitmapFromFile(File photoFile) {
        Log.d(TAG, "getBitmapFromFile: Membaca file gambar dari " + photoFile.getAbsolutePath());
        try (FileInputStream fis = new FileInputStream(photoFile)) {
            Bitmap bitmap = BitmapFactory.decodeStream(fis);  // Mengonversi file gambar menjadi Bitmap
            Log.d(TAG, "getBitmapFromFile: Bitmap berhasil dibaca");
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "Gagal membaca file gambar", e);
            return null;
        }
    }
}
