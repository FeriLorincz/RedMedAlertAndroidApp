package com.feri.redmedalertandroidapp.network;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class NetworkStateMonitorTest {


    private Context context;
    private TestNetworkCallback testCallback;
    private NetworkStateMonitor networkStateMonitor;


    // Implementare concretă pentru teste
    private static class TestNetworkCallback implements NetworkStateCallback {
        private boolean lastKnownState;
        private int callCount = 0;


        @Override
        public void onNetworkStateChanged(boolean isConnected) {
            this.lastKnownState = isConnected;
            callCount++;
        }


        public boolean getLastKnownState() {
            return lastKnownState;
        }


        public int getCallCount() {
            return callCount;
        }


        public void reset() {
            callCount = 0;
            lastKnownState = false;
        }
    }


    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testCallback = new TestNetworkCallback();
        networkStateMonitor = new TestableNetworkStateMonitor(context);
    }


    private class TestableNetworkStateMonitor extends NetworkStateMonitor {
        public TestableNetworkStateMonitor(Context context) {
            super(context);
        }


        @Override
        public void startMonitoring(NetworkStateCallback callback) {
            // Doar setăm callback-ul fără a înregistra network callback-ul real
            this.callback = callback;
        }


        @Override
        public void stopMonitoring() {
            // Setăm callback-ul la null când oprim monitorizarea
            this.callback = null;
        }


        public void simulateNetworkAvailable() {
            notifyNetworkState(true);
        }


        public void simulateNetworkLost() {
            notifyNetworkState(false);
        }
    }


    @Test
    public void startMonitoring_shouldStartMonitoring() {
        // Arrange
        testCallback.reset();
        // Act
        networkStateMonitor.startMonitoring(testCallback);
        ((TestableNetworkStateMonitor) networkStateMonitor).simulateNetworkAvailable();
        // Assert
        assertEquals("Callback should have been called once", 1, testCallback.getCallCount());
        assertTrue("Network should be reported as available", testCallback.getLastKnownState());
    }


    @Test
    public void stopMonitoring_shouldStopMonitoring() {
        // Arrange
        networkStateMonitor.startMonitoring(testCallback);
        testCallback.reset();
        // Act
        networkStateMonitor.stopMonitoring();
        ((TestableNetworkStateMonitor) networkStateMonitor).simulateNetworkAvailable();
        // Assert
        assertEquals("Callback should not have been called after stopping", 0, testCallback.getCallCount());
    }


    @Test
    public void networkCallback_shouldNotifyOnNetworkAvailable() {
        // Arrange
        networkStateMonitor.startMonitoring(testCallback);
        testCallback.reset();  // Reset după startMonitoring
        // Act
        ((TestableNetworkStateMonitor) networkStateMonitor).simulateNetworkAvailable();
        // Assert
        assertTrue("Network should be reported as available", testCallback.getLastKnownState());
        assertEquals("Callback should have been called once", 1, testCallback.getCallCount());
    }


    @Test
    public void networkCallback_shouldNotifyOnNetworkLost() {
        // Arrange
        networkStateMonitor.startMonitoring(testCallback);
        ((TestableNetworkStateMonitor) networkStateMonitor).simulateNetworkAvailable();
        testCallback.reset();
        // Act
        ((TestableNetworkStateMonitor) networkStateMonitor).simulateNetworkLost();
        // Assert
        assertFalse("Network should be reported as lost", testCallback.getLastKnownState());
        assertEquals("Callback should have been called once", 1, testCallback.getCallCount());
    }
}