#ifndef FRAME_PROCESSOR_H
#define FRAME_PROCESSOR_H

#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>

enum ProcessingMode {
    MODE_GRAYSCALE = 0,
    MODE_CANNY_EDGE = 1,
    MODE_BLUR = 2,
    MODE_ORIGINAL = 3
};

class FrameProcessor {
public:
    FrameProcessor();
    ~FrameProcessor();
    
    bool processFrame(const cv::Mat& input, cv::Mat& output);
    void setProcessingMode(ProcessingMode mode);
    
private:
    ProcessingMode currentMode;
    cv::Mat tempMat1, tempMat2;
    
    void applyGrayscale(const cv::Mat& input, cv::Mat& output);
    void applyCannyEdge(const cv::Mat& input, cv::Mat& output);
    void applyBlur(const cv::Mat& input, cv::Mat& output);
};

#endif // FRAME_PROCESSOR_H
