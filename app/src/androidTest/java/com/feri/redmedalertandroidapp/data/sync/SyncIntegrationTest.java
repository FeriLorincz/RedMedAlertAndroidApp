package com.feri.redmedalertandroidapp.data.sync;


import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.network.NetworkStateCallback;
import com.feri.redmedalertandroidapp.network.NetworkStateMonitor;
import com.feri.redmedalertandroidapp.util.RetryHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
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




    private class TestSyncManager extends SyncManager {
        private final CountDownLatch completionLatch;




        TestSyncManager(Context context, DataRepository repository, CountDownLatch latch) {
            super(context, repository);
            this.completionLatch = latch;
        }




        @Override
        protected NetworkStateMonitor createNetworkMonitor() {
            return testNetworkMonitor;
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
    }


    @Test
    public void testCompleteSync() throws Exception {
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
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );
        testData.setSynced(false);
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




        // Create and save test data
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );




        // Save data locally
        repository.saveSensorData(testData).get(5, TimeUnit.SECONDS);
        Thread.sleep(500);




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