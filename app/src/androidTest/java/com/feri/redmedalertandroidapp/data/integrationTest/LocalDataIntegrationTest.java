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

<<<<<<< HEAD

=======
>>>>>>> origin/master
    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DataRepository.resetInstance(); // Reset any existing instance
        repository = DataRepository.getInstance(context);

<<<<<<< HEAD

=======
>>>>>>> origin/master
        try {
            repository.clearAllData().get(5, TimeUnit.SECONDS);
            Thread.sleep(500); // Small delay to ensure cleanup is complete
        } catch (Exception e) {
            e.printStackTrace();
            fail("Setup failed: " + e.getMessage());
        }
    }


    @After
    public void cleanup() {
        if (repository != null) {
            try {
                repository.clearAllData().get(5, TimeUnit.SECONDS);
                Thread.sleep(500); // Small delay to ensure cleanup is complete
                repository.shutdown().get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DataRepository.resetInstance();
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
        Thread.sleep(500); // Small delay to ensure data is saved
<<<<<<< HEAD

=======
>>>>>>> origin/master

        // Verify data exists
        List<SensorDataEntity> savedData = repository.getUnsyncedData().get(5, TimeUnit.SECONDS);
        assertEquals("Should have exactly one record", 1, savedData.size());


        SensorDataEntity savedEntity = savedData.get(0);
        assertEquals("Device ID should match", testData.getDeviceId(), savedEntity.getDeviceId());
        assertEquals("User ID should match", testData.getUserId(), savedEntity.getUserId());
        assertEquals("Sensor type should match", testData.getSensorType(), savedEntity.getSensorType());
        assertEquals("Value should match", testData.getValue(), savedEntity.getValue(), 0.001);
    }


<<<<<<< HEAD


=======
>>>>>>> origin/master
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
        Thread.sleep(500); // Small delay to ensure data is saved
<<<<<<< HEAD

=======
>>>>>>> origin/master

        // Verify data was added
        List<SensorDataEntity> initialData = repository.getUnsyncedData().get(5, TimeUnit.SECONDS);
        assertFalse("Should have data initially", initialData.isEmpty());


        // Clear data and wait for completion
        repository.clearAllData().get(5, TimeUnit.SECONDS);
        Thread.sleep(500); // Small delay to ensure cleanup is complete
<<<<<<< HEAD

=======
>>>>>>> origin/master

        // Verify data was cleared
        List<SensorDataEntity> finalData = repository.getUnsyncedData().get(5, TimeUnit.SECONDS);
        assertTrue("Should have no data after clearing", finalData.isEmpty());
    }
}