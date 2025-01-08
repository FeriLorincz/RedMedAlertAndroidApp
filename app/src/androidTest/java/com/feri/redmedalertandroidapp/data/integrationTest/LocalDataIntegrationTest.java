package com.feri.redmedalertandroidapp.data.integrationTest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import android.content.Context;

import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LocalDataIntegrationTest {

    private DataRepository repository;
    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        repository = DataRepository.getInstance(context);
        repository.clearAllData();
        // Wait for clearAllData to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void cleanup() {
        if (repository != null) {
            repository.clearAllData();
            // Wait for cleanup to complete
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            repository.shutdown();
        }
    }

    @Test
    public void testLocalDataStorage() throws InterruptedException {
        // Create test data
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );

        // Save data
        long insertedId = repository.saveSensorData(testData);
        assertTrue("Data should be saved successfully", insertedId > 0);

        // Wait for save to complete
        Thread.sleep(1000);

        // Verify data exists
        List<SensorDataEntity> savedData = repository.getUnsyncedData();
        assertEquals("Should have exactly one record", 1, savedData.size());

        SensorDataEntity savedEntity = savedData.get(0);
        assertEquals("Device ID should match", testData.getDeviceId(), savedEntity.getDeviceId());
        assertEquals("User ID should match", testData.getUserId(), savedEntity.getUserId());
        assertEquals("Sensor type should match", testData.getSensorType(), savedEntity.getSensorType());
        assertEquals("Value should match", testData.getValue(), savedEntity.getValue(), 0.001);
    }

    @Test
    public void testClearData() throws InterruptedException {
        // Add test data
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );
        repository.saveSensorData(testData);
        Thread.sleep(1000);

        // Verify data was added
        List<SensorDataEntity> initialData = repository.getUnsyncedData();
        assertFalse("Should have data initially", initialData.isEmpty());

        // Clear data
        repository.clearAllData();
        Thread.sleep(1000);

        // Verify data was cleared
        List<SensorDataEntity> finalData = repository.getUnsyncedData();
        assertTrue("Should have no data after clearing", finalData.isEmpty());
    }
}




/* asta-i bun , dar cu erori la get

    private DataRepository repository;
    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        repository = DataRepository.getInstance(context);

        // Clear data and wait for completion
        try {
            repository.clearAllData().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void cleanup() {
        if (repository != null) {
            try {
                // Clear data and wait for completion
                repository.clearAllData().get(5, TimeUnit.SECONDS);
                // Shutdown repository and wait for completion
                repository.shutdown().get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testLocalDataStorage() throws Exception {
        // Create test data
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );

        // Save data and wait for completion
        long insertedId = repository.saveSensorData(testData).get(5, TimeUnit.SECONDS);
        assertTrue("Data should be saved successfully", insertedId > 0);

        // Verify data exists
        List<SensorDataEntity> savedData = repository.getUnsyncedData().get(5, TimeUnit.SECONDS);
        assertEquals("Should have exactly one record", 1, savedData.size());

        SensorDataEntity savedEntity = savedData.get(0);
        assertEquals("Device ID should match", testData.getDeviceId(), savedEntity.getDeviceId());
        assertEquals("User ID should match", testData.getUserId(), savedEntity.getUserId());
        assertEquals("Sensor type should match", testData.getSensorType(), savedEntity.getSensorType());
        assertEquals("Value should match", testData.getValue(), savedEntity.getValue(), 0.001);
    }

    @Test
    public void testClearData() throws Exception {
        // Add test data
        SensorDataEntity testData = new SensorDataEntity(
                "test-device",
                "test-user",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );

        // Save data and wait for completion
        repository.saveSensorData(testData).get(5, TimeUnit.SECONDS);

        // Verify data was added
        List<SensorDataEntity> initialData = repository.getUnsyncedData().get(5, TimeUnit.SECONDS);
        assertFalse("Should have data initially", initialData.isEmpty());

        // Clear data and wait for completion
        repository.clearAllData().get(5, TimeUnit.SECONDS);

        // Verify data was cleared
        List<SensorDataEntity> finalData = repository.getUnsyncedData().get(5, TimeUnit.SECONDS);
        assertTrue("Should have no data after clearing", finalData.isEmpty());
    }
}
*/