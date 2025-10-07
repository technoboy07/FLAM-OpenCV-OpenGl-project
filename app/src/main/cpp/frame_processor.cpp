#include "frame_processor.h"
#include <android/log.h>

#define LOG_TAG "FrameProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

FrameProcessor::FrameProcessor() : currentMode(MODE_GRAYSCALE) {
    LOGI("FrameProcessor created");
}

FrameProcessor::~FrameProcessor() {
    LOGI("FrameProcessor destroyed");
}

bool FrameProcessor::processFrame(const cv::Mat& input, cv::Mat& output) {
    if (input.empty()) {
        return false;
    }

    try {
        switch (currentMode) {
            case MODE_GRAYSCALE:
                applyGrayscale(input, output);
                break;
            case MODE_CANNY_EDGE:
                applyCannyEdge(input, output);
                break;
            case MODE_BLUR:
                applyBlur(input, output);
                break;
            case MODE_ORIGINAL:
                input.copyTo(output);
                break;
            default:
                applyGrayscale(input, output);
                break;
        }
        return true;
    } catch (const cv::Exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "OpenCV Exception: %s", e.what());
        return false;
    }
}

void FrameProcessor::setProcessingMode(ProcessingMode mode) {
    currentMode = mode;
    LOGI("Processing mode set to: %d", mode);
}

void FrameProcessor::applyGrayscale(const cv::Mat& input, cv::Mat& output) {
    if (input.channels() == 4) {
        cv::cvtColor(input, output, cv::COLOR_BGRA2GRAY);
        cv::cvtColor(output, output, cv::COLOR_GRAY2BGRA);
    } else if (input.channels() == 3) {
        cv::cvtColor(input, output, cv::COLOR_BGR2GRAY);
        cv::cvtColor(output, output, cv::COLOR_GRAY2BGRA);
    } else {
        input.copyTo(output);
    }
}

void FrameProcessor::applyCannyEdge(const cv::Mat& input, cv::Mat& output) {
    if (input.channels() == 4) {
        cv::cvtColor(input, tempMat1, cv::COLOR_BGRA2GRAY);
    } else if (input.channels() == 3) {
        cv::cvtColor(input, tempMat1, cv::COLOR_BGR2GRAY);
    } else {
        input.copyTo(tempMat1);
    }
    
    // Apply Gaussian blur to reduce noise
    cv::GaussianBlur(tempMat1, tempMat2, cv::Size(5, 5), 1.4);
    
    // Apply Canny edge detection
    cv::Canny(tempMat2, tempMat1, 50, 150);
    
    // Convert back to BGRA
    cv::cvtColor(tempMat1, output, cv::COLOR_GRAY2BGRA);
}

void FrameProcessor::applyBlur(const cv::Mat& input, cv::Mat& output) {
    cv::GaussianBlur(input, output, cv::Size(15, 15), 0);
}
