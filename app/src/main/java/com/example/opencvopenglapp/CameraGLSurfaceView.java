package com.example.opencvopenglapp;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class CameraGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "CameraGLSurfaceView";
    
    private OpenGLRenderer renderer;
    private OpenCVProcessor openCVProcessor;
    private SimpleCameraHandler cameraHandler;
    private boolean isProcessingEnabled = true;
    private int processingMode = 0; // 0 = grayscale, 1 = canny, 2 = blur, 3 = original
    private SurfaceTexture surfaceTexture;
    private int cameraTextureId = -1;

    public CameraGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        
        renderer = new OpenGLRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        
        openCVProcessor = new OpenCVProcessor();
    }

    public void setCameraHandler(SimpleCameraHandler handler) {
        this.cameraHandler = handler;
    }

    public void setProcessingEnabled(boolean enabled) {
        this.isProcessingEnabled = enabled;
    }

    public void setProcessingMode(int mode) {
        this.processingMode = mode;
        if (openCVProcessor != null) {
            openCVProcessor.setProcessingMode(mode);
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }
    
    public void createSurfaceTexture() {
        if (surfaceTexture == null) {
            // Create SurfaceTexture on OpenGL thread
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    // Create OpenGL texture for camera
                    int[] textures = new int[1];
                    GLES20.glGenTextures(1, textures, 0);
                    cameraTextureId = textures[0];
                    
                    // Configure texture
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureId);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                    
                    // Create SurfaceTexture from OpenGL texture
                    surfaceTexture = new SurfaceTexture(cameraTextureId);
                    surfaceTexture.setDefaultBufferSize(1920, 1080);
                    
                    // Set up the SurfaceTexture listener to process camera frames
                    surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                        @Override
                        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                            processCameraFrame();
                        }
                    });
                    
                    // Set the camera texture in the renderer
                    renderer.setCameraTexture(cameraTextureId);
                    
                    Log.d(TAG, "SurfaceTexture created with OpenGL texture ID: " + cameraTextureId);
                }
            });
        }
    }
    
    private void processCameraFrame() {
        // This is already called from the OpenGL thread via queueEvent in createSurfaceTexture
        try {
            // Update the SurfaceTexture with the camera frame
            surfaceTexture.updateTexImage();
            
            // Get the texture matrix for proper orientation
            float[] mtx = new float[16];
            surfaceTexture.getTransformMatrix(mtx);
            renderer.setTextureMatrix(mtx);
            
            // Enable camera texture mode
            renderer.enableCameraTexture();
            
            // Request a render to display the camera frame
            requestRender();
            
            Log.d(TAG, "Camera frame processed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing camera frame", e);
            
            // Fallback to test pattern if camera frame processing fails
            showTestPattern();
        }
    }

    // Generate a test pattern for demonstration
    public void showTestPattern() {
        // Create a simple test pattern to show the app is working
        int[] testPattern = generateTestPattern(800, 600);
        renderer.updateTexture(testPattern, 800, 600);
    }
    
    private int[] generateTestPattern(int width, int height) {
        int[] pattern = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                // Create a more visible test pattern with bright colors
                int r = 255; // Bright red
                int g = (x * 255) / width; // Green gradient
                int b = (y * 255) / height; // Blue gradient
                
                // Add some geometric patterns to make it more visible
                if ((x / 50 + y / 50) % 2 == 0) {
                    pattern[index] = 0xFFFFFFFF; // White squares
                } else {
                    pattern[index] = 0xFF000000 | (r << 16) | (g << 8) | b; // Colorful squares
                }
            }
        }
        return pattern;
    }

    public void cleanup() {
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
        if (cameraTextureId != -1) {
            GLES20.glDeleteTextures(1, new int[]{cameraTextureId}, 0);
            cameraTextureId = -1;
        }
        if (openCVProcessor != null) {
            openCVProcessor.destroy();
            openCVProcessor = null;
        }
    }
}