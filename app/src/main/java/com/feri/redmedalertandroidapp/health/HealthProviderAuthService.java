package com.feri.redmedalertandroidapp.health;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class HealthProviderAuthService extends Service {
    private static final String TAG = "HealthProviderAuthSvc";

    public HealthProviderAuthService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() - returning authorization service");
        return new IHealthProviderAuthService.Stub();
    }

    private static class IHealthProviderAuthService {
        private static class Stub extends android.os.Binder {
            public Stub() {
                this.attachInterface(null, "com.samsung.android.sdk.healthdata.IHealthProviderAuthService");
            }
        }
    }
}