package com.feri.redmedalertandroidapp.data.sync;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.data.upload.DatabaseUploader;
import com.feri.redmedalertandroidapp.network.NetworkStateMonitor;
import com.feri.redmedalertandroidapp.util.RetryHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class SyncManagerTest {

    @Mock private Context mockContext;
    @Mock private DataRepository mockDataRepository;
    @Mock private DatabaseUploader mockDatabaseUploader;
    @Mock private NetworkStateMonitor mockNetworkMonitor;
    @Mock private RetryHandler mockRetryHandler;
    @Mock private Future<List<SensorDataEntity>> mockFuture;

    private SyncManager syncManager;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mockNetworkMonitor.isNetworkAvailable()).thenReturn(true);
        when(mockDataRepository.getUnsyncedData()).thenReturn(mockFuture);
        when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(Arrays.asList(
                createTestSensorData("test-device")
        ));

        // Configure RetryHandler
        doAnswer(invocation -> {
            Callable<?> callable = invocation.getArgument(0);
            callable.call(); // Execute the task immediately
            return true; // Return success
        }).when(mockRetryHandler).executeWithRetry(any());

        syncManager = new TestSyncManager();
    }

    private class TestSyncManager extends SyncManager {  // făcut static
        TestSyncManager() {
            super(mockContext, mockDataRepository);
        }

        @Override
        protected NetworkStateMonitor createNetworkMonitor() {
            return mockNetworkMonitor;
        }

        @Override
        protected DatabaseUploader createDatabaseUploader() {
            return mockDatabaseUploader;
        }

        @Override
        protected RetryHandler createRetryHandler() {
            return mockRetryHandler;
        }

        @Override
        protected void initializeNetworkMonitoring() {
            // Skip network monitoring initialization in tests
        }
    }

    @Test
    public void startSync_whenNetworkAvailable_shouldSyncData() throws Exception {
        // Arrange
        doNothing().when(mockDatabaseUploader).uploadPendingData();

        // Act
        syncManager.startSync();

        // Assert with increased timeout and polling interval
        verify(mockDataRepository, timeout(3000).times(1)).getUnsyncedData();
        verify(mockDatabaseUploader, timeout(3000).times(1)).uploadPendingData();
    }

    @Test
    public void startSync_whenNetworkUnavailable_shouldNotSync() throws Exception {
        // Arrange
        when(mockNetworkMonitor.isNetworkAvailable()).thenReturn(false);

        // Act
        syncManager.startSync();
        Thread.sleep(1000);

        // Assert
        verify(mockDataRepository, never()).getUnsyncedData();
    }

    @Test
    public void startSync_whenNoUnsyncedData_shouldNotUpload() throws Exception {
        // Arrange
        when(mockFuture.get(anyLong(), any())).thenReturn(Arrays.asList());

        // Act
        syncManager.startSync();
        Thread.sleep(1000);

        // Assert
        verify(mockDataRepository).getUnsyncedData();
        verify(mockDatabaseUploader, never()).uploadPendingData();
    }

    @Test
    public void startSync_whenUploadFails_shouldRetry() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Upload failed"))
                .when(mockDatabaseUploader)
                .uploadPendingData();

        // Act
        syncManager.startSync();
        Thread.sleep(1000);

        // Assert
        verify(mockRetryHandler).executeWithRetry(any());
    }

    private SensorDataEntity createTestSensorData(String deviceId) {
        return new SensorDataEntity(
                deviceId,
                "user1",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );
    }
}