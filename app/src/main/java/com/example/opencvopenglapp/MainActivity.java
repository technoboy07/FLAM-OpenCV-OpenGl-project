package com.example.opencvopenglapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    
    private CameraGLSurfaceView glSurfaceView;
    private CameraHandler cameraHandler;
    private Button toggleProcessingButton;
    private Button modeButton;
    private TextView fpsTextView;
    private TextView resolutionTextView;
    
    private boolean isProcessingEnabled = true;
    private int currentMode = 0;
    private String[] modeNames = {"Grayscale", "Canny Edge", "Blur", "Original"};
    
    private FPSMonitor fpsMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupCamera();
        setupFPSMonitor();
        
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void initViews() {
        glSurfaceView = findViewById(R.id.gl_surface_view);
        toggleProcessingButton = findViewById(R.id.toggle_processing_button);
        modeButton = findViewById(R.id.mode_button);
        fpsTextView = findViewById(R.id.fps_text);
        resolutionTextView = findViewById(R.id.resolution_text);
        
        toggleProcessingButton.setOnClickListener(v -> toggleProcessing());
        modeButton.setOnClickListener(v -> cycleMode());
        
        updateModeButtonText();
    }

    private void setupCamera() {
        cameraHandler = new CameraHandler(this);
        cameraHandler.setGLSurfaceView(glSurfaceView);
        glSurfaceView.setCameraHandler(cameraHandler);
    }

    private void setupFPSMonitor() {
        fpsMonitor = new FPSMonitor(fps -> runOnUiThread(() -> {
            fpsTextView.setText("FPS: " + String.format("%.1f", fps));
        }));
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        try {
            cameraHandler.startPreview();
            updateResolutionText();
            Log.d(TAG, "Camera started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start camera", e);
            Toast.makeText(this, "Failed to start camera", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleProcessing() {
        isProcessingEnabled = !isProcessingEnabled;
        glSurfaceView.setProcessingEnabled(isProcessingEnabled);
        
        String buttonText = isProcessingEnabled ? "Disable Processing" : "Enable Processing";
        toggleProcessingButton.setText(buttonText);
        
        Toast.makeText(this, 
                isProcessingEnabled ? "Processing enabled" : "Processing disabled", 
                Toast.LENGTH_SHORT).show();
    }

    private void cycleMode() {
        currentMode = (currentMode + 1) % modeNames.length;
        glSurfaceView.setProcessingMode(currentMode);
        updateModeButtonText();
        
        Toast.makeText(this, "Mode: " + modeNames[currentMode], Toast.LENGTH_SHORT).show();
    }

    private void updateModeButtonText() {
        modeButton.setText("Mode: " + modeNames[currentMode]);
    }

    private void updateResolutionText() {
        if (cameraHandler.getPreviewSize() != null) {
            String resolution = cameraHandler.getPreviewSize().getWidth() + "x" + 
                              cameraHandler.getPreviewSize().getHeight();
            resolutionTextView.setText("Resolution: " + resolution);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraHandler != null && !cameraHandler.isPreviewRunning()) {
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraHandler != null) {
            cameraHandler.stopPreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (glSurfaceView != null) {
            glSurfaceView.cleanup();
        }
        if (cameraHandler != null) {
            cameraHandler.stopPreview();
        }
    }
}
