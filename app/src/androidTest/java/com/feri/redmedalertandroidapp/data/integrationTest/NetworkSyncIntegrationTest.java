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
        repository.clearAllData();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        syncLatch = new CountDownLatch(1);
        uploader = createTestUploader();
    }

    private DatabaseUploader createTestUploader() {
        return new DatabaseUploader(context, repository) {
            @Override
            public void uploadPendingData() {
                try {
                    super.uploadPendingData();
                } finally {
                    syncLatch.countDown();
                }
            }
        };
    }

    @After
    public void cleanup() {
        if (repository != null) {
            repository.clearAllData();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            repository.shutdown();
        }
    }

    @Test
    public void testOfflineCapabilities() throws InterruptedException {
        // Create and save test data
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );

        long insertedId = repository.saveSensorData(testData);
        assertTrue("Data should be saved successfully", insertedId > 0);
        Thread.sleep(1000);

        // Verify initial state
        List<SensorDataEntity> initialData = repository.getUnsyncedData();
        assertEquals("Should have one unsynced record", 1, initialData.size());

        // Attempt upload with no network
        uploader.uploadPendingData();
        Thread.sleep(1000);

        // Verify data remains unsynced
        List<SensorDataEntity> finalData = repository.getUnsyncedData();
        assertEquals("Should still have one unsynced record", 1, finalData.size());
    }
}
