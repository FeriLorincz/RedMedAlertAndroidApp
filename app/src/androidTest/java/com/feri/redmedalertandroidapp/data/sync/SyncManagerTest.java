package com.feri.redmedalertandroidapp.data.sync;

<<<<<<< HEAD
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.data.upload.DatabaseUploader;
import com.feri.redmedalertandroidapp.network.NetworkStateCallback;
import com.feri.redmedalertandroidapp.network.NetworkStateMonitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;
=======
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
>>>>>>> origin/master

@RunWith(AndroidJUnit4.class)
public class SyncManagerTest {

<<<<<<< HEAD
    private Context context;
    private DataRepository repository;
    private TestNetworkMonitor testNetworkMonitor;
    private TestSyncManager syncManager;
    private static final int TIMEOUT_SECONDS = 20;

    public static class TestNetworkMonitor extends NetworkStateMonitor {
        private boolean networkAvailable = true;
        private NetworkStateCallback callback;
        public boolean forceFailUpload = false;

        public TestNetworkMonitor(Context context) {
            super(context);
        }

        @Override
        public boolean isNetworkAvailable() {
            return networkAvailable;
        }

        public void setNetworkAvailable(boolean available) {
            this.networkAvailable = available;
            if (callback != null) {
                callback.onNetworkStateChanged(available);
            }
        }

        @Override
        public void startMonitoring(NetworkStateCallback callback) {
            this.callback = callback;
        }

        @Override
        public void stopMonitoring() {
            this.callback = null;
        }
    }

    public class TestSyncManager extends SyncManager {
        public TestSyncManager(Context context, DataRepository repository) {
            super(context, repository);
=======
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
>>>>>>> origin/master
        }

        @Override
        protected void initializeNetworkMonitoring() {
            // Skip network monitoring initialization in tests
        }
<<<<<<< HEAD

        @Override
        protected NetworkStateMonitor createNetworkMonitor() {
            return testNetworkMonitor;
        }

