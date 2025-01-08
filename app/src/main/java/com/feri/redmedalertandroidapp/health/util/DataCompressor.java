package com.feri.redmedalertandroidapp.health.util;

import android.util.Log;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import java.io.*;
import java.util.*;

public class DataCompressor {

    private static final String TAG = "DataCompressor";
    private static final int OPTIMAL_BATCH_SIZE = 100;
    private static final Gson gson = new Gson();
    private static final double COMPRESSION_RATIO = 0.4; // 40% din dimensiunea originală

    public static List<byte[]> compressDataBatches(List<HealthDataEntity> data, float sizeLimit) {
        List<byte[]> batches = new ArrayList<>();
        List<List<HealthDataEntity>> chunks = splitIntoChunks(data);

        for (List<HealthDataEntity> chunk : chunks) {
            byte[] compressed = compressData(chunk);
            if (compressed != null && compressed.length <= sizeLimit) {
                batches.add(compressed);
            }
        }
        return batches;
    }

    public static byte[] compressData(List<HealthDataEntity> data) {
        try {
            String jsonData = gson.toJson(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipStream = new GZIPOutputStream(outputStream)) {
                gzipStream.write(jsonData.getBytes(StandardCharsets.UTF_8));
            }

            byte[] compressedData = outputStream.toByteArray();
            Log.d(TAG, String.format("Compressed %d entities from %d bytes to %d bytes",
                    data.size(), jsonData.length(), compressedData.length));

            return compressedData;
        } catch (Exception e) {
            Log.e(TAG, "Error compressing data: " + e.getMessage());
            return null;
        }
    }

    private static List<List<HealthDataEntity>> splitIntoChunks(List<HealthDataEntity> data) {
        List<List<HealthDataEntity>> chunks = new ArrayList<>();
        for (int i = 0; i < data.size(); i += OPTIMAL_BATCH_SIZE) {
            chunks.add(data.subList(i, Math.min(data.size(), i + OPTIMAL_BATCH_SIZE)));
        }
        return chunks;
    }

    public static int estimateCompressedSize(List<HealthDataEntity> data) {
        String jsonData = gson.toJson(data);
        return (int) (jsonData.length() * COMPRESSION_RATIO);
    }
}
