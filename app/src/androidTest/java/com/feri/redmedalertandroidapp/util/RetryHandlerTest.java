package com.feri.redmedalertandroidapp.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
<<<<<<< HEAD
import androidx.test.ext.junit.runners.AndroidJUnit4;
=======

import androidx.test.ext.junit.runners.AndroidJUnit4;

>>>>>>> origin/master
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
<<<<<<< HEAD
=======

>>>>>>> origin/master
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

<<<<<<< HEAD

=======
>>>>>>> origin/master
public class RetryHandlerTest {

    private RetryHandler retryHandler;
    @Mock
    private Callable<String> mockTask;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        retryHandler = new RetryHandler();
    }

    @Test
    public void executeWithRetry_shouldSucceedOnFirstTry() throws Exception {
        // Arrange
        when(mockTask.call()).thenReturn("success");
<<<<<<< HEAD
        // Act
        String result = retryHandler.executeWithRetry(mockTask);
=======

        // Act
        String result = retryHandler.executeWithRetry(mockTask);

>>>>>>> origin/master
        // Assert
        assertEquals("success", result);
        verify(mockTask, times(1)).call();
    }

    @Test
    public void executeWithRetry_shouldRetryAndSucceed() throws Exception {
        // Arrange
        when(mockTask.call())
                .thenThrow(new RuntimeException("First attempt failed"))
                .thenThrow(new RuntimeException("Second attempt failed"))
                .thenReturn("success");
<<<<<<< HEAD
        // Act
        String result = retryHandler.executeWithRetry(mockTask);
=======

        // Act
        String result = retryHandler.executeWithRetry(mockTask);

>>>>>>> origin/master
        // Assert
        assertEquals("success", result);
        verify(mockTask, times(3)).call();
    }

    @Test(expected = RuntimeException.class)
    public void executeWithRetry_shouldFailAfterMaxRetries() throws Exception {
        // Arrange
        when(mockTask.call()).thenThrow(new RuntimeException("Failed"));
<<<<<<< HEAD
=======

>>>>>>> origin/master
        // Act
        retryHandler.executeWithRetry(mockTask);
    }

    @Test
    public void splitIntoBatches_shouldCreateCorrectBatches() {
        // Arrange
        List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        int batchSize = 3;
<<<<<<< HEAD
        // Act
        List<List<Integer>> batches = RetryHandler.splitIntoBatches(items, batchSize);
=======

        // Act
        List<List<Integer>> batches = RetryHandler.splitIntoBatches(items, batchSize);

>>>>>>> origin/master
        // Assert
        assertEquals(3, batches.size());
        assertEquals(Arrays.asList(1, 2, 3), batches.get(0));
        assertEquals(Arrays.asList(4, 5, 6), batches.get(1));
        assertEquals(Arrays.asList(7), batches.get(2));
    }

    @Test
    public void waitForRetry_shouldRespectExponentialBackoff() throws Exception {
        // Arrange
        long startTime = System.currentTimeMillis();
<<<<<<< HEAD
        // Act
        retryHandler.waitForRetry(1); // Should wait initial delay
        long firstWaitTime = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        retryHandler.waitForRetry(2); // Should wait longer
        long secondWaitTime = System.currentTimeMillis() - startTime;
        // Assert
        assertTrue("Second wait should be longer than first", secondWaitTime > firstWaitTime);
    }
}
=======

        // Act
        retryHandler.waitForRetry(1); // Should wait initial delay
        long firstWaitTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        retryHandler.waitForRetry(2); // Should wait longer
        long secondWaitTime = System.currentTimeMillis() - startTime;

        // Assert
        assertTrue("Second wait should be longer than first", secondWaitTime > firstWaitTime);
    }
}
>>>>>>> origin/master
