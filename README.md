# Screenshots

Below are screenshots demonstrating the working module of the Real-Time Edge Detection Viewer:

## Web Interface
![Web Interface Screenshot](deployment/web-screenshot-1.jpg)

## Android App Interface
![Android App Screenshot](deployment/android-screenshot-1.jpg)

## Original Camera Frame
![Original Frame Screenshot](deployment/original-frame-1.jpg)

These images showcase the real-time processing and visualization features of the application across both the web and Android platforms.
# Android + OpenCV-C++ + OpenGL + TypeScript Web Viewer

A comprehensive Android application that demonstrates real-time camera frame processing using OpenCV (C++) through JNI, OpenGL ES rendering, and a TypeScript-based web viewer for visualization.

##  Features

- **Real-time Camera Processing**: Live camera feed capture using Camera2 API
- **OpenCV C++ Processing**: Native C++ image processing with JNI bridge
- **OpenGL ES Rendering**: Hardware-accelerated rendering with 15+ FPS performance
- **Multiple Processing Modes**: Grayscale, Canny Edge Detection, Blur, and Original
- **TypeScript Web Viewer**: Real-time visualization with performance monitoring
- **Performance Monitoring**: FPS tracking and processing time analysis

##  Architecture

```
├── app/                          # Android Application
│   ├── src/main/java/           # Java/Kotlin code
│   ├── src/main/cpp/            # C++ OpenCV processing
│   └── src/main/res/            # Android resources
├── web/                         # TypeScript Web Viewer
│   ├── src/                     # TypeScript source
│   └── dist/                    # Compiled JavaScript
└── README.md                    # This file
```

##  Prerequisites

### Android Development
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0)
- NDK 25.0.8775105 or later
- OpenCV Android SDK 4.8.0+

### Web Development
- Node.js 18+ and npm
- TypeScript 5.0+

##  Setup Instructions

### 1. Android Project Setup

1. **Clone and Open Project**
   ```bash
   git clone <repository-url>
   cd android-opencv-opengl-project
   ```

2. **Download OpenCV Android SDK**
   - Download OpenCV Android SDK from [OpenCV Releases](https://opencv.org/releases/)
   - Extract to project root as `OpenCV-android-sdk/`

3. **Configure NDK**
   - Open Android Studio
   - Go to File → Project Structure → SDK Location
   - Set NDK location (usually `~/Android/Sdk/ndk/25.0.8775105`)

4. **Build Project**
   ```bash
   ./gradlew assembleDebug
   ```

### 2. Web Viewer Setup

1. **Install Dependencies**
   ```bash
   cd web
   npm install
   ```

2. **Build TypeScript**
   ```bash
   npm run build
   ```

3. **Start Web Server**
   ```bash
   npm run serve
   ```

4. **Open in Browser**
   - Navigate to `http://localhost:8080`
   - The web viewer will attempt to connect to the Android app via WebSocket

##  Core Components

### Android App Components

#### 1. Camera Integration (`CameraHandler.java`)
- Camera2 API implementation
- Automatic camera selection and configuration
- Optimal preview size selection
- Real-time frame capture

#### 2. OpenCV Processing (`OpenCVProcessor.java` + C++)
- JNI bridge for Java ↔ C++ communication
- Multiple processing modes:
  - **Grayscale**: Color to grayscale conversion
  - **Canny Edge**: Edge detection with noise reduction
  - **Blur**: Gaussian blur filtering
  - **Original**: No processing
- Memory-efficient frame processing

#### 3. OpenGL Rendering (`OpenGLRenderer.java`)
- OpenGL ES 2.0 shader-based rendering
- Texture-based frame display
- Hardware-accelerated processing
- 15+ FPS performance target

#### 4. Main Activity (`MainActivity.java`)
- UI controls for mode switching
- Processing toggle functionality
- FPS monitoring and display
- Permission handling

### Web Viewer Components

#### 1. Frame Viewer (`FrameViewer.ts`)
- Real-time frame visualization
- Performance overlay display
- Processing mode indicators
- FPS and resolution display

#### 2. Performance Chart (`PerformanceChart.ts`)
- Real-time performance graphing
- FPS and processing time visualization
- Historical data tracking

#### 3. WebSocket Client (`WebSocketClient.ts`)
- Real-time communication with Android app
- Automatic reconnection handling
- Error handling and status reporting

## 🔧 Configuration

### Android Configuration

#### CMakeLists.txt
```cmake
# OpenCV configuration
set(OpenCV_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../OpenCV-android-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)

# Native library
add_library(opencv_processor SHARED opencv_processor.cpp frame_processor.cpp)
target_link_libraries(opencv_processor ${OpenCV_LIBS} android log GLESv2 EGL)
```

#### build.gradle (app level)
```gradle
android {
    externalNativeBuild {
        cmake {
            path file('src/main/cpp/CMakeLists.txt')
            version '3.22.1'
        }
    }
}
```

### Web Configuration

#### tsconfig.json
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ES2020",
    "outDir": "./dist",
    "strict": true
  }
}
```

##  Performance Targets

- **FPS**: 15+ frames per second
- **Processing Time**: < 50ms per frame
- **Memory Usage**: < 100MB
- **Resolution**: Up to 1920x1080

##  Testing

### Android Testing
1. **Unit Tests**: JNI function testing
2. **Integration Tests**: Camera → OpenCV → OpenGL pipeline
3. **Performance Tests**: FPS and memory usage monitoring

### Web Testing
1. **Connection Tests**: WebSocket connectivity
2. **Visualization Tests**: Frame rendering accuracy
3. **Performance Tests**: Chart rendering performance

##  Deployment

### Android App
1. **Debug Build**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Release Build**
   ```bash
   ./gradlew assembleRelease
   ```

### Web Viewer
1. **Build**
   ```bash
   cd web && npm run build
   ```

2. **Deploy**
   - Copy `web/dist/` and `web/index.html` to web server
   - Configure WebSocket endpoint

## 🔍 Troubleshooting

### Common Issues

1. **OpenCV Not Found**
   - Ensure OpenCV Android SDK is extracted to project root
   - Check CMakeLists.txt OpenCV_DIR path

2. **NDK Build Errors**
   - Verify NDK version compatibility
   - Check C++ standard (C++17 required)

3. **WebSocket Connection Failed**
   - Ensure Android app is running
   - Check WebSocket URL configuration
   - Verify network connectivity

4. **Low FPS Performance**
   - Reduce camera resolution
   - Optimize OpenCV processing
   - Check device capabilities

##  Performance Optimization

### Android Optimizations
- Use OpenGL ES 2.0 for hardware acceleration
- Optimize OpenCV operations for mobile
- Implement frame skipping for heavy processing
- Use native memory management

### Web Optimizations
- Implement efficient WebSocket message handling
- Use requestAnimationFrame for smooth rendering
- Optimize canvas operations
- Implement data compression for large frames

##  Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

##  License

This project is licensed under the MIT License - see the LICENSE file for details.

##  Learning Outcomes

This project demonstrates:
- **Native Android Development**: Camera APIs, OpenGL ES, JNI
- **Computer Vision**: OpenCV integration and image processing
- **Web Development**: TypeScript, WebSocket communication
- **Performance Optimization**: Real-time processing techniques
- **Cross-Platform Integration**: Android ↔ Web communication

##  Support

For questions or issues:
- Create an issue in the repository
- Check the troubleshooting section
- Review the documentation

---

**Note**: This is a comprehensive assessment project demonstrating advanced Android development with computer vision, graphics programming, and web integration techniques.
