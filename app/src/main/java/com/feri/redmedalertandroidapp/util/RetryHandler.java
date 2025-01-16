package com.feri.redmedalertandroidapp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class RetryHandler {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    public <T> T executeWithRetry(Callable<T> task) throws Exception {
        int attempts = 0;
        Exception lastException = null;
        long delay = INITIAL_RETRY_DELAY_MS;

        while (attempts < MAX_RETRIES) {
            try {
                return task.call();
            } catch (Exception e) {
                lastException = e;
                attempts++;

                if (attempts >= MAX_RETRIES) {
                    break;
                }

                Timber.w(e, "Retry attempt %d/%d failed", attempts, MAX_RETRIES);

                try {
                    Thread.sleep(delay);
                    delay = (long) (delay * BACKOFF_MULTIPLIER);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }

        throw new RuntimeException("Failed after " + MAX_RETRIES + " attempts", lastException);
    }

    public static <T> List<List<T>> splitIntoBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            batches.add(items.subList(i, end));
        }
        return batches;
    }

    public void waitForRetry(int attempt) {
        long delayMs = calculateDelay(attempt);
        try {
            TimeUnit.MILLISECONDS.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry wait interrupted", e);
        }
    }

    private long calculateDelay(int attempt) {
        return (long) (INITIAL_RETRY_DELAY_MS * Math.pow(BACKOFF_MULTIPLIER, attempt - 1));
    }
}
