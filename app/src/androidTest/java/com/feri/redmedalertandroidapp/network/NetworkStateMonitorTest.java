package com.feri.redmedalertandroidapp.network;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class NetworkStateMonitorTest {

    @Mock private Context mockContext;
    @Mock private ConnectivityManager mockConnectivityManager;
    @Mock private Network mockNetwork;
    @Mock private NetworkStateMonitor.NetworkStateCallback mockCallback;

    private NetworkStateMonitor networkStateMonitor;
    private ConnectivityManager.NetworkCallback capturedCallback;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
                .thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetwork())
                .thenReturn(mockNetwork);

        networkStateMonitor = new NetworkStateMonitor(mockContext);
    }
    @Test
    public void startMonitoring_shouldRegisterCallback() {
        // Act
        networkStateMonitor.startMonitoring(mockCallback);

        // Assert
        verify(mockConnectivityManager).registerNetworkCallback(
                any(NetworkRequest.class),
                any(ConnectivityManager.NetworkCallback.class)
        );
    }

    @Test
    public void stopMonitoring_shouldUnregisterCallback() {
        // Arrange
        networkStateMonitor.startMonitoring(mockCallback);

        // Act
        networkStateMonitor.stopMonitoring();

        // Assert
        verify(mockConnectivityManager).unregisterNetworkCallback(
                any(ConnectivityManager.NetworkCallback.class)
        );
    }

    @Test
    public void networkCallback_shouldNotifyOnNetworkAvailable() {
        // Arrange
        ArgumentCaptor<ConnectivityManager.NetworkCallback> callbackCaptor =
                ArgumentCaptor.forClass(ConnectivityManager.NetworkCallback.class);

        networkStateMonitor.startMonitoring(mockCallback);
        verify(mockConnectivityManager).registerNetworkCallback(
                any(NetworkRequest.class),
                callbackCaptor.capture()
        );

        ConnectivityManager.NetworkCallback callback = callbackCaptor.getValue();

        // Act
        callback.onAvailable(mockNetwork);

        // Assert
        verify(mockCallback).onNetworkStateChanged(true);
    }

    @Test
    public void networkCallback_shouldNotifyOnNetworkLost() {
        // Arrange
        ArgumentCaptor<ConnectivityManager.NetworkCallback> callbackCaptor =
                ArgumentCaptor.forClass(ConnectivityManager.NetworkCallback.class);

        networkStateMonitor.startMonitoring(mockCallback);
        verify(mockConnectivityManager).registerNetworkCallback(
                any(NetworkRequest.class),
                callbackCaptor.capture()
        );

        ConnectivityManager.NetworkCallback callback = callbackCaptor.getValue();

        // Act
        // First simulate a network available state
        callback.onAvailable(mockNetwork);
        // Then simulate network loss
        callback.onLost(mockNetwork);

        // Assert with timeout to handle async callbacks
        verify(mockCallback, timeout(1000).atLeastOnce()).onNetworkStateChanged(false);
    }
}