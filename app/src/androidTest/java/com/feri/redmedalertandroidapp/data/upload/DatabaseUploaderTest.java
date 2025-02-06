package com.feri.redmedalertandroidapp.data.upload;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.network.NetworkStateMonitor;
import com.feri.redmedalertandroidapp.network.SensorDataApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


@RunWith(MockitoJUnitRunner.class)
public class DatabaseUploaderTest {
    private static final int TIMEOUT_SECONDS = 10;
    private Context context;
    private DataRepository repository;
    private DatabaseUploader uploader;
    private TestNetworkStateMonitor networkMonitor;
    private SensorDataApi mockApi;

    private static class TestNetworkStateMonitor extends NetworkStateMonitor {
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
        }
    }

    private static class TestSensorDataApi implements SensorDataApi {
        private boolean shouldFail = false;
        private boolean throwingCall = false;

        public void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }
        public void setThrowingCall(boolean throwing) {
            this.throwingCall = throwing;
        }
        @Override
        public Call<Void> uploadSensorData(List<SensorDataEntity> data) {
            return new TestCall(shouldFail, throwingCall);
        }
    }

    private static class TestCall implements Call<Void> {
        private final boolean shouldFail;
        private final boolean throwing;

        TestCall(boolean shouldFail, boolean throwing) {
            this.shouldFail = shouldFail;
            this.throwing = throwing;
        }
        @Override
        public Response<Void> execute() throws IOException {
            // Asigură-te că aruncă excepție când e setat să eșueze
            if (shouldFail) {
                if (throwing) {
                    throw new IOException("Simulated network error");
                }
                return Response.error(500, ResponseBody.create(
                        MediaType.parse("text/plain"), "Error"
                ));
            }
            return Response.success(null);
        }
        @Override
        public void enqueue(Callback<Void> callback) {
            // Not needed for tests
        }
        @Override
        public boolean isExecuted() {
            return false;
        }
        @Override
        public void cancel() {
            // Not needed for tests
        }
        @Override
        public boolean isCanceled() {
            return false;
        }
        @Override
        public Call<Void> clone() {
            return new TestCall(shouldFail, throwing);
        }
        @Override
        public Request request() {
            // Returnează un request fără URL real pentru a preveni conexiunile reale
            return new Request.Builder()
                    .url("http://localhost/mock")  // URL local pentru mock
                    .build();
        }
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }
    }

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Resetare repository
        DataRepository.resetInstance();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        repository = DataRepository.getInstance(context);
        networkMonitor = new TestNetworkStateMonitor(context);
        mockApi = new TestSensorDataApi();
        // IMPORTANT: Configurăm mockApi ÎNAINTE de crearea uploader-ului
        ((TestSensorDataApi)mockApi).setShouldFail(true);
        ((TestSensorDataApi)mockApi).setThrowingCall(true);
        try {
            repository.clearAllData().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Cream un uploader custom care folosește networkMonitor și mockApi
        uploader = new DatabaseUploader(context, repository) {
            @Override
            protected NetworkStateMonitor createNetworkMonitor() {
                return networkMonitor;
            }
            @Override
            protected SensorDataApi createSensorApi() {
                return mockApi;
            }
        };
    }

    @After
    public void cleanup() {
        try {
            if (repository != null) {
                repository.clearAllData().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                Thread.sleep(100);


                repository.shutdown().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                Thread.sleep(100);
            }
        } catch (Exception e) {
            Timber.e(e, "Error during cleanup");
        } finally {
            DataRepository.resetInstance();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Timber.e(e);
            }
        }
    }

    @Test
    public void uploadPendingData_whenNoNetwork_shouldSkipUpload() throws Exception {
        // Arrange
        networkMonitor.setNetworkAvailable(false);
        SensorDataEntity testData = createTestEntity();
        repository.saveSensorData(testData).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Thread.sleep(500);
        // Act
        boolean result = uploader.uploadPendingData();
        // Assert
        assertFalse("Upload should fail when network is not available", result);
        List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertEquals("Data should remain unsynced", 1, unsyncedData.size());
    }

    @Test
    public void uploadPendingData_whenNoData_shouldSkipUpload() throws Exception {
        // Act
        boolean result = uploader.uploadPendingData();
        // Assert
        assertTrue(result);
    }

    @Test
    public void uploadPendingData_whenSuccess_shouldMarkAsSynced() throws Exception {
        // Arrange
        networkMonitor.setNetworkAvailable(true);
        SensorDataEntity testData = createTestEntity();
        repository.saveSensorData(testData).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Thread.sleep(500);  // Așteaptă să se salveze datele
        // Act
        boolean result = uploader.uploadPendingData();
        // Assert
        assertTrue(result);
        // Așteaptă să se proceseze marcarea ca sincronizat
        Thread.sleep(1000);
        List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue("Data should be marked as synced", unsyncedData.isEmpty());
    }

    @Test
    public void uploadPendingData_whenError_shouldIncrementAttempts() throws Exception {
        // Arrange
        networkMonitor.setNetworkAvailable(true);
        ((TestSensorDataApi)mockApi).setShouldFail(true);
        ((TestSensorDataApi)mockApi).setThrowingCall(true);
        // Create and save test data
        SensorDataEntity testData = createTestEntity();
        assertFalse("Entity should not be synced initially", testData.isSynced());
        assertEquals("Entity should have 0 upload attempts initially", 0, testData.getUploadAttempts());
        // Salvează și verifică rezultatul
        Future<Long> saveFuture = repository.saveSensorData(testData);
        saveFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);  // Așteaptă salvarea
        Thread.sleep(500);
        // Verifică starea inițială cu debug
        List<SensorDataEntity> initialData = repository.getUnsyncedData()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        System.out.println("Initial data size: " + initialData.size());
        if (!initialData.isEmpty()) {
            System.out.println("First record synced status: " + initialData.get(0).isSynced());
            System.out.println("First record upload attempts: " + initialData.get(0).getUploadAttempts());
        }
        assertEquals("Should have 1 unsynced record initially", 1, initialData.size());
        assertEquals("Initial upload attempts should be 0", 0, initialData.get(0).getUploadAttempts());
        // Re-verifică configurarea mock-ului chiar înainte de upload
        ((TestSensorDataApi)mockApi).setShouldFail(true);
        ((TestSensorDataApi)mockApi).setThrowingCall(true);
        // Act
        boolean result = uploader.uploadPendingData();
        // Assert
        assertFalse("Upload should fail when error occurs", result);
        Thread.sleep(500);
        List<SensorDataEntity> unsyncedData = repository.getUnsyncedData()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertEquals("Should still have unsynced data", 1, unsyncedData.size());
        assertEquals("Upload attempts should be incremented", 1, unsyncedData.get(0).getUploadAttempts());
    }

    private SensorDataEntity createTestEntity() {
        SensorDataEntity entity = new SensorDataEntity(
                "test-device",
                "test-user",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );
        entity.setSynced(false);  // Asigură-te că entitatea este marcată ca nesincronizată
        entity.setUploadAttempts(0);  // Resetează numărul de încercări
        return entity;
    }
}