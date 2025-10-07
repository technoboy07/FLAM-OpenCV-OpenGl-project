cmake_minimum_required(VERSION 3.22.1)

project("opencvopenglapp")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)

# Add OpenCV
set(OpenCV_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../OpenCV-android-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)

# Include directories
include_directories(${OpenCV_INCLUDE_DIRS})

# Add the native library
add_library(
    opencv_processor
    SHARED
    opencv_processor.cpp
    frame_processor.cpp
)

# Link libraries
target_link_libraries(
    opencv_processor
    ${OpenCV_LIBS}
    android
    log
    GLESv2
    EGL
)
