package com.feri.redmedalertandroidapp.health;

import android.content.Context;
import android.content.Intent;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.health.connect.client.PermissionController;

import java.util.Set;

public class HealthConnectPermissionContract extends ActivityResultContract<Set<String>, Set<String>> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Set<String> permissions) {
        return PermissionController.createRequestPermissionResultContract().createIntent(context, permissions);
    }

    @Override
    public Set<String> parseResult(int resultCode, @Nullable Intent intent) {
        return PermissionController.createRequestPermissionResultContract().parseResult(resultCode, intent);
    }
}
