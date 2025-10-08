package com.example.opencvopenglapp;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SamsungCameraHandler {
    private static final String TAG = "SamsungCameraHandler";
    
    private Context context;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraGLSurfaceView glSurfaceView;
    
    // CameraX components
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    
    private String cameraId;
    private Size previewSize;
    private boolean isPreviewRunning = false;
    private boolean useCameraX = false;
    private String manufacturer;

    public SamsungCameraHandler(Context context) {
        this.context = context;
        this.manufacturer = Build.MANUFACTURER.toLowerCase();
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        
        Log.d(TAG, "Manufacturer: " + manufacturer + ", Model: " + Build.MODEL);
        
        // Use CameraX for Samsung devices as primary option
        if (manufacturer.contains("samsung")) {
            Log.d(TAG, "Samsung device detected - using CameraX for better compatibility");
            useCameraX = true;
        }
    }

    public void setGLSurfaceView(CameraGLSurfaceView surfaceView) {
        this.glSurfaceView = surfaceView;
    }

    public void startPreview() {
        if (useCameraX) {
            startCameraXPreview();
        } else {
            startCamera2Preview();
        }
    }
    
    private void startCameraXPreview() {
        Log.d(TAG, "Starting CameraX preview");
        
        // For now, fall back to Camera2 for Samsung devices
        // CameraX integration with OpenGL SurfaceTexture is complex
        Log.d(TAG, "CameraX not fully implemented yet, using Camera2 for Samsung");
        useCameraX = false;
        startCamera2Preview();
    }

    private void startCamera2Preview() {
        Log.d(TAG, "Starting Camera2 preview");
        
        try {
            cameraId = getCameraId();
            if (cameraId == null) {
                Log.e(TAG, "No camera found");
                return;
            }

            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            
            if (map == null) {
                Log.e(TAG, "Stream configuration map is null");
                return;
            }

            // Get optimal preview size
            Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            previewSize = chooseOptimalSize(sizes, 1920, 1080);
            Log.d(TAG, "Selected preview size: " + previewSize.getWidth() + "x" + previewSize.getHeight());

            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCaptureSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "Camera error: " + error);
                    camera.close();
                    cameraDevice = null;
                    
                    // Try CameraX as fallback if Camera2 fails
                    if (!useCameraX) {
                        Log.d(TAG, "Camera2 failed, trying CameraX fallback");
                        useCameraX = true;
                        startCameraXPreview();
                    }
                }
            }, null);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception", e);
            
            // Try CameraX as fallback
            if (!useCameraX) {
                Log.d(TAG, "Camera2 access failed, trying CameraX fallback");
                useCameraX = true;
                startCameraXPreview();
            }
        }
    }

    private void createCaptureSession() {
        try {
            // Ensure SurfaceTexture is created before proceeding
            glSurfaceView.createSurfaceTexture();
            SurfaceTexture surfaceTexture = glSurfaceView.getSurfaceTexture();
            
            if (surfaceTexture == null) {
                Log.e(TAG, "Failed to create SurfaceTexture");
                isPreviewRunning = false;
                return;
            }

            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface surface = new Surface(surfaceTexture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(
                Collections.singletonList(surface),
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        captureSession = session;
                        try {
                            // Samsung-specific camera settings
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                            
                            // Samsung optimization: reduce frame rate for stability
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, 
                                new android.util.Range<>(15, 30));
                            
                            // Samsung-specific: disable noise reduction for better performance
                            captureRequestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, 
                                CaptureRequest.NOISE_REDUCTION_MODE_OFF);
                            
                            // Samsung-specific: set color correction mode
                            captureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, 
                                CaptureRequest.COLOR_CORRECTION_MODE_FAST);
                            
                            session.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                            isPreviewRunning = true;
                            Log.d(TAG, "Camera2 preview started successfully");
                            
                            // Show success message
                            if (context instanceof android.app.Activity) {
                                ((android.app.Activity) context).runOnUiThread(() -> 
                                    Toast.makeText(context, "Camera2 started successfully", Toast.LENGTH_SHORT).show());
                            }
                            
                        } catch (CameraAccessException e) {
                            Log.e(TAG, "Failed to start preview", e);
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Log.e(TAG, "Capture session configuration failed");
                        
                        // Try CameraX as fallback
                        if (!useCameraX) {
                            Log.d(TAG, "Camera2 session failed, trying CameraX fallback");
                            useCameraX = true;
                            startCameraXPreview();
                        }
                    }
                },
                null
            );
            
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to create capture session", e);
            isPreviewRunning = false;
        }
    }

    public void stopPreview() {
        if (useCameraX && cameraProvider != null) {
            // Stop CameraX
            cameraProvider.unbindAll();
            camera = null;
            preview = null;
            imageAnalysis = null;
            Log.d(TAG, "CameraX preview stopped");
        } else if (captureSession != null) {
            // Stop Camera2
            try {
                captureSession.stopRepeating();
                captureSession.close();
                captureSession = null;
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to stop capture session", e);
            }
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        
        isPreviewRunning = false;
        Log.d(TAG, "Preview stopped");
    }

    private String getCameraId() {
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            Log.d(TAG, "Available cameras: " + Arrays.toString(cameraIds));
            
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    Log.d(TAG, "Selected back camera: " + id);
                    return id;
                }
            }
            
            // Fallback to first available camera
            if (cameraIds.length > 0) {
                Log.d(TAG, "Using first available camera: " + cameraIds[0]);
                return cameraIds[0];
            }
            
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to get camera list", e);
        }
        return null;
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = Arrays.asList(choices);
        Collections.sort(bigEnough, new CompareSizesByArea());
        
        // Samsung devices prefer specific resolutions
        for (Size option : bigEnough) {
            if (option.getHeight() <= height && option.getWidth() <= width) {
                Log.d(TAG, "Chosen size: " + option.getWidth() + "x" + option.getHeight());
                return option;
            }
        }
        
        Size fallback = bigEnough.get(bigEnough.size() - 1);
        Log.d(TAG, "Fallback size: " + fallback.getWidth() + "x" + fallback.getHeight());
        return fallback;
    }

    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    public boolean isPreviewRunning() {
        return isPreviewRunning;
    }

    public Size getPreviewSize() {
        if (useCameraX) {
            return new Size(1920, 1080); // Default for CameraX
        }
        return previewSize;
    }
    
    public boolean isUsingCameraX() {
        return useCameraX;
    }
    
    public void onSurfaceTextureReady() {
        Log.d(TAG, "SurfaceTexture is ready, starting camera preview");
        // The camera preview will start automatically when SurfaceTexture is available
    }
}
