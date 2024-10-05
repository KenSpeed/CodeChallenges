package com.ken;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VWAPCalculatorApplicationTest {

    private final VWAPCalculatorApplication vwapCalculatorApplication = new VWAPCalculatorApplication();

    /**
     * Test Case 1: edge case - empty trade
     */
    @Test
    public void test_empty_trade() {
        String[][] trades = {};

        Map<String, Double> expectedResults = new HashMap<>();

        assertEquals(expectedResults, vwapCalculatorApplication.processTrades(trades));
    }

    /**
     * Test Case 2: edge case - invalid trade stream format
     */
    @Test
    public void test_invalid_trade_stream_format() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1001", "100", "abc"}
        };

        try {
            vwapCalculatorApplication.processTrades(trades);
            assert false;
        } catch (IllegalArgumentException e) {
            assert true;
            assertEquals("Invalid trade stream format", e.getMessage());
        }
    }

    /**
     * Test Case 3: edge case - invalid trade time
     */
    @Test
    public void test_invalid_trade_time() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1001", "100"},
                {"abc", "EUR/USD", "1.1001", "100"}
        };

        try {
            vwapCalculatorApplication.processTrades(trades);
            assert false;
        } catch (IllegalArgumentException e) {
            assert true;
            assertEquals("Invalid trade time format", e.getMessage());
        }
    }

    /**
     * Test Case 4: edge case - invalid currency pair
     */
    @Test
    public void test_invalid_currency_pair() {
        String[][] trades = {
                {"9:31 AM", "abc", "1.1001", "100"}
        };

        try {
            vwapCalculatorApplication.processTrades(trades);
            assert false;
        } catch (IllegalArgumentException e) {
            assert true;
            assertEquals("Invalid currency pair", e.getMessage());
        }
    }

    /**
     * Test Case 5: edge case - zero trade price
     */
    @Test
    public void test_zero_trade_price() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "0", "100"}
        };

        try {
            vwapCalculatorApplication.processTrades(trades);
            assert false;
        } catch (IllegalArgumentException e) {
            assert true;
            assertEquals("Invalid trade price", e.getMessage());
        }
    }

    /**
     * Test Case 6: edge case - invalid trade price
     */
    @Test
    public void test_invalid_price() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "abc", "100"},
                {"9:31 AM", "EUR/USD", "1.1001", "def"}
        };

        try {
            vwapCalculatorApplication.processTrades(trades);
            assert false;
        } catch (IllegalArgumentException e) {
            assert true;
            assertEquals("Invalid trade price", e.getMessage());
        }
    }

    /**
     * Test Case 7: edge case - zero trade volume
     */
    @Test
    public void test_zero_trade_volume() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "110.002", "0"}
        };

        try {
            vwapCalculatorApplication.processTrades(trades);
            assert false;
        } catch (IllegalArgumentException e) {
            assert true;
            assertEquals("Invalid trade volume", e.getMessage());
        }
    }

    /**
     * Test Case 8: edge case - invalid trade volume
     */
    @Test
    public void test_zero_invalid_volume() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "110.002", "abc"}
        };

        try {
            vwapCalculatorApplication.processTrades(trades);
            assert false;
        } catch (IllegalArgumentException e) {
            assert true;
            assertEquals("Invalid trade volume", e.getMessage());
        }
    }

    /**
     * Test Case 9: single trade
     */
    @Test
    public void test_single_trade() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1000", "100"}
        };
        Map<String, Double> expectedResults = new HashMap<>();
        expectedResults.put("EUR/USD 9:00 AM", 1.1000);
        Map<String, Double> actualResults = vwapCalculatorApplication.processTrades(trades);
        assertEquals(expectedResults, actualResults);
    }

    /**
     * Test Case 10: multiple trades for one currency pair at the same time
     */
    @Test
    public void test_multiple_trades_for_one_currency_pair_at_the_same_time() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1000", "100"},
                {"9:31 AM", "EUR/USD", "1.1001", "200"},
                {"9:31 AM", "EUR/USD", "1.1002", "300"}
        };

        Map<String, Double> expectedResults = new HashMap<>();
        expectedResults.put("EUR/USD 9:00 AM", 1.1001);

        Map<String, Double> actualResults = vwapCalculatorApplication.processTrades(trades);
        assertEquals(expectedResults.size(), actualResults.size());
        for (Map.Entry<String, Double> entry : expectedResults.entrySet()) {
            assertEquals(entry.getValue(), actualResults.get(entry.getKey()), 0.0001);
        }
    }

    /**
     * Test Case 11: multiple trades for one currency pair in continuous hours
     */
    @Test
    public void test_multiple_trades_for_one_currency_pair_in_continuous_hours() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1000", "100"},
                {"10:31 AM", "EUR/USD", "1.1001", "200"},
                {"11:31 AM", "EUR/USD", "1.1002", "300"}
        };

        Map<String, Double> expectedResults = new HashMap<>();
        expectedResults.put("EUR/USD 9:00 AM", 1.1001);
        expectedResults.put("EUR/USD 10:00 AM", 1.1001);
        expectedResults.put("EUR/USD 11:00 AM", 1.1002);

        Map<String, Double> actualResults = vwapCalculatorApplication.processTrades(trades);
        assertEquals(expectedResults.size(), actualResults.size());
        for (Map.Entry<String, Double> entry : expectedResults.entrySet()) {
            assertEquals(entry.getValue(), actualResults.get(entry.getKey()), 0.0001);
        }
    }

    /**
     * Test Case 12: multiple trades for two currency pairs at the same time
     */
    @Test
    public void test_multiple_trades_for_two_currency_pairs_at_the_same_time() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1000", "100"},
                {"9:31 AM", "EUR/USD", "1.1001", "200"},
                {"9:31 AM", "USD/JPY", "110.00", "500"},
                {"9:31 AM", "EUR/USD", "1.1002", "300"},
                {"9:31 AM", "USD/JPY", "110.01", "600"}
        };

        Map<String, Double> expectedResults = new HashMap<>();
        expectedResults.put("EUR/USD 9:00 AM", 1.1001);
        expectedResults.put("USD/JPY 9:00 AM", 110.0054);

        Map<String, Double> actualResults = vwapCalculatorApplication.processTrades(trades);
        assertEquals(expectedResults.size(), actualResults.size());
        for (Map.Entry<String, Double> entry : expectedResults.entrySet()) {
            assertEquals(entry.getValue(), actualResults.get(entry.getKey()), 0.0001);
        }
    }

    /**
     * Test Case 13: multiple trades for two currency pairs within an hour
     */
    @Test
    public void test_multiple_trades_for_two_currency_pairs_within_an_hour() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1000", "100"},
                {"9:32 AM", "EUR/USD", "1.1001", "200"},
                {"9:33 AM", "USD/JPY", "110.00", "500"},
                {"9:34 AM", "EUR/USD", "1.1002", "300"},
                {"9:35 AM", "USD/JPY", "110.01", "600"}
        };

        Map<String, Double> expectedResults = new HashMap<>();
        expectedResults.put("EUR/USD 9:00 AM", 1.1001);
        expectedResults.put("USD/JPY 9:00 AM", 110.0054);

        Map<String, Double> actualResults = vwapCalculatorApplication.processTrades(trades);
        assertEquals(expectedResults.size(), actualResults.size());
        for (Map.Entry<String, Double> entry : expectedResults.entrySet()) {
            assertEquals(entry.getValue(), actualResults.get(entry.getKey()), 0.0001);
        }
    }


    /**
     * Test Case 14: multiple trades for one currency pair with continuous hours
     */
    @Test
    public void test_multiple_trades_for_one_currency_pair_with_continuous_hours() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1000", "100"},
                {"9:31 AM", "EUR/USD", "1.1001", "200"},
                {"10:31 AM", "EUR/USD", "1.1005", "300"},
                {"10:31 AM", "EUR/USD", "1.1006", "400"}
        };

        Map<String, Double> expectedResults = new HashMap<>();
        expectedResults.put("EUR/USD 9:00 AM", 1.1001);
        expectedResults.put("EUR/USD 10:00 AM", 1.10055);

        Map<String, Double> actualResults = vwapCalculatorApplication.processTrades(trades);
        assertEquals(expectedResults.size(), actualResults.size());
        for (Map.Entry<String, Double> entry : expectedResults.entrySet()) {
            assertEquals(entry.getValue(), actualResults.get(entry.getKey()), 0.0001);
        }
    }

    /**
     * Test Case 15: multiple trades for one currency pair with broken hours
     */
    @Test
    public void test_multiple_trades_for_one_currency_pair_with_broken_hours() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1000", "100"},
                {"9:31 AM", "EUR/USD", "1.1001", "200"},
                {"11:31 AM", "EUR/USD", "1.1005", "300"}
        };

        Map<String, Double> expectedResults = new HashMap<>();
        expectedResults.put("EUR/USD 9:00 AM", 1.1001);
        expectedResults.put("EUR/USD 11:00 AM", 1.1005);

        Map<String, Double> actualResults = vwapCalculatorApplication.processTrades(trades);
        assertEquals(expectedResults.size(), actualResults.size());
        for (Map.Entry<String, Double> entry : expectedResults.entrySet()) {
            assertEquals(entry.getValue(), actualResults.get(entry.getKey()), 0.0001);
        }
    }


}