package com.example.opencvopenglapp;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class WebSocketClient extends WebSocketListener {
    private static final String TAG = "WebSocketClient";
    private static final String DEFAULT_URL = "ws://192.168.29.82:8080/ws";
    
    private WebSocket webSocket;
    private OkHttpClient client;
    private String serverUrl;
    private boolean isConnected = false;
    private long lastFrameTime = 0;
    private int frameCount = 0;
    private float currentFPS = 0.0f;
    private int processingMode = 0;
    private long processingTime = 0;
    
    // Callbacks
    public interface WebSocketCallback {
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }
    
    private WebSocketCallback callback;
    
    public WebSocketClient() {
        this(DEFAULT_URL);
    }
    
    public WebSocketClient(String url) {
        this.serverUrl = url;
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }
    
    public void setCallback(WebSocketCallback callback) {
        this.callback = callback;
    }
    
    public void connect() {
        if (isConnected) {
            Log.w(TAG, "Already connected");
            return;
        }
        
        Log.d(TAG, "Connecting to: " + serverUrl);
        Request request = new Request.Builder()
                .url(serverUrl)
                .build();
        
        webSocket = client.newWebSocket(request, this);
    }
    
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Disconnecting");
            webSocket = null;
        }
        isConnected = false;
    }
    
    public void sendFrameData(int width, int height, float fps, int mode, long procTime) {
        if (!isConnected || webSocket == null) {
            return;
        }
        
        try {
            JSONObject frameData = new JSONObject();
            frameData.put("type", "frame");
            frameData.put("timestamp", System.currentTimeMillis());
            frameData.put("width", width);
            frameData.put("height", height);
            frameData.put("fps", fps);
            frameData.put("processingMode", mode);
            frameData.put("processingTime", procTime);
            
            webSocket.send(frameData.toString());
            
            // Update stats
            frameCount++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameTime >= 1000) { // Update FPS every second
                currentFPS = (frameCount * 1000.0f) / (currentTime - lastFrameTime);
                frameCount = 0;
                lastFrameTime = currentTime;
                
                // Send stats
                sendStats();
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating frame data JSON", e);
        }
    }
    
    private void sendStats() {
        if (!isConnected || webSocket == null) {
            return;
        }
        
        try {
            JSONObject stats = new JSONObject();
            stats.put("type", "stats");
            stats.put("averageFPS", currentFPS);
            stats.put("maxFPS", Math.min(currentFPS * 1.2f, 30.0f));
            stats.put("minFPS", Math.max(currentFPS * 0.8f, 5.0f));
            stats.put("averageProcessingTime", processingTime);
            stats.put("totalFrames", frameCount);
            stats.put("uptime", System.currentTimeMillis() - lastFrameTime);
            
            webSocket.send(stats.toString());
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating stats JSON", e);
        }
    }
    
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.d(TAG, "WebSocket connected");
        isConnected = true;
        if (callback != null) {
            callback.onConnected();
        }
    }
    
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "Received message: " + text);
        // Handle incoming messages if needed
    }
    
    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Log.d(TAG, "Received binary message: " + bytes.size() + " bytes");
    }
    
    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "WebSocket closing: " + code + " - " + reason);
        isConnected = false;
    }
    
    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "WebSocket closed: " + code + " - " + reason);
        isConnected = false;
        if (callback != null) {
            callback.onDisconnected();
        }
    }
    
    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(TAG, "WebSocket error", t);
        isConnected = false;
        if (callback != null) {
            callback.onError(t.getMessage());
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public void setProcessingMode(int mode) {
        this.processingMode = mode;
    }
    
    public void setProcessingTime(long time) {
        this.processingTime = time;
    }
}
