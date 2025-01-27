package com.feri.redmedalertandroidapp.data.sync;

import android.content.Context;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

<<<<<<< HEAD

=======
>>>>>>> origin/master
import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.data.upload.DatabaseUploader;
import com.feri.redmedalertandroidapp.network.NetworkStateMonitor;
import com.feri.redmedalertandroidapp.util.RetryHandler;

<<<<<<< HEAD

=======
>>>>>>> origin/master
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

<<<<<<< HEAD

=======
>>>>>>> origin/master
import timber.log.Timber;

public class SyncManager {

    private static final int SYNC_TIMEOUT_SECONDS = 30;
    private static final int MAX_BATCH_SIZE = 100;

    private final Context context;
    private final DataRepository dataRepository;
    protected final DatabaseUploader databaseUploader;
    protected final NetworkStateMonitor networkMonitor;
    protected final RetryHandler retryHandler;
    private final ExecutorService syncExecutor;
    private final AtomicBoolean isSyncing;

    public SyncManager(Context context, DataRepository dataRepository) {
        this.context = context;
        this.dataRepository = dataRepository;
        this.networkMonitor = createNetworkMonitor();
        this.databaseUploader = createDatabaseUploader();
        this.retryHandler = createRetryHandler();
        this.syncExecutor = Executors.newSingleThreadExecutor();
        this.isSyncing = new AtomicBoolean(false);

<<<<<<< HEAD

        initializeNetworkMonitoring();
    }


=======
        initializeNetworkMonitoring();
    }

>>>>>>> origin/master
    protected NetworkStateMonitor createNetworkMonitor() {
        return new NetworkStateMonitor(context);
    }

<<<<<<< HEAD

=======
>>>>>>> origin/master
    protected DatabaseUploader createDatabaseUploader() {
        return new DatabaseUploader(context, dataRepository);
    }

<<<<<<< HEAD

=======
>>>>>>> origin/master
    protected RetryHandler createRetryHandler() {
        return new RetryHandler();
    }

<<<<<<< HEAD

=======
>>>>>>> origin/master
    protected void initializeNetworkMonitoring() {
        networkMonitor.startMonitoring(isConnected -> {
            if (isConnected && !isSyncing.get()) {
                startSync();
            }
        });
    }

<<<<<<< HEAD

=======
>>>>>>> origin/master
    public void startSync() {
        if (!networkMonitor.isNetworkAvailable() || isSyncing.get()) {
            Timber.d("Sync skipped: Network unavailable or sync in progress");
            return;
        }

<<<<<<< HEAD

=======
>>>>>>> origin/master
        syncExecutor.execute(() -> {
            if (!isSyncing.compareAndSet(false, true)) {
                return;
            }

<<<<<<< HEAD

=======
>>>>>>> origin/master
            try {
                performSync();
            } catch (Exception e) {
                Timber.e(e, "Error during sync");
            } finally {
                isSyncing.set(false);
            }
        });
    }


<<<<<<< HEAD


=======
>>>>>>> origin/master
    private void performSync() {
        try {
            Future<List<SensorDataEntity>> futureData = dataRepository.getUnsyncedData();
            List<SensorDataEntity> unsyncedData = futureData.get(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);

<<<<<<< HEAD

=======
>>>>>>> origin/master
            if (unsyncedData.isEmpty()) {
                Timber.d("No unsynced data to process");
                return;
            }

<<<<<<< HEAD

            boolean result = databaseUploader.uploadPendingData();
            if (!result) {
                Timber.w("Upload failed, scheduling retry");
                scheduleRetry();
            }


=======
            try {
                Timber.d("Starting sync for %d records", unsyncedData.size());
                // Executăm sincronizarea
                Boolean result = retryHandler.executeWithRetry(() -> {
                    boolean uploadResult = databaseUploader.uploadPendingData();
                    Timber.d("Upload attempt result: %s", uploadResult);
                    return uploadResult;
                });

                if (result != null && result) {
                    Timber.d("Sync successful, cleaning up old data");
                    cleanupOldData();
                } else {
                    Timber.w("Sync failed after retries");
                }
            } catch (Exception e) {
                Timber.e(e, "Error during data upload");
                throw e;
            }

>>>>>>> origin/master
        } catch (Exception e) {
            Timber.e(e, "Error in performSync");
            scheduleRetry();
        }
    }

<<<<<<< HEAD

=======
>>>>>>> origin/master
    private List<List<SensorDataEntity>> createBatches(List<SensorDataEntity> data) {
        return RetryHandler.splitIntoBatches(data, MAX_BATCH_SIZE);
    }

<<<<<<< HEAD

=======
>>>>>>> origin/master
    private void cleanupOldData() {
        try {
            long cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000); // 7 days
            dataRepository.cleanOldData(cutoffTime).get(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            Timber.e(e, "Error cleaning old data");
        }
    }

<<<<<<< HEAD

=======
>>>>>>> origin/master
    private void scheduleRetry() {
        WorkManager.getInstance(context)
                .getWorkInfosByTagLiveData("SYNC_WORK")
                .observeForever(workInfos -> {
                    for (WorkInfo workInfo : workInfos) {
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            startSync();
                            break;
                        }
                    }
                });
    }

<<<<<<< HEAD

=======
>>>>>>> origin/master
    public void shutdown() {
        networkMonitor.stopMonitoring();
        syncExecutor.shutdown();
        try {
            if (!syncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                syncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            syncExecutor.shutdownNow();
        }
    }
<<<<<<< HEAD

}
=======
}
>>>>>>> origin/master
