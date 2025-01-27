package com.feri.redmedalertandroidapp.data.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.feri.redmedalertandroidapp.data.dao.SensorDataDao;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.data.utils.DateConverter;
import java.util.concurrent.atomic.AtomicBoolean;
import timber.log.Timber;

@Database(entities = {SensorDataEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase{

    private static volatile AppDatabase instance;
    private static final Object LOCK = new Object();
    private final AtomicBoolean isOpen = new AtomicBoolean(false);

    public abstract SensorDataDao sensorDataDao();

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            if (instance != null) {
                instance.isOpen.set(true);
            }
            createInvalidationTracker(db);
        }
    };

    private static void createInvalidationTracker(SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS room_table_modification_log");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_table_modification_log " +
                "(`table_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`table_name` TEXT NOT NULL, " +
                "`invalidated` INTEGER NOT NULL DEFAULT 0)");
    }

    public static synchronized AppDatabase buildDatabase(Context context) {
        if (instance == null || !instance.isOpen()) {
            synchronized (LOCK) {
                try {
                    if (instance != null) {
                        try {
                            instance.close();
                        } catch (Exception e) {
                            Timber.e(e, "Error closing old database instance");
                        }
                        instance = null;
                    }

                    // Delete database file to ensure clean state
                    context.deleteDatabase("redmedalert_db");
                    Thread.sleep(100);

                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "redmedalert_db")
                            .addCallback(roomCallback)
                            .build();

                    // Ensure database is open
                    instance.getOpenHelper().getWritableDatabase();
                    instance.isOpen.set(true);

                } catch (Exception e) {
                    Timber.e(e, "Error building database");
                    throw new RuntimeException("Could not build database", e);
                }
            }
        }
        return instance;
    }

    @Override
    public void close() {
        synchronized (LOCK) {
            isOpen.set(false);
            super.close();
        }
    }

    public boolean isOpen() {
        return isOpen.get() && getOpenHelper().getWritableDatabase().isOpen();
    }


    public static void closeDatabase() {
        if (instance != null && instance.isOpen()) {
            synchronized (LOCK) {
                try {
                    instance.close();
                    Timber.d("Database closed successfully");
                } catch (Exception e) {
                    Timber.e(e, "Error closing database");
                } finally {
                    instance = null;
                }
            }
        }
    }
}
