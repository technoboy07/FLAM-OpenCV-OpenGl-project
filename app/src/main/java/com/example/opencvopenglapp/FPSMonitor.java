package com.example.opencvopenglapp;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.atomic.AtomicLong;

public class FPSMonitor {
    private static final String TAG = "FPSMonitor";
    
    private final AtomicLong frameCount = new AtomicLong(0);
    private final AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final FPSCallback callback;
    
    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long frames = frameCount.get();
            long timeDiff = currentTime - lastTime.get();
            
            if (timeDiff > 0) {
                double fps = (frames * 1000.0) / timeDiff;
                callback.onFPSUpdate(fps);
            }
            
            // Reset counters
            frameCount.set(0);
            lastTime.set(currentTime);
            
            // Schedule next update
            mainHandler.postDelayed(this, 1000); // Update every second
        }
    };

    public interface FPSCallback {
        void onFPSUpdate(double fps);
    }

    public FPSMonitor(FPSCallback callback) {
        this.callback = callback;
        mainHandler.post(updateTask);
    }

    public void recordFrame() {
        frameCount.incrementAndGet();
    }

    public void stop() {
        mainHandler.removeCallbacks(updateTask);
    }
}
