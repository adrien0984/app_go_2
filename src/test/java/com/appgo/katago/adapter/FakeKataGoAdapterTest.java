package com.appgo.katago.adapter;

import com.appgo.katago.port.KataGoException;
import com.appgo.katago.port.SuggestionRequest;
import com.appgo.katago.port.SuggestionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FakeKataGoAdapterTest {

    private FakeKataGoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FakeKataGoAdapter();
    }

    @Test
    void testSuggestionNotNull() throws KataGoException {
        SuggestionRequest request = new SuggestionRequest(new int[19][19], "BLACK", 19);
        SuggestionResponse response = adapter.getSuggestion(request);

        assertNotNull(response);
        assertNotNull(response.getSuggestedRow());
        assertNotNull(response.getSuggestedCol());
    }

    @Test
    void testSuggestionConfidence() throws KataGoException {
        SuggestionRequest request = new SuggestionRequest(new int[19][19], "BLACK", 19);
        SuggestionResponse response = adapter.getSuggestion(request);

        assertEquals(0.75, response.getConfidence());
    }

    @Test
    void testSuggestionsAreDeterministic() throws KataGoException {
        int[][] board = new int[19][19];
        SuggestionRequest request = new SuggestionRequest(board, "BLACK", 19);

        // Reset and get first 4 suggestions (corners)
        adapter.resetCallCount();
        SuggestionResponse r1 = adapter.getSuggestion(request);
        SuggestionResponse r2 = adapter.getSuggestion(request);
        SuggestionResponse r3 = adapter.getSuggestion(request);
        SuggestionResponse r4 = adapter.getSuggestion(request);

        assertEquals(18, r1.getSuggestedRow());
        assertEquals(18, r1.getSuggestedCol());

        assertEquals(18, r2.getSuggestedRow());
        assertEquals(0, r2.getSuggestedCol());

        assertEquals(0, r3.getSuggestedRow());
        assertEquals(0, r3.getSuggestedCol());

        assertEquals(0, r4.getSuggestedRow());
        assertEquals(18, r4.getSuggestedCol());
    }

    @Test
    void testSuggestionsAfterCornersForSize19() throws KataGoException {
        int[][] board = new int[19][19];
        SuggestionRequest request = new SuggestionRequest(board, "BLACK", 19);

        adapter.resetCallCount();
        // Skip first 4 corners
        for (int i = 0; i < 4; i++) {
            adapter.getSuggestion(request);
        }

        // Next should be star points
        SuggestionResponse r5 = adapter.getSuggestion(request);
        assertThat19StarPoint(r5);
    }

    @Test
    void testResponseTimeIsRecorded() throws KataGoException {
        SuggestionRequest request = new SuggestionRequest(new int[19][19], "BLACK", 19);
        SuggestionResponse response = adapter.getSuggestion(request);

        assertTrue(response.getTimeMs() >= 0);
    }

    @Test
    void testMultipleSuggestionsStable() throws KataGoException {
        int[][] board = new int[19][19];
        SuggestionRequest request = new SuggestionRequest(board, "BLACK", 19);

        // Get 100 suggestions and verify no exceptions
        for (int i = 0; i < 100; i++) {
            SuggestionResponse response = adapter.getSuggestion(request);
            assertNotNull(response);
            assertTrue(response.getSuggestedRow() >= 0 && response.getSuggestedRow() < 19);
            assertTrue(response.getSuggestedCol() >= 0 && response.getSuggestedCol() < 19);
        }
    }

    @Test
    void testSize9Board() throws KataGoException {
        int[][] board = new int[9][9];
        SuggestionRequest request = new SuggestionRequest(board, "BLACK", 9);

        adapter.resetCallCount();
        // First 4 corners
        SuggestionResponse r1 = adapter.getSuggestion(request);
        assertEquals(8, r1.getSuggestedRow());
        assertEquals(8, r1.getSuggestedCol());

        // After 4 corners, should use center for 9x9
        SuggestionResponse r5 = adapter.getSuggestion(request);
        SuggestionResponse r6 = adapter.getSuggestion(request);
        SuggestionResponse r7 = adapter.getSuggestion(request);
        SuggestionResponse r8 = adapter.getSuggestion(request);
        SuggestionResponse r9 = adapter.getSuggestion(request);
        assertEquals(4, r9.getSuggestedRow());
        assertEquals(4, r9.getSuggestedCol());
    }

    private void assertThat19StarPoint(SuggestionResponse response) {
        int[][] starPoints = {
                {3, 3}, {3, 9}, {3, 15},
                {9, 3}, {9, 9}, {9, 15},
                {15, 3}, {15, 9}, {15, 15}
        };

        boolean isStarPoint = false;
        for (int[] point : starPoints) {
            if (response.getSuggestedRow() == point[0] && response.getSuggestedCol() == point[1]) {
                isStarPoint = true;
                break;
            }
        }
        assertTrue(isStarPoint, "Response should be a star point");
    }
}
