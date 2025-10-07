package com.example.opencvopenglapp;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

public class CameraGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "CameraGLSurfaceView";
    
    private OpenGLRenderer renderer;
    private OpenCVProcessor openCVProcessor;
    private CameraHandler cameraHandler;
    private boolean isProcessingEnabled = true;
    private int processingMode = 0; // 0 = grayscale, 1 = canny, 2 = blur, 3 = original

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

    public void setCameraHandler(CameraHandler handler) {
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

    public void onFrameAvailable(int[] pixelData, int width, int height) {
        if (!isProcessingEnabled) {
            // Render original frame
            renderer.updateTexture(pixelData, width, height);
            return;
        }

        // Process frame with OpenCV
        int[] processedData = openCVProcessor.processFrame(pixelData, width, height);
        if (processedData != null) {
            renderer.updateTexture(processedData, width, height);
        } else {
            // Fallback to original frame if processing fails
            renderer.updateTexture(pixelData, width, height);
        }
    }

    public void cleanup() {
        if (openCVProcessor != null) {
            openCVProcessor.destroy();
            openCVProcessor = null;
        }
    }
    
    public SurfaceTexture getSurfaceTexture() {
        // GLSurfaceView doesn't have getSurfaceTexture(), so we need to create one
        // This is a simplified approach - in a real implementation, you'd manage this properly
        return null;
    }
}
