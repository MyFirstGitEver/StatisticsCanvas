package com.example.statisticscanvas2;

import android.os.Handler;
import android.os.Looper;

public class MyTimer {
    private Runnable r, r2;
    private final Handler handler;
    private long interval;
    private long progress = -1;
    private boolean finiteTriggered;

    public MyTimer(OnTickListener listener) {
        handler = new Handler(Looper.getMainLooper());
        r = () ->
        {
            listener.onTick();
            handler.removeCallbacks(r);
        };

        r2 = () ->
        {
            progress += interval;
            listener.onTick();
            handler.postDelayed(r2, interval);
        };
    }

    public void start_infinite(long interval) {
        this.interval = interval;
        handler.postDelayed(r2, interval);
        finiteTriggered = false;
        progress = 0;
    }

    public void start(long interval) {
        handler.postDelayed(r, interval);
        finiteTriggered = true;
    }

    public void stop() {
        if (finiteTriggered) {
            handler.removeCallbacks(r);
            finiteTriggered = false;
        }
        else {
            handler.removeCallbacks(r2);
            progress = -1;
        }
    }

    public boolean isTicking(){
        return (progress != -1 || finiteTriggered);
    }

    public long getProgress() {
        return progress;
    }

    public interface OnTickListener{
        void onTick();
    }
}