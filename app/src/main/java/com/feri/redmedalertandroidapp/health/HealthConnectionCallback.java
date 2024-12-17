package com.feri.redmedalertandroidapp.health;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;

public interface HealthConnectionCallback {

    void onConnected();
    void onConnectionFailed(HealthConnectionErrorResult error); // Actualizăm tipul parametrului
    void onDisconnected();
    void onPermissionsGranted();
    void onPermissionsDenied();
}
