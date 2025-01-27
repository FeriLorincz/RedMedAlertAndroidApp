package com.feri.redmedalertandroidapp.data.upload;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.feri.redmedalertandroidapp.data.DataRepository;
import timber.log.Timber;


public class DatabaseUploadWorker extends Worker{


    private final DatabaseUploader databaseUploader;


    public DatabaseUploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        DataRepository repository = DataRepository.getInstance(context);
        this.databaseUploader = new DatabaseUploader(context, repository);
    }


    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.d("Starting scheduled database upload");
            boolean uploadSuccess = databaseUploader.uploadPendingData();
            if (!uploadSuccess) {
                Timber.w("Upload was not successful");
                return Result.retry();
            }
            databaseUploader.cleanOldData();
            return Result.success();
        } catch (Exception e) {
            Timber.e(e, "Error during scheduled upload");
            return Result.retry();
        }
    }
}