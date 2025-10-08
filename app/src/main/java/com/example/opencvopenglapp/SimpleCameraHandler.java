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
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SimpleCameraHandler {
    private static final String TAG = "SimpleCameraHandler";
    
    private Context context;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraGLSurfaceView glSurfaceView;
    
    private String cameraId;
    private Size previewSize;
    private boolean isPreviewRunning = false;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    public SimpleCameraHandler(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        startBackgroundThread();
        
        Log.d(TAG, "Manufacturer: " + Build.MANUFACTURER + ", Model: " + Build.MODEL);
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping background thread", e);
            }
        }
    }

    public void setGLSurfaceView(CameraGLSurfaceView surfaceView) {
        this.glSurfaceView = surfaceView;
    }

    public void startPreview() {
        Log.d(TAG, "Starting camera preview");
        
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

            // Get optimal preview size - use smaller size for Samsung stability
            Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            previewSize = chooseOptimalSize(sizes, 1280, 720); // Reduced from 1920x1080
            Log.d(TAG, "Selected preview size: " + previewSize.getWidth() + "x" + previewSize.getHeight());

            // Open camera with background handler
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.d(TAG, "Camera opened successfully");
                    cameraDevice = camera;
                    createCaptureSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.w(TAG, "Camera disconnected");
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "Camera error: " + error);
                    camera.close();
                    cameraDevice = null;
                }
            }, backgroundHandler); // Use background handler

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception", e);
        }
    }

    private void createCaptureSession() {
        try {
            // Ensure SurfaceTexture is created
            glSurfaceView.createSurfaceTexture();
            SurfaceTexture surfaceTexture = glSurfaceView.getSurfaceTexture();
            
            if (surfaceTexture == null) {
                Log.e(TAG, "Failed to create SurfaceTexture");
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
                        Log.d(TAG, "Capture session configured");
                        captureSession = session;
                        try {
                            // Minimal settings for Samsung compatibility
                            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                            
                            // Samsung-specific: use lower frame rate
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, 
                                new android.util.Range<>(10, 15));
                            
                            session.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
                            isPreviewRunning = true;
                            Log.d(TAG, "Camera preview started successfully");
                            
                        } catch (CameraAccessException e) {
                            Log.e(TAG, "Failed to start preview", e);
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Log.e(TAG, "Capture session configuration failed");
                    }
                },
                backgroundHandler // Use background handler
            );
            
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to create capture session", e);
        }
    }

    public void stopPreview() {
        Log.d(TAG, "Stopping camera preview");
        
        if (captureSession != null) {
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
        stopBackgroundThread();
        Log.d(TAG, "Camera preview stopped");
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
        
        // Choose smaller size for Samsung stability
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
        return previewSize;
    }
}
