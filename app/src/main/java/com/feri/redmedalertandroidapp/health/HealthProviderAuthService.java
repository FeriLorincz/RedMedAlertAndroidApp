package com.feri.redmedalertandroidapp.health;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class HealthProviderAuthService extends Service {
    private static final String TAG = "HealthProviderAuthSvc";

    private final IBinder mBinder = new HealthProviderAuthServiceBinder();

    public HealthProviderAuthService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() - returning authorization service");
        return mBinder;
    }

    public class HealthProviderAuthServiceBinder extends Binder {
        public HealthProviderAuthService getService() {
            return HealthProviderAuthService.this;
        }
    }
}