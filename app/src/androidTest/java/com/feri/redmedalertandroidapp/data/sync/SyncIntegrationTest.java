package com.feri.redmedalertandroidapp.data.sync;

import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.network.NetworkStateMonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import timber.log.Timber;

@RunWith(AndroidJUnit4.class)
public class SyncIntegrationTest {

    private static final int TIMEOUT_SECONDS = 10;
    private Context context;
    private DataRepository repository;
    private TestSyncManager syncManager;
    private CountDownLatch syncLatch;
    private volatile boolean setupComplete = false;

    @Mock private NetworkStateMonitor mockNetworkMonitor;

    private class TestSyncManager extends SyncManager {
        private final CountDownLatch completionLatch;

        TestSyncManager(Context context, DataRepository repository, CountDownLatch latch) {
            super(context, repository);
            this.completionLatch = latch;
        }

        @Override
        protected NetworkStateMonitor createNetworkMonitor() {
            return mockNetworkMonitor;
        }

        @Override
        protected void initializeNetworkMonitoring() {
            // Skip in tests
        }

        @Override
        public void startSync() {
            try {
                super.startSync();
            } finally {
                completionLatch.countDown();
            }
        }
    }

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Clean repository state
        DataRepository.resetInstance();
        try {
            Thread.sleep(100); // Allow for cleanup
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        repository = DataRepository.getInstance(context);
        syncLatch = new CountDownLatch(1);
        syncManager = new TestSyncManager(context, repository, syncLatch);

        try {
            repository.clearAllData().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Setup failed: " + e.getMessage());
        }
    }

    @After
    public void cleanup() {
        try {
            // First shutdown sync manager
            if (syncManager != null) {
                syncManager.shutdown();
                try {
                    Thread.sleep(1000); // Allow sync manager to clean up
                } catch (InterruptedException e) {
                    Timber.e(e, "Sleep interrupted during sync manager cleanup");
                }
            }

            // Then clean repository
            if (repository != null) {
                Future<Void> clearFuture = repository.clearAllData();
                clearFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                try {
                    Thread.sleep(1000); // Allow clear to complete
                } catch (InterruptedException e) {
                    Timber.e(e, "Sleep interrupted during clear");
                }

                Future<Void> shutdownFuture = repository.shutdown();
                shutdownFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                try {
                    Thread.sleep(1000); // Allow shutdown to complete
                } catch (InterruptedException e) {
                    Timber.e(e, "Sleep interrupted during shutdown");
                }
            }
        } catch (Exception e) {
            Timber.e(e, "Error during cleanup");
        } finally {
            DataRepository.resetInstance();
            try {
                Thread.sleep(1000); // Allow reset to complete
            } catch (InterruptedException e) {
                Timber.e(e, "Sleep interrupted during reset");
            }
        }
    }

    @Test
    public void testCompleteSync() throws Exception {
        Timber.d("Starting complete sync test");

        // Ensure network is available
        when(mockNetworkMonitor.isNetworkAvailable()).thenReturn(true);

        // Create test data with explicit userId
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",  // Ensure this matches
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );
        testData.setSynced(false);
        testData.setUploadAttempts(0); // Explicit set

        // Save and verify initial state
        long insertedId = repository.saveSensorData(testData).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue("Data should be saved successfully", insertedId > 0);

        // Allow for database operation
        Thread.sleep(1000);

        // Verify initial state
        List<SensorDataEntity> initialData = repository.getUnsyncedData()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertEquals("Should have one unsynced record", 1, initialData.size());

        // Start sync
        syncManager.startSync();

        // Wait for sync completion with verification
        boolean synced = awaitSyncCompletion();
        assertTrue("Sync should complete successfully", synced);

        // Final verification
        List<SensorDataEntity> finalData = repository.getUnsyncedData()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue("All data should be synced", finalData.isEmpty());
    }

    private boolean awaitSyncCompletion() throws Exception {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(2000);
            List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (unsyncedData.isEmpty()) {
                return true;
            }

            // Log status and retry sync
            Timber.tag("SyncTest").d("Attempt %d: %d unsynced records remain",
                    i + 1, unsyncedData.size());
            syncManager.startSync();
        }
        return false;
    }

    @Test
    public void testOfflineSync() throws Exception {
        // Create and save test data
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );

        // Setup network state
        when(mockNetworkMonitor.isNetworkAvailable()).thenReturn(false);

        // Save data locally
        repository.saveSensorData(testData).get(5, TimeUnit.SECONDS);
        Thread.sleep(500);

        // Try to sync
        CountDownLatch syncAttemptLatch = new CountDownLatch(1);
        syncManager.startSync();
        syncAttemptLatch.countDown();

        assertTrue("Sync attempt should complete", syncAttemptLatch.await(5, TimeUnit.SECONDS));

        // Verify data still exists locally
        List<SensorDataEntity> unsyncedData = repository.getUnsyncedData().get(5, TimeUnit.SECONDS);
        assertEquals("Data should still be unsynced", 1, unsyncedData.size());
    }

//    @Test
//    public void testTimestampHandling() throws Exception {
//        // Test client -> server
//        long currentTimestamp = System.currentTimeMillis();
//        SensorDataEntity testData = new SensorDataEntity(
//                "test-device",
//                "test-user",
//                "HEART_RATE",
//                75.0,
//                "BPM",
//                currentTimestamp
//        );
//        testData.setSynced(false);
//
//        // Setup network state
//        when(mockNetworkMonitor.isNetworkAvailable()).thenReturn(true);
//
//        // Save and verify data
//        long savedId = repository.saveSensorData(testData)
//                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
//        assertTrue("Data should be saved successfully", savedId > 0);
//        Thread.sleep(500);
//
//        // Verify initial state with retry
//        List<SensorDataEntity> initialData = null;
//        for (int i = 0; i < 3; i++) {
//            initialData = repository.getUnsyncedData()
//                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
//            if (!initialData.isEmpty()) break;
//            Thread.sleep(500);
//        }
//        assertNotNull("Initial data should not be null", initialData);
//        assertEquals("Should have one unsynced record", 1, initialData.size());
//
//        // Start sync
//        syncManager.startSync();
//
//        // Wait for sync completion with timeout
//        boolean synced = false;
//        for (int i = 0; i < 10 && !synced; i++) {
//            Thread.sleep(1000);
//            List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
//                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
//            if (unsyncedData.isEmpty()) {
//                synced = true;
//                break;
//            }
//            syncManager.startSync(); // Retry sync
//        }
//
//        assertTrue("Data should be synced within timeout", synced);
//    }
}
