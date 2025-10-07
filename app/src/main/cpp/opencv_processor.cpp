#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
#include <android/log.h>
#include <vector>
#include "frame_processor.h"

#define LOG_TAG "OpenCVProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_opencvopenglapp_OpenCVProcessor_nativeCreateProcessor(JNIEnv *env, jobject thiz) {
    FrameProcessor* processor = new FrameProcessor();
    return reinterpret_cast<jlong>(processor);
}

JNIEXPORT void JNICALL
Java_com_example_opencvopenglapp_OpenCVProcessor_nativeDestroyProcessor(JNIEnv *env, jobject thiz, jlong processorPtr) {
    FrameProcessor* processor = reinterpret_cast<FrameProcessor*>(processorPtr);
    if (processor) {
        delete processor;
    }
}

JNIEXPORT jintArray JNICALL
Java_com_example_opencvopenglapp_OpenCVProcessor_nativeProcessFrame(JNIEnv *env, jobject thiz, 
                                                                    jlong processorPtr,
                                                                    jintArray inputData, 
                                                                    jint width, jint height) {
    FrameProcessor* processor = reinterpret_cast<FrameProcessor*>(processorPtr);
    if (!processor) {
        LOGE("Processor is null");
        return nullptr;
    }

    // Get input data
    jint* inputArray = env->GetIntArrayElements(inputData, nullptr);
    if (!inputArray) {
        LOGE("Failed to get input array");
        return nullptr;
    }

    // Convert to OpenCV Mat
    cv::Mat inputMat(height, width, CV_8UC4, inputArray);
    cv::Mat processedMat;

    // Process frame
    bool success = processor->processFrame(inputMat, processedMat);
    
    // Release input array
    env->ReleaseIntArrayElements(inputData, inputArray, JNI_ABORT);

    if (!success) {
        LOGE("Frame processing failed");
        return nullptr;
    }

    // Convert result back to int array
    int totalPixels = width * height;
    jintArray result = env->NewIntArray(totalPixels);
    if (!result) {
        LOGE("Failed to create result array");
        return nullptr;
    }

    jint* resultArray = env->GetIntArrayElements(result, nullptr);
    if (!resultArray) {
        LOGE("Failed to get result array");
        env->DeleteLocalRef(result);
        return nullptr;
    }

    // Copy processed data
    memcpy(resultArray, processedMat.data, totalPixels * sizeof(jint));
    env->ReleaseIntArrayElements(result, resultArray, 0);

    return result;
}

JNIEXPORT void JNICALL
Java_com_example_opencvopenglapp_OpenCVProcessor_nativeSetProcessingMode(JNIEnv *env, jobject thiz, 
                                                                         jlong processorPtr, 
                                                                         jint mode) {
    FrameProcessor* processor = reinterpret_cast<FrameProcessor*>(processorPtr);
    if (processor) {
        processor->setProcessingMode(static_cast<ProcessingMode>(mode));
    }
}

}
