#!/bin/bash

# OpenCV OpenGL Android + Web Project Build Script
# This script builds both the Android app and web viewer

set -e  # Exit on any error

echo "🚀 Building OpenCV OpenGL Project..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "build.gradle" ]; then
    print_error "Please run this script from the project root directory"
    exit 1
fi

# Check for OpenCV Android SDK
if [ ! -d "OpenCV-android-sdk" ]; then
    print_warning "OpenCV Android SDK not found!"
    print_status "Please download OpenCV Android SDK and extract it to 'OpenCV-android-sdk/' directory"
    print_status "Download from: https://opencv.org/releases/"
    print_status "Example:"
    print_status "  wget https://github.com/opencv/opencv/releases/download/4.8.0/opencv-4.8.0-android-sdk.zip"
    print_status "  unzip opencv-4.8.0-android-sdk.zip"
    print_status "  mv OpenCV-android-sdk opencv-4.8.0-android-sdk/"
    exit 1
fi

print_success "OpenCV Android SDK found"

# Build Android app
print_status "Building Android application..."

# Check if gradlew exists, if not create it
if [ ! -f "gradlew" ]; then
    print_status "Creating Gradle wrapper..."
    gradle wrapper
fi

# Make gradlew executable
chmod +x gradlew

# Clean and build
print_status "Cleaning project..."
./gradlew clean

print_status "Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    print_success "Android app built successfully!"
    print_status "APK location: app/build/outputs/apk/debug/app-debug.apk"
else
    print_error "Android build failed!"
    exit 1
fi

# Build web viewer
print_status "Building web viewer..."

cd web

# Check if package.json exists
if [ ! -f "package.json" ]; then
    print_error "package.json not found in web directory"
    exit 1
fi

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    print_status "Installing web dependencies..."
    npm install
fi

# Build TypeScript
print_status "Compiling TypeScript..."
npm run build

if [ $? -eq 0 ]; then
    print_success "Web viewer built successfully!"
    print_status "Web files location: web/dist/"
else
    print_error "Web build failed!"
    exit 1
fi

cd ..

# Create deployment package
print_status "Creating deployment package..."

DEPLOY_DIR="deployment"
mkdir -p $DEPLOY_DIR

# Copy Android APK
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    cp app/build/outputs/apk/debug/app-debug.apk $DEPLOY_DIR/
    print_success "Android APK copied to deployment/"
fi

# Copy web files
if [ -d "web/dist" ]; then
    cp -r web/dist $DEPLOY_DIR/web-dist
    cp web/index.html $DEPLOY_DIR/
    print_success "Web files copied to deployment/"
fi

# Copy documentation
cp README.md $DEPLOY_DIR/
print_success "Documentation copied to deployment/"

print_success "🎉 Build completed successfully!"
print_status ""
print_status "📱 Android App:"
print_status "  - APK: deployment/app-debug.apk"
print_status "  - Install: adb install deployment/app-debug.apk"
print_status ""
print_status "🌐 Web Viewer:"
print_status "  - Files: deployment/web-dist/ and deployment/index.html"
print_status "  - Test server: python3 web/server.py"
print_status "  - Open: http://localhost:8081"
print_status ""
print_status "📚 Documentation: deployment/README.md"
print_status ""
print_status "Next steps:"
print_status "1. Install the Android APK on your device"
print_status "2. Run the web server: python3 web/server.py"
print_status "3. Open http://localhost:8081 in your browser"
print_status "4. Test the real-time visualization!"
