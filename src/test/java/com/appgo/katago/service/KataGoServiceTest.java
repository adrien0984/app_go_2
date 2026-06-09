package com.appgo.katago.service;

import com.appgo.katago.config.KataGoProperties;
import com.appgo.katago.port.KataGoException;
import com.appgo.katago.port.KataGoSuggestionPort;
import com.appgo.katago.port.SuggestionRequest;
import com.appgo.katago.port.SuggestionResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KataGoServiceTest {

    private KataGoService service;
    private KataGoProperties properties;
    private MockKataGoAdapter mockAdapter;

    @BeforeEach
    void setUp() {
        properties = new KataGoProperties();
        properties.setTimeout(1000);
        properties.setRetryCount(2);
        properties.setRetryDelayMs(100);
        properties.setEnabled(true);

        mockAdapter = new MockKataGoAdapter();
        service = new KataGoService(mockAdapter, properties);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    void testSuccessfulSuggestion() {
        SuggestionRequest request = new SuggestionRequest(new int[19][19], "BLACK", 19);
        SuggestionResponse response = service.getSuggestion(request);

        assertNotNull(response);
        assertEquals(5, response.getSuggestedRow());
        assertEquals(5, response.getSuggestedCol());
    }

    @Test
    void testDisabledService() {
        properties.setEnabled(false);
        SuggestionRequest request = new SuggestionRequest(new int[19][19], "BLACK", 19);
        SuggestionResponse response = service.getSuggestion(request);

        assertNull(response);
    }

    @Test
    void testTimeoutAndRetry() {
        mockAdapter.setDelay(5000); // Very long delay to guarantee timeout
        properties.setTimeout(100);
        properties.setRetryCount(1); // Reduce retries to speed up test

        SuggestionRequest request = new SuggestionRequest(new int[19][19], "BLACK", 19);
        SuggestionResponse response = service.getSuggestion(request);

        // Should fail after retries and return null (no fallback)
        assertNull(response, "Should return null after timeout and retries");
    }

    @Test
    void testRetryOnError() {
        mockAdapter.setThrowOnAttempt(0); // Fail first call
        properties.setRetryCount(2);

        SuggestionRequest request = new SuggestionRequest(new int[19][19], "BLACK", 19);
        SuggestionResponse response = service.getSuggestion(request);

        assertNotNull(response, "Should succeed after retry");
        assertEquals(2, mockAdapter.getCallCount(), "Should have retried once");
    }

    @Test
    void testFallbackToLastValidSuggestion() {
        SuggestionRequest request = new SuggestionRequest(new int[19][19], "BLACK", 19);

        // First call succeeds
        SuggestionResponse firstResponse = service.getSuggestion(request);
        assertNotNull(firstResponse);

        // Second call fails but should fallback
        mockAdapter.setAlwaysThrow(true);
        SuggestionResponse fallbackResponse = service.getSuggestion(request);

        assertNotNull(fallbackResponse, "Should return fallback suggestion");
        assertEquals(firstResponse.getSuggestedRow(), fallbackResponse.getSuggestedRow());
        assertEquals(firstResponse.getSuggestedCol(), fallbackResponse.getSuggestedCol());
    }

    @Test
    void testNoFallbackWhenNoPriorSuccess() {
        mockAdapter.setAlwaysThrow(true);
        properties.setRetryCount(1);

        SuggestionRequest request = new SuggestionRequest(new int[19][19], "BLACK", 19);
        SuggestionResponse response = service.getSuggestion(request);

        assertNull(response, "Should return null when no prior success and fallback unavailable");
    }

    /**
     * Mock adapter for testing
     */
    private static class MockKataGoAdapter implements KataGoSuggestionPort {

        private int callCount = 0;
        private long delay = 0;
        private int throwOnAttempt = -1;
        private boolean alwaysThrow = false;

        @Override
        public SuggestionResponse getSuggestion(SuggestionRequest request) throws KataGoException {
            callCount++;

            if (alwaysThrow) {
                throw new KataGoException("Mock error");
            }

            if (throwOnAttempt == callCount - 1) {
                throw new KataGoException("Mock error on attempt " + callCount);
            }

            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new KataGoException("Sleep interrupted", e);
                }
            }

            return new SuggestionResponse(5, 5, 0.5, delay);
        }

        void setDelay(long delayMs) {
            this.delay = delayMs;
        }

        void setThrowOnAttempt(int attempt) {
            this.throwOnAttempt = attempt;
        }

        void setAlwaysThrow(boolean alwaysThrow) {
            this.alwaysThrow = alwaysThrow;
        }

        int getCallCount() {
            return callCount;
        }
    }
}
