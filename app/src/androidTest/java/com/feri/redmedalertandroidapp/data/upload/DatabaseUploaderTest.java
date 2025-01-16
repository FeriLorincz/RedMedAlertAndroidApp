package com.feri.redmedalertandroidapp.data.upload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.network.SensorDataApi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseUploaderTest {
    @Mock
    private Context mockContext;

    @Mock
    private DataRepository mockRepository;

    @Mock
    private SensorDataApi mockApi;

    @Mock
    private ConnectivityManager mockConnectivityManager;

    @Mock
    private NetworkInfo mockNetworkInfo;

    @Mock
    private Call<Void> mockCall;

    private DatabaseUploader uploader;
    private List<SensorDataEntity> testData;

    @Before
    public void setup() throws IOException {
        // Setup context și network info
        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
                .thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo())
                .thenReturn(mockNetworkInfo);
        when(mockNetworkInfo.isConnectedOrConnecting())
                .thenReturn(true);

        // Setup test data
        testData = Arrays.asList(
                createTestEntity(1L),
                createTestEntity(2L)
        );

        // Setup repository și API mocks
        Future<List<SensorDataEntity>> futureData = CompletableFuture.completedFuture(testData);
        when(mockRepository.getUnsyncedData()).thenReturn(futureData);

        when(mockApi.uploadSensorData(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(null));

        // Setup mock pentru operațiile asincrone
        Future<Void> futureVoid = CompletableFuture.completedFuture(null);
        when(mockRepository.markAsSynced(anyList())).thenReturn(futureVoid);
        when(mockRepository.incrementUploadAttempts(anyList())).thenReturn(futureVoid);

        // Inițializare uploader cu API mock
        uploader = new DatabaseUploader(mockContext, mockRepository) {
            @Override
            protected SensorDataApi createSensorApi() {
                return mockApi;
            }
        };
    }

    @Test
    public void uploadPendingData_whenNoNetwork_shouldSkipUpload() {
        when(mockNetworkInfo.isConnectedOrConnecting()).thenReturn(false);
        uploader.uploadPendingData();
        verify(mockRepository, never()).getUnsyncedData();
    }

    @Test
    public void uploadPendingData_whenNoData_shouldSkipUpload() {
        Future<List<SensorDataEntity>> emptyFuture = CompletableFuture.completedFuture(Arrays.asList());
        when(mockRepository.getUnsyncedData()).thenReturn(emptyFuture);
        uploader.uploadPendingData();
        verify(mockApi, never()).uploadSensorData(any());
    }

    @Test
    public void uploadPendingData_whenSuccess_shouldMarkAsSynced() throws Exception {
        Future<Void> successFuture = CompletableFuture.completedFuture(null);
        when(mockRepository.markAsSynced(Arrays.asList(1L, 2L))).thenReturn(successFuture);

        uploader.uploadPendingData();

        verify(mockRepository).markAsSynced(Arrays.asList(1L, 2L));
    }

    @Test
    public void uploadPendingData_whenError_shouldIncrementAttempts() throws Exception {
        when(mockCall.execute()).thenThrow(new RuntimeException("Network error"));
        Future<Void> errorFuture = CompletableFuture.completedFuture(null);
        when(mockRepository.incrementUploadAttempts(Arrays.asList(1L, 2L))).thenReturn(errorFuture);

        uploader.uploadPendingData();

        verify(mockRepository).incrementUploadAttempts(Arrays.asList(1L, 2L));
    }

    private SensorDataEntity createTestEntity(Long id) {
        SensorDataEntity entity = new SensorDataEntity(
                "device1",
                "user1",
                "HEART_RATE",
                75.0,
                "BPM",
                System.currentTimeMillis()
        );
        entity.setId(id);
        return entity;
    }
}
