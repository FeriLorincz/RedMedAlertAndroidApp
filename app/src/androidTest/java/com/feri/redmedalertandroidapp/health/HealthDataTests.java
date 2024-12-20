package com.feri.redmedalertandroidapp.health;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;
import com.feri.redmedalertandroidapp.api.service.ApiCallback;
import com.feri.redmedalertandroidapp.api.service.DatabaseHelper;
import com.feri.redmedalertandroidapp.api.validator.HealthDataValidator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class HealthDataTests {

    private Context context;
    private ApiClient apiClient;
    private DatabaseHelper databaseHelper;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        apiClient = ApiClient.getInstance(context);
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    @Test
    public void testValidData() {
        Map<String, Double> validData = new HashMap<>();
        validData.put("heart_rate", 75.0);
        validData.put("temperature", 36.6);
        validData.put("blood_oxygen", 98.0);

        assertTrue(HealthDataValidator.isValidData(validData));
    }

    @Test
    public void testInvalidData() {
        Map<String, Double> invalidData = new HashMap<>();
        invalidData.put("heart_rate", 250.0); // Valoare invalidă

        assertFalse(HealthDataValidator.isValidData(invalidData));
    }

    @Test
    public void testDataCaching() {
        Map<String, Double> testData = new HashMap<>();
        testData.put("heart_rate", 75.0);

        databaseHelper.cacheHealthData(testData);

        // Verificăm că datele au fost salvate
        List<HealthDataEntity> cachedData = databaseHelper.getUnuploadedData();
        assertFalse(cachedData.isEmpty());
        assertEquals(75.0, cachedData.get(0).value, 0.01);
    }

    @Test
    public void testDataUpload() {
        Map<String, Double> testData = new HashMap<>();
        testData.put("heart_rate", 75.0);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        apiClient.uploadHealthData(testData, new ApiCallback() {
            @Override
            public void onSuccess() {
                success[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                success[0] = false;
                latch.countDown();
            }
        });

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Test timed out");
        }

        assertTrue(success[0]);
    }
}
