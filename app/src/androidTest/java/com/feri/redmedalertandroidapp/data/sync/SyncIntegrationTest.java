package com.feri.redmedalertandroidapp.data.sync;

import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
<<<<<<< HEAD
import com.feri.redmedalertandroidapp.network.NetworkStateCallback;
import com.feri.redmedalertandroidapp.network.NetworkStateMonitor;
import com.feri.redmedalertandroidapp.util.RetryHandler;
=======
import com.feri.redmedalertandroidapp.network.NetworkStateMonitor;

>>>>>>> origin/master
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
<<<<<<< HEAD

import java.util.Arrays;
=======
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

>>>>>>> origin/master
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
<<<<<<< HEAD
import static org.junit.Assert.*;
=======

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

>>>>>>> origin/master
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
<<<<<<< HEAD
    private TestNetworkStateMonitor testNetworkMonitor;


    // Implementare de test pentru NetworkStateMonitor
    private class TestNetworkStateMonitor extends NetworkStateMonitor {
        private boolean networkAvailable = true;


        public TestNetworkStateMonitor(Context context) {
            super(context);
        }


        @Override
        public boolean isNetworkAvailable() {
            return networkAvailable;
        }


        public void setNetworkAvailable(boolean available) {
            this.networkAvailable = available;
            notifyNetworkState(available);
        }


        @Override
        public void startMonitoring(NetworkStateCallback callback) {
            // Doar setăm callback-ul fără a înregistra network callback-ul real
            this.callback = callback;
        }


        @Override
        public void stopMonitoring() {
            this.callback = null;
        }
    }

=======
    private volatile boolean setupComplete = false;

    @Mock private NetworkStateMonitor mockNetworkMonitor;
>>>>>>> origin/master

    private class TestSyncManager extends SyncManager {
        private final CountDownLatch completionLatch;

<<<<<<< HEAD

=======
>>>>>>> origin/master
        TestSyncManager(Context context, DataRepository repository, CountDownLatch latch) {
            super(context, repository);
            this.completionLatch = latch;
        }

<<<<<<< HEAD

        @Override
        protected NetworkStateMonitor createNetworkMonitor() {
            return testNetworkMonitor;
        }


=======
        @Override
        protected NetworkStateMonitor createNetworkMonitor() {
            return mockNetworkMonitor;
        }

>>>>>>> origin/master
        @Override
        protected void initializeNetworkMonitoring() {
            // Skip in tests
        }

<<<<<<< HEAD

=======
>>>>>>> origin/master
        @Override
        public void startSync() {
            try {
                super.startSync();
            } finally {
                completionLatch.countDown();
            }
        }
    }

<<<<<<< HEAD

    private boolean awaitSyncCompletion() throws Exception {
        int maxAttempts = 10;
        int waitTimeMs = 1000;


        for (int i = 0; i < maxAttempts; i++) {
            Thread.sleep(waitTimeMs);


            List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);


            if (unsyncedData.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Before
    public void setup() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Cleanup any existing state
        try {
            DataRepository.resetInstance();
            context.deleteDatabase("redmedalert_db");
            Thread.sleep(500);
        } catch (Exception e) {
            Timber.e(e, "Error during cleanup");
        }

        repository = DataRepository.getInstance(context);

        // Verify repository is properly initialized
        RetryHandler retryHandler = new RetryHandler();
        retryHandler.executeWithRetry(() -> {
            Future<Void> clearFuture = repository.clearAllData();
            clearFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Verify database is accessible
            Future<List<SensorDataEntity>> checkFuture = repository.getUnsyncedData();
            List<SensorDataEntity> checkData = checkFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (checkData == null) {
                throw new RuntimeException("Database not accessible");
            }
            Thread.sleep(500);
            return null;
        });

        syncLatch = new CountDownLatch(1);
        testNetworkMonitor = new TestNetworkStateMonitor(context);
        syncManager = new TestSyncManager(context, repository, syncLatch);
=======
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
>>>>>>> origin/master
    }

    @Test
    public void testCompleteSync() throws Exception {
<<<<<<< HEAD
        // Disable network initially
        testNetworkMonitor.setNetworkAvailable(false);
        Thread.sleep(500);

        // Clear data with retry
        RetryHandler retryHandler = new RetryHandler();
        retryHandler.executeWithRetry(() -> {
            repository.clearAllData().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Thread.sleep(500);

            // Verify clear
            List<SensorDataEntity> checkData = repository.getUnsyncedData()
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!checkData.isEmpty()) {
                throw new RuntimeException("Database not cleared properly");
            }
            return null;
        });

        // Create test data
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",
=======
        Timber.d("Starting complete sync test");

        // Ensure network is available
        when(mockNetworkMonitor.isNetworkAvailable()).thenReturn(true);

        // Create test data with explicit userId
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",  // Ensure this matches
>>>>>>> origin/master
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );
        testData.setSynced(false);
