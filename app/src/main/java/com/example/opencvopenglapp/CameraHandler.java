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
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraHandler {
    private static final String TAG = "CameraHandler";
    
    private Context context;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraGLSurfaceView glSurfaceView;
    
    private String cameraId;
    private Size previewSize;
    private boolean isPreviewRunning = false;

    public CameraHandler(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void setGLSurfaceView(CameraGLSurfaceView surfaceView) {
        this.glSurfaceView = surfaceView;
    }

    public void startPreview() {
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
                }
            }, null);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception", e);
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

            if (surfaceTexture != null) {
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
                                   captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                   captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                   
                                   session.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                                   isPreviewRunning = true;
                                   Log.d(TAG, "Camera preview started successfully - frames should now be sent to SurfaceTexture");
                               } catch (CameraAccessException e) {
                                   Log.e(TAG, "Failed to start preview", e);
                               }
                           }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Capture session configuration failed");
                        }
                    },
                    null
                );
            } else {
                Log.e(TAG, "Could not create surface texture");
                isPreviewRunning = false;
            }
            
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to create capture session", e);
            isPreviewRunning = false;
        }
    }

    public void stopPreview() {
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
        Log.d(TAG, "Preview stopped");
    }

    private String getCameraId() {
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return id;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to get camera list", e);
        }
        return null;
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = Arrays.asList(choices);
        Collections.sort(bigEnough, new CompareSizesByArea());
        
        for (Size option : bigEnough) {
            if (option.getHeight() <= height && option.getWidth() <= width) {
                return option;
            }
        }
        
        return bigEnough.get(bigEnough.size() - 1);
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