        @Override
        protected DatabaseUploader createDatabaseUploader() {
            return new DatabaseUploader(context, repository) {
                @Override
                protected void schedulePeriodicUpload() {
                    // Skip scheduling in tests
                }

                @Override
                public boolean uploadPendingData() {
                    try {
                        List<SensorDataEntity> data = repository.getUnsyncedData()
                                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        if (!data.isEmpty()) {
                            List<Long> ids = new ArrayList<>();
                            for (SensorDataEntity entity : data) {
                                ids.add(entity.getId());
                            }
                            if (testNetworkMonitor.isNetworkAvailable() &&
                                    !testNetworkMonitor.forceFailUpload) {
                                repository.markAsSynced(ids).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                                return true;
                            } else {
                                repository.incrementUploadAttempts(ids).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                                return false;
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
            };
        }
    }

    @Before
    public void setup() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testNetworkMonitor = new TestNetworkMonitor(context);

        DataRepository.resetInstance();
        Thread.sleep(2000);

        repository = DataRepository.getInstance(context);
        syncManager = new TestSyncManager(context, repository);

        repository.clearAllData().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Thread.sleep(2000);
=======
>>>>>>> origin/master
    }

    @Test
    public void startSync_whenNetworkAvailable_shouldSyncData() throws Exception {
<<<<<<< HEAD
        // Arrange cu verificări explicite
        testNetworkMonitor.setNetworkAvailable(true);
        Thread.sleep(1000);

        SensorDataEntity testData = createTestSensorData("test-device");
        testData.setSynced(false); // Explicit setăm ca nesinconizat

        Future<Long> saveFuture = repository.saveSensorData(testData);
        Long id = saveFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue("Data should be saved successfully", id > 0);
        Thread.sleep(1000);

        // Verificăm starea inițială cu retry
        boolean initialStateVerified = false;
        for (int i = 0; i < 5 && !initialStateVerified; i++) {
            List<SensorDataEntity> initialData = repository.getUnsyncedData()
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (initialData.size() == 1) {
                initialStateVerified = true;
                break;
            }
            Thread.sleep(1000);
        }
        assertTrue("Failed to verify initial state", initialStateVerified);
=======
        // Arrange
        doNothing().when(mockDatabaseUploader).uploadPendingData();
>>>>>>> origin/master

        // Act
        syncManager.startSync();

<<<<<<< HEAD
        // Assert cu verificare robustă
        boolean synced = awaitSyncCompletion();
        assertTrue("Data should be synced", synced);
=======
        // Assert with increased timeout and polling interval
        verify(mockDataRepository, timeout(3000).times(1)).getUnsyncedData();
        verify(mockDatabaseUploader, timeout(3000).times(1)).uploadPendingData();
>>>>>>> origin/master
    }

    @Test
    public void startSync_whenNetworkUnavailable_shouldNotSync() throws Exception {
        // Arrange
<<<<<<< HEAD
        testNetworkMonitor.setNetworkAvailable(false);
        Thread.sleep(1000);

        SensorDataEntity testData = createTestSensorData("test-device");
        testData.setSynced(false);

        Future<Long> saveFuture = repository.saveSensorData(testData);
        Long id = saveFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue("Data should be saved successfully", id > 0);
        Thread.sleep(1000);

        // Verificăm starea inițială cu retry
        boolean initialStateVerified = false;
        for (int i = 0; i < 5 && !initialStateVerified; i++) {
            List<SensorDataEntity> initialData = repository.getUnsyncedData()
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (initialData.size() == 1) {
                initialStateVerified = true;
                break;
            }
            Thread.sleep(1000);
        }
        assertTrue("Failed to verify initial state", initialStateVerified);

        // Act
        syncManager.startSync();
        Thread.sleep(2000);

        // Assert
        List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertEquals("Data should remain unsynced", 1, unsyncedData.size());
=======
        when(mockNetworkMonitor.isNetworkAvailable()).thenReturn(false);

        // Act
        syncManager.startSync();
        Thread.sleep(1000);

        // Assert
        verify(mockDataRepository, never()).getUnsyncedData();
>>>>>>> origin/master
    }

    @Test
    public void startSync_whenNoUnsyncedData_shouldNotUpload() throws Exception {
<<<<<<< HEAD
        // Arrange - nu trebuie să salvăm date, vom verifica doar că nu se face upload
        repository.clearAllData().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Thread.sleep(500);

        // Act
        testNetworkMonitor.setNetworkAvailable(true);
        Thread.sleep(500);
        syncManager.startSync();
        Thread.sleep(1000);

        // Assert - verificăm că nu există date nesincronizate
        List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue("Should have no unsynced data", unsyncedData.isEmpty());
=======
        // Arrange
        when(mockFuture.get(anyLong(), any())).thenReturn(Arrays.asList());

        // Act
        syncManager.startSync();
        Thread.sleep(1000);

        // Assert
        verify(mockDataRepository).getUnsyncedData();
        verify(mockDatabaseUploader, never()).uploadPendingData();
>>>>>>> origin/master
    }

    @Test
    public void startSync_whenUploadFails_shouldRetry() throws Exception {
<<<<<<< HEAD
        testNetworkMonitor.forceFailUpload = true; // Forțăm eșecul chiar dacă rețeaua e disponibilă
        SensorDataEntity testData = createTestSensorData("test-device");
        testData.setUploadAttempts(0);
        testData.setSynced(false);

        Future<Long> saveFuture = repository.saveSensorData(testData);
        Long id = saveFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Thread.sleep(2000);

        testNetworkMonitor.setNetworkAvailable(true);
        Thread.sleep(1000);

        syncManager.startSync();
        Thread.sleep(5000);  // Crescut timpul de așteptare

        List<SensorDataEntity> finalData = repository.getUnsyncedData()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue("Should still have unsynced data", !finalData.isEmpty());
        assertTrue("Upload attempts should have increased",
                finalData.get(0).getUploadAttempts() > 0);
    }

    private boolean awaitSyncCompletion() throws Exception {
        for (int i = 0; i < 15; i++) {
            Thread.sleep(1500);
            List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (unsyncedData.isEmpty()) {
                return true;
            }
            Timber.d("Sync attempt %d: Still have %d unsynced records",
                    i + 1, unsyncedData.size());
        }
        return false;
=======
        // Arrange
        doThrow(new RuntimeException("Upload failed"))
                .when(mockDatabaseUploader)
                .uploadPendingData();

        // Act
        syncManager.startSync();
        Thread.sleep(1000);

        // Assert
        verify(mockRetryHandler).executeWithRetry(any());
>>>>>>> origin/master
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
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/master
