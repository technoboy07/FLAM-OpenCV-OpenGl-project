package com.example.opencvopenglapp;

public class OpenCVProcessor {
    static {
        System.loadLibrary("opencv_processor");
    }

    private long nativeProcessorPtr;

    public OpenCVProcessor() {
        nativeProcessorPtr = nativeCreateProcessor();
    }

    public void destroy() {
        if (nativeProcessorPtr != 0) {
            nativeDestroyProcessor(nativeProcessorPtr);
            nativeProcessorPtr = 0;
        }
    }

    public int[] processFrame(int[] inputData, int width, int height) {
        if (nativeProcessorPtr == 0) {
            return null;
        }
        return nativeProcessFrame(nativeProcessorPtr, inputData, width, height);
    }

    public void setProcessingMode(int mode) {
        if (nativeProcessorPtr != 0) {
            nativeSetProcessingMode(nativeProcessorPtr, mode);
        }
    }

    // Native method declarations
    private native long nativeCreateProcessor();
    private native void nativeDestroyProcessor(long processorPtr);
    private native int[] nativeProcessFrame(long processorPtr, int[] inputData, int width, int height);
    private native void nativeSetProcessingMode(long processorPtr, int mode);
}
