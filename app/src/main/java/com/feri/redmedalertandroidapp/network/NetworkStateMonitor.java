package com.feri.redmedalertandroidapp.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import timber.log.Timber;

public class NetworkStateMonitor {

    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback;
    private NetworkStateCallback callback;
    private boolean isRegistered = false;
    private boolean lastKnownState = false;

    public interface NetworkStateCallback {
        void onNetworkStateChanged(boolean isConnected);
    }

    public NetworkStateMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                Timber.d("Network became available");
                updateAndNotifyNetworkState(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                Timber.d("Network connection lost");
                // Ensure immediate notification
                lastKnownState = true; // Reset state to ensure notification
                updateAndNotifyNetworkState(false);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities capabilities) {
                boolean isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                Timber.d("Network capabilities changed - Validated: %s, Internet: %s", isValidated, hasInternet);
                updateAndNotifyNetworkState(isValidated && hasInternet);
            }
        };
    }

    private synchronized void updateAndNotifyNetworkState(boolean newState) {
        lastKnownState = newState;
        notifyNetworkState(newState);
        Timber.d("Network state updated: %s", newState);
    }

    public void startMonitoring(NetworkStateCallback callback) {
        if (isRegistered) {
            return;
        }

        this.callback = callback;
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback);
            isRegistered = true;

            // Notify initial state
            boolean isInitiallyConnected = isNetworkAvailable();
            updateAndNotifyNetworkState(isInitiallyConnected);

            Timber.d("Network monitoring started. Initial state: %s", isInitiallyConnected);
        } catch (Exception e) {
            Timber.e(e, "Error starting network monitoring");
        }
    }

    public void stopMonitoring() {
        if (!isRegistered) {
            return;
        }

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            isRegistered = false;
            callback = null;
            Timber.d("Network monitoring stopped");
        } catch (Exception e) {
            Timber.e(e, "Error stopping network monitoring");
        }
    }

    public boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (capabilities == null) return false;

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    private void notifyNetworkState(boolean isConnected) {
        if (callback != null) {
            callback.onNetworkStateChanged(isConnected);
        }
    }

    // Add this method for testing purposes
    @VisibleForTesting
    protected void forceNetworkStateUpdate(boolean state) {
        updateAndNotifyNetworkState(state);
    }
}