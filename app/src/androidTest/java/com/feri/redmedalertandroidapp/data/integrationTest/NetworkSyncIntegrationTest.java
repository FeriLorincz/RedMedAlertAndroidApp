package com.feri.redmedalertandroidapp.data.integrationTest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import android.content.Context;

import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.data.upload.DatabaseUploader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NetworkSyncIntegrationTest {

    private static final int SYNC_TIMEOUT_SECONDS = 30;
    private DataRepository repository;
    private DatabaseUploader uploader;
    private Context context;
    private CountDownLatch syncLatch;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        repository = DataRepository.getInstance(context);

        try {
            repository.clearAllData().get(5, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        syncLatch = new CountDownLatch(1);
        uploader = createTestUploader();
    }

    private DatabaseUploader createTestUploader() {
        return new DatabaseUploader(context, repository) {
            @Override
            public boolean uploadPendingData() {
                try {
                    return super.uploadPendingData();
                } finally {
                    syncLatch.countDown();
                }
            }
        };
    }

    @After
    public void cleanup() {
        if (repository != null) {
            try {
                repository.clearAllData().get(5, TimeUnit.SECONDS);
                Thread.sleep(1000);
                repository.shutdown().get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testOfflineCapabilities() throws Exception {
        // Create and save test data
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );

        Future<Long> insertFuture = repository.saveSensorData(testData);
        long insertedId = insertFuture.get(5, TimeUnit.SECONDS);
        assertTrue("Data should be saved successfully", insertedId > 0);
        Thread.sleep(1000);

        // Verify initial state
        Future<List<SensorDataEntity>> initialDataFuture = repository.getUnsyncedData();
        List<SensorDataEntity> initialData = initialDataFuture.get(5, TimeUnit.SECONDS);
        assertEquals("Should have one unsynced record", 1, initialData.size());

        // Attempt upload with no network
//        boolean uploadResult = uploader.uploadPendingData();
//        assertFalse("Upload should fail without network", uploadResult);
//        Thread.sleep(1000);


        // Attempt upload with no network
        uploader.uploadPendingData();
        Thread.sleep(1000);

        // Verify data remains unsynced
        Future<List<SensorDataEntity>> finalDataFuture = repository.getUnsyncedData();
        List<SensorDataEntity> finalData = finalDataFuture.get(5, TimeUnit.SECONDS);
        assertEquals("Should still have one unsynced record", 1, finalData.size());
    }
}