<<<<<<< HEAD
        testData.setUploadAttempts(0);

        // Save with explicit verification
        Long savedId = retryHandler.executeWithRetry(() -> {
            Future<Long> saveFuture = repository.saveSensorData(testData);
            Long id = saveFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Timber.d("Data saved with ID: %d", id);

            Thread.sleep(2000);

            // Verificare directă după ID
            List<SensorDataEntity> verification = repository.getByIds(Arrays.asList(id))
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (verification.isEmpty()) {
                throw new RuntimeException("Data not found by ID after save");
            }

            // Verificare separată pentru unsynced data
            List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (unsyncedData.isEmpty()) {
                throw new RuntimeException("Data not found in unsynced after save");
            }

            return id;
        });

        assertTrue("Data should be saved successfully", savedId > 0);
        Thread.sleep(1000);

        // Verify initial state with better error handling
        int retryCount = 0;
        boolean initialStateVerified = false;
        String verificationError = "";

        while (!initialStateVerified && retryCount < 5) {
            try {
                List<SensorDataEntity> initialData = repository.getUnsyncedData()
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

                Timber.d("Found %d unsynced records", initialData.size());

                if (initialData.size() == 1) {
                    SensorDataEntity savedEntity = initialData.get(0);
                    if (!testData.getDeviceId().equals(savedEntity.getDeviceId())) {
                        verificationError = "Device ID mismatch";
                        continue;
                    }
                    if (!testData.getUserId().equals(savedEntity.getUserId())) {
                        verificationError = "User ID mismatch";
                        continue;
                    }
                    if (savedEntity.isSynced()) {
                        verificationError = "Data incorrectly marked as synced";
                        continue;
                    }
                    initialStateVerified = true;
                } else {
                    verificationError = String.format("Expected 1 unsynced record, found %d", initialData.size());
                }
            } catch (Exception e) {
                verificationError = "Error checking initial state: " + e.getMessage();
                Timber.e(e, "Verification attempt %d failed", retryCount);
            }

            if (!initialStateVerified) {
                retryCount++;
                Thread.sleep(1000);
            }
        }

        assertTrue("Initial state verification failed: " + verificationError,
                initialStateVerified);

        // Enable network and continue with sync
        testNetworkMonitor.setNetworkAvailable(true);
        Thread.sleep(1000);

        syncManager.startSync();
        assertTrue("Sync latch wait failed",
                syncLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // Verify sync completion with retry
        boolean syncCompleted = awaitSyncCompletion();
        assertTrue("Sync should complete successfully", syncCompleted);
    }


    @Test
    public void testOfflineSync() throws Exception {
        // Set network as unavailable
        testNetworkMonitor.setNetworkAvailable(false);


=======
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
>>>>>>> origin/master
        // Create and save test data
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );

<<<<<<< HEAD
=======
        // Setup network state
        when(mockNetworkMonitor.isNetworkAvailable()).thenReturn(false);
>>>>>>> origin/master

        // Save data locally
        repository.saveSensorData(testData).get(5, TimeUnit.SECONDS);
        Thread.sleep(500);

<<<<<<< HEAD

        // Try to sync
        syncManager.startSync();


        // Verify data still exists locally
        List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
                .get(5, TimeUnit.SECONDS);
        assertEquals("Data should still be unsynced", 1, unsyncedData.size());
    }


    @After
    public void cleanup() {
        try {
            if (syncManager != null) {
                syncManager.shutdown();
                Thread.sleep(1000);
            }


            if (repository != null) {
                RetryHandler retryHandler = new RetryHandler();
                retryHandler.executeWithRetry(() -> {
                    repository.clearAllData().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    Thread.sleep(500);


                    Future<Void> shutdownFuture = repository.shutdown();
                    shutdownFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    Thread.sleep(500);
                    return null;
                });
            }
        } catch (Exception e) {
            Timber.e(e, "Error during cleanup");
        } finally {
            try {
                DataRepository.resetInstance();
                Thread.sleep(1000);
            } catch (Exception e) {
                Timber.e(e, "Error during repository reset");
            }
        }
    }






=======
        // Try to sync
        CountDownLatch syncAttemptLatch = new CountDownLatch(1);
        syncManager.startSync();
        syncAttemptLatch.countDown();

        assertTrue("Sync attempt should complete", syncAttemptLatch.await(5, TimeUnit.SECONDS));

        // Verify data still exists locally
        List<SensorDataEntity> unsyncedData = repository.getUnsyncedData().get(5, TimeUnit.SECONDS);
        assertEquals("Data should still be unsynced", 1, unsyncedData.size());
    }

>>>>>>> origin/master
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
