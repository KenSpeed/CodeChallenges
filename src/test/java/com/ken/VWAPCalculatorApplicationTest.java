package com.ken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VWAPCalculatorApplicationTest {

    private VWAPCalculatorApplication vwapCalculatorApplication;

    @BeforeEach
    public void setUp() {
        vwapCalculatorApplication = new VWAPCalculatorApplication();
    }

    /**
     * Test Case 1: edge case - empty trade
     */
    @Test
    public void test_empty_trade() {
        String[][] trades = {};

        Map<String, Double> expectedResults = new HashMap<>();
        vwapCalculatorApplication.processTrades(trades);
        Map<String, Double> actualResults = vwapCalculatorApplication.getAllVWAP();
        assertEquals(expectedResults, actualResults);
    }

    /**
     * Test Case 2: edge case - invalid trade stream format
     */
    @Test
    public void test_invalid_trade_stream_format() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1001", "100", "abc"}
        };

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> vwapCalculatorApplication.processTrades(trades));
        assertEquals("Invalid trade stream format", thrown.getMessage());
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

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> vwapCalculatorApplication.processTrades(trades));
        assertEquals("Invalid trade parameters", thrown.getMessage());
    }

    /**
     * Test Case 4: edge case - invalid currency pair
     */
    @Test
    public void test_invalid_currency_pair() {
        String[][] trades = {
                {"9:31 AM", "abc", "1.1001", "100"}
        };

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> vwapCalculatorApplication.processTrades(trades));
        assertEquals("Invalid trade parameters", thrown.getMessage());
    }

    /**
     * Test Case 5: edge case - zero trade price
     */
    @Test
    public void test_zero_trade_price() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "0", "100"}
        };

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> vwapCalculatorApplication.processTrades(trades));
        assertEquals("Invalid trade parameters", thrown.getMessage());
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

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> vwapCalculatorApplication.processTrades(trades));
        assertEquals("Invalid trade parameters", thrown.getMessage());
    }

    /**
     * Test Case 7: edge case - zero trade volume
     */
    @Test
    public void test_zero_trade_volume() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "110.002", "0"}
        };

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> vwapCalculatorApplication.processTrades(trades));
        assertEquals("Invalid trade parameters", thrown.getMessage());
    }

    /**
     * Test Case 8: edge case - invalid trade volume
     */
    @Test
    public void test_zero_invalid_volume() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "110.002", "abc"}
        };

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> vwapCalculatorApplication.processTrades(trades));
        assertEquals("Invalid trade parameters", thrown.getMessage());
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
        vwapCalculatorApplication.processTrades(trades);
        Map<String, Double> actualResults = vwapCalculatorApplication.getAllVWAP();
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

        vwapCalculatorApplication.processTrades(trades);
        Map<String, Double> actualResults = vwapCalculatorApplication.getAllVWAP();
        assertEquals(expectedResults.size(), actualResults.size());
        expectedResults.forEach((key, value) -> assertEquals(value, actualResults.get(key), 0.0001));
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
        expectedResults.put("EUR/USD 9:00 AM", 1.1000);
        expectedResults.put("EUR/USD 10:00 AM", 1.1001);
        expectedResults.put("EUR/USD 11:00 AM", 1.1002);

        vwapCalculatorApplication.processTrades(trades);
        Map<String, Double> actualResults = vwapCalculatorApplication.getAllVWAP();
        assertEquals(expectedResults.size(), actualResults.size());
        expectedResults.forEach((key, value) -> assertEquals(value, actualResults.get(key), 0.0001));
    }

    /**
     * Test Case 12: multiple trades for one currency pair with different times in continuous hours
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

        vwapCalculatorApplication.processTrades(trades);
        Map<String, Double> actualResults = vwapCalculatorApplication.getAllVWAP();
        assertEquals(expectedResults.size(), actualResults.size());
        expectedResults.forEach((key, value) -> assertEquals(value, actualResults.get(key), 0.0001));
    }

    /**
     * Test Case 13: multiple trades for two currency pairs at the same time
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

        vwapCalculatorApplication.processTrades(trades);
        Map<String, Double> actualResults = vwapCalculatorApplication.getAllVWAP();
        assertEquals(expectedResults.size(), actualResults.size());
        expectedResults.forEach((key, value) -> assertEquals(value, actualResults.get(key), 0.0001));
    }

    /**
     * Test Case 14: multiple trades for two currency pairs within an hour
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

        vwapCalculatorApplication.processTrades(trades);
        Map<String, Double> actualResults = vwapCalculatorApplication.getAllVWAP();
        assertEquals(expectedResults.size(), actualResults.size());
        expectedResults.forEach((key, value) -> assertEquals(value, actualResults.get(key), 0.0001));
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

        vwapCalculatorApplication.processTrades(trades);
        Map<String, Double> actualResults = vwapCalculatorApplication.getAllVWAP();
        assertEquals(expectedResults.size(), actualResults.size());
        expectedResults.forEach((key, value) -> assertEquals(value, actualResults.get(key), 0.0001));
    }

    /**
     * Test Case 16: edge case - large trades
     */
    @Test
    public void test_large_trade_stream() {
        String[][] trades = new String[10000][4];
        for (int i = 0; i < 10000; i++) {
            trades[i][0] = "9:31 AM";
            trades[i][1] = "EUR/USD";
            trades[i][2] = "1.1000";
            trades[i][3] = "100";
        }
        Map<String, Double> expectedResults = new HashMap<>();
        expectedResults.put("EUR/USD 9:00 AM", 1.1000);
        vwapCalculatorApplication.processTrades(trades);
        Map<String, Double> actualResults = vwapCalculatorApplication.getAllVWAP();
        expectedResults.forEach((key, value) -> assertEquals(value, actualResults.get(key), 0.0001));
    }

    /**
     * Test Case 17: edge case - boundary trade price
     */
    @Test
    public void test_boundary_values() {
        String[][] trades = {
                {"9:31 AM", "EUR/USD", "0.0001", "100"},
                {"9:31 AM", "EUR/USD", "999999.9999", "100"}
        };
        Map<String, Double> expectedResults = new HashMap<>();
        expectedResults.put("EUR/USD 9:00 AM", 500000.00005);
        vwapCalculatorApplication.processTrades(trades);
        Map<String, Double> actualResults = vwapCalculatorApplication.getAllVWAP();
        expectedResults.forEach((key, value) -> assertEquals(value, actualResults.get(key), 0.0001));
    }

    /**
     * Test Case 18: edge case - concurrent thread for multiple trades
     */
    @Test
    @Timeout(10)
    public void test_concurrent_thread_for_multiple_trades() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        String[][] trades1 = {
                {"9:31 AM", "EUR/USD", "1.1000", "100"},
                {"9:32 AM", "EUR/USD", "1.1001", "200"}
        };

        String[][] trades2 = {
                {"9:31 AM", "GBP/USD", "1.3000", "100"},
                {"9:32 AM", "GBP/USD", "1.3001", "200"}
        };

        String[][] trades3 = {
                {"9:31 AM", "EUR/USD", "1.1002", "100"},
                {"9:32 AM", "EUR/USD", "1.1003", "200"}
        };

        Runnable task1 = () -> vwapCalculatorApplication.processTrades(trades1);
        Runnable task2 = () -> vwapCalculatorApplication.processTrades(trades2);
        Runnable task3 = () -> vwapCalculatorApplication.processTrades(trades3);

        executor.execute(task1);
        executor.execute(task2);
        executor.execute(task3);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(1.10016, vwapCalculatorApplication.getVWAP("EUR/USD", "9:00 AM"), 0.00001);
        assertEquals(1.30006, vwapCalculatorApplication.getVWAP("GBP/USD", "9:00 AM"), 0.00001);
        assertEquals(1.10016, vwapCalculatorApplication.getVWAP("EUR/USD", "9:00 AM"), 0.00001);
    }

    /**
     *
     * Test Case 19: edge case - concurrent thread trade for one trade
     *
     * @throws InterruptedException
     */
    @Test
    @Timeout(10)
    public void test_concurrent_thread_for_one_trade() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        String[][] trades = {
                {"9:31 AM", "EUR/USD", "1.1000", "100"},
                {"9:32 AM", "EUR/USD", "1.1001", "200"},
                {"9:33 AM", "EUR/USD", "1.1002", "300"},
                {"9:34 AM", "EUR/USD", "1.1003", "400"},
                {"9:35 AM", "EUR/USD", "1.1004", "500"}
        };

        Runnable task = () -> vwapCalculatorApplication.processTrades(trades);

        for (int i = 0; i < 10; i++) {
            executor.execute(task);
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(1, vwapCalculatorApplication.getAllVWAP().size());
        assertEquals(1.1002, vwapCalculatorApplication.getVWAP("EUR/USD", "9:00 AM"), 0.0001);
    }

}