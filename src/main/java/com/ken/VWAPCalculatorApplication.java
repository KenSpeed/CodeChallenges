package com.ken;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

class VWAPCalculator {
    private final DoubleAdder priceVolumeSum = new DoubleAdder();  // Sum of Price * Volume
    private final AtomicLong volumeSum = new AtomicLong();       // Sum of Volume

    public void addTrade(double price, long volume) {
        priceVolumeSum.add(price * volume);
        volumeSum.addAndGet(volume);
    }

    public double calculateVWAP() {
        long volume = volumeSum.get();
        return (volume == 0) ? 0.0 : priceVolumeSum.sum() / volume;
    }
}

public class VWAPCalculatorApplication {

    private static final Logger LOGGER = Logger.getLogger(VWAPCalculatorApplication.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private final ConcurrentHashMap<String, ConcurrentHashMap<LocalTime, VWAPCalculator>> vwapDataMap = new ConcurrentHashMap<>();
    private static final Set<String> VALID_CURRENCIES = Set.of(
            "EUR/USD", "USD/JPY", "GBP/USD", "USD/CHF", "AUD/USD", "USD/CAD",
            "NZD/USD", "EUR/GBP", "EUR/JPY", "GBP/JPY", "AUD/JPY", "EUR/AUD",
            "CHF/JPY", "GBP/CHF", "USD/TRY", "USD/ZAR", "USD/SGD", "USD/MXN",
            "USD/PLN", "EUR/TRY", "EUR/HUF"
    );

    /**
     * Processes the incoming trades and calculates VWAP for each unique currency pair.
     *
     * @param trades: data stream in this format: [Timestamp, Currency-pair, Price, Volume]
     */
    public void processTrades(String[][] trades) {
        for (String[] trade : trades) {
            try {
                processSingleTrade(trade);
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Skipping invalid trade: {0} - {1}", new Object[]{String.join(", ", trade), e.getMessage()});
            }
        }
    }

    private void processSingleTrade(String[] trade) {
        validateTradeParams(trade);
        LocalTime tradeTime = LocalTime.parse(trade[0], TIME_FORMATTER).withMinute(0);
        String currencyPair = trade[1];
        double price = Double.parseDouble(trade[2]);
        long volume = Long.parseLong(trade[3]);

        vwapDataMap
                .computeIfAbsent(currencyPair, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(tradeTime, k -> new VWAPCalculator())
                .addTrade(price, volume);
    }

    /**
     * Gets the calculated VWAP for all currency pairs in the dataset.
     *
     * @return A map containing the currency pair and VWAP value for each time interval.
     */
    public Map<String, Double> getAllVWAP() {
        Map<String, Double> vwapResults = new HashMap<>();

        vwapDataMap.forEach((currencyPair, vwapDataForCurrencyPair) ->
                vwapDataForCurrencyPair.forEach((time, vwapCalculator) ->
                        vwapResults.put(currencyPair + " " + time.format(TIME_FORMATTER), vwapCalculator.calculateVWAP())
                )
        );

        return vwapResults;
    }

    /**
     * Validates the parameters of a trade.
     *
     * @param trade A trade represented as an array of strings.
     */
    private void validateTradeParams(String[] trade) {
        if (trade.length != 4) {
            throw new IllegalArgumentException("Invalid trade stream format");
        }

        String time = trade[0];
        String currency = trade[1];
        String price = trade[2];
        String volume = trade[3];

        if (!isValidTime(time)) {
            throw new IllegalArgumentException("Invalid trade time format");
        }
        if (!isValidCurrency(currency)) {
            throw new IllegalArgumentException("Invalid currency pair");
        }
        if (!isValidPrice(price)) {
            throw new IllegalArgumentException("Invalid trade price");
        }
        if (!isValidVolume(volume)) {
            throw new IllegalArgumentException("Invalid trade volume");
        }
    }

    /**
     * Checks if the trade time is valid.
     *
     * @param time The trade time as a string.
     * @return True if the time is valid, false otherwise.
     */
    private boolean isValidTime(String time) {
        try {
            LocalTime.parse(time, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Checks if the currency pair is valid.
     *
     * @param currency The currency pair as a string.
     * @return True if the currency pair is valid, false otherwise.
     */
    private boolean isValidCurrency(String currency) {
        return VALID_CURRENCIES.contains(currency);
    }

    /**
     * Checks if the trade volume is valid.
     *
     * @param volume The trade volume as a string.
     * @return True if the volume is valid, false otherwise.
     */
    private boolean isValidVolume(String volume) {
        try {
            return Long.parseLong(volume) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the trade price is valid.
     *
     * @param price The trade price as a string.
     * @return True if the price is valid, false otherwise.
     */
    private boolean isValidPrice(String price) {
        try {
            return Double.parseDouble(price) > 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
