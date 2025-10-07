package com.example.opencvopenglapp;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "OpenGLRenderer";
    
    // Shader code
    private static final String VERTEX_SHADER_CODE =
            "attribute vec4 vPosition;" +
            "attribute vec2 vTexCoord;" +
            "varying vec2 texCoord;" +
            "uniform mat4 uMVPMatrix;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  texCoord = vTexCoord;" +
            "}";

    private static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
            "varying vec2 texCoord;" +
            "uniform sampler2D uTexture;" +
            "void main() {" +
            "  gl_FragColor = texture2D(uTexture, texCoord);" +
            "}";

    // Quad vertices (x, y, z, u, v)
    private static final float[] QUAD_VERTICES = {
            -1.0f, -1.0f, 0.0f, 0.0f, 1.0f,  // Bottom left
             1.0f, -1.0f, 0.0f, 1.0f, 1.0f,  // Bottom right
            -1.0f,  1.0f, 0.0f, 0.0f, 0.0f,  // Top left
             1.0f,  1.0f, 0.0f, 1.0f, 0.0f   // Top right
    };

    private FloatBuffer vertexBuffer;
    private int shaderProgram;
    private int positionHandle;
    private int texCoordHandle;
    private int mvpMatrixHandle;
    private int textureHandle;
    
    private float[] mvpMatrix = new float[16];
    private int[] textures = new int[1];
    private int frameWidth = 0;
    private int frameHeight = 0;
    private boolean textureInitialized = false;

    public OpenGLRenderer() {
        // Initialize vertex buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(QUAD_VERTICES.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(QUAD_VERTICES);
        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "Surface created");
        
        // Set background color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        // Enable blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        
        // Create shader program
        shaderProgram = createShaderProgram();
        
        // Get handles
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "vTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        textureHandle = GLES20.glGetUniformLocation(shaderProgram, "uTexture");
        
        // Initialize MVP matrix
        Matrix.setIdentityM(mvpMatrix, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "Surface changed: " + width + "x" + height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        
        if (!textureInitialized) {
            return;
        }
        
        // Use shader program
        GLES20.glUseProgram(shaderProgram);
        
        // Set vertex attributes
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);
        
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 5 * 4, 
                                    vertexBuffer.position(3));
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        
        // Set uniforms
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniform1i(textureHandle, 0);
        
        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        
        // Draw quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        
        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

    public void updateTexture(int[] pixelData, int width, int height) {
        if (width <= 0 || height <= 0 || pixelData == null) {
            return;
        }
        
        // Initialize texture if needed
        if (!textureInitialized || frameWidth != width || frameHeight != height) {
            initializeTexture(width, height);
            frameWidth = width;
            frameHeight = height;
        }
        
        // Update texture data
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height, 
                              GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 
                              IntBuffer.wrap(pixelData));
    }

    private void initializeTexture(int width, int height) {
        // Generate texture
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        
        // Set texture parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        
        // Allocate texture storage
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, 
                           GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        
        textureInitialized = true;
        Log.d(TAG, "Texture initialized: " + width + "x" + height);
    }

    private int createShaderProgram() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);
        
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        
        // Check linking status
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(program);
            Log.e(TAG, "Shader program linking failed: " + error);
            GLES20.glDeleteProgram(program);
            return 0;
        }
        
        return program;
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        
        // Check compilation status
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String error = GLES20.glGetShaderInfoLog(shader);
            Log.e(TAG, "Shader compilation failed: " + error);
            GLES20.glDeleteShader(shader);
            return 0;
        }
        
        return shader;
    }
}
