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

    public void reset() {
        priceVolumeSum.reset();
        volumeSum.set(0);
    }
}

public class VWAPCalculatorApplication {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private final ConcurrentHashMap<String, ConcurrentHashMap<LocalTime, VWAPCalculator>> vwapDataMap = new ConcurrentHashMap<>();
    private static final Set<String> VALID_CURRENCIES = Set.of(
            "EUR/USD", "USD/JPY", "GBP/USD", "USD/CHF", "AUD/USD", "USD/CAD",
            "NZD/USD", "EUR/GBP", "EUR/JPY", "GBP/JPY", "AUD/JPY", "EUR/AUD",
            "CHF/JPY", "GBP/CHF", "USD/TRY", "USD/ZAR", "USD/SGD", "USD/MXN",
            "USD/PLN", "EUR/TRY", "EUR/HUF"
    );

    /**
     * Obtains VWAP for each currency-pair in data stream
     *
     * @param trades: data stream in this format: [Timestamp, Currency-pair, Price, Volume]
     */
    public void processTrades(String[][] trades) {
        for (String[] trade : trades) {
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
    }

    public Map<String, Double> getAllVWAP() {
        Map<String, Double> vwapResults = new HashMap<>();

        vwapDataMap.forEach((currencyPair, vwapDataForCurrencyPair) ->
                vwapDataForCurrencyPair.forEach((time, vwapCalculator) ->
                        vwapResults.put(currencyPair + " " + time.format(TIME_FORMATTER), vwapCalculator.calculateVWAP())
                )
        );

        return vwapResults;
    }

    public double getVWAP(String currencyPair, String time) {
        if (!isValidCurrency(currencyPair)) {
            throw new IllegalArgumentException("Invalid currency pair");
        }

        LocalTime tradeTime = LocalTime.parse(time, TIME_FORMATTER).withMinute(0);
        VWAPCalculator data = vwapDataMap.getOrDefault(currencyPair, new ConcurrentHashMap<>()).get(tradeTime);
        return (data == null) ? 0.0 : data.calculateVWAP();
    }

    private void validateTradeParams(String[] trade) {
        if (trade.length != 4) {
            throw new IllegalArgumentException("Invalid trade stream format");
        }

        String time = trade[0];
        String currency = trade[1];
        String price = trade[2];
        String volume = trade[3];

        if (!isValidTime(time) || !isValidCurrency(currency) || !isValidPrice(price) || !isValidVolume(volume)) {
            throw new IllegalArgumentException("Invalid trade parameters");
        }
    }

    private boolean isValidTime(String time) {
        try {
            LocalTime.parse(time, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidCurrency(String currency) {
        return VALID_CURRENCIES.contains(currency);
    }

    private boolean isValidVolume(String volume) {
        try {
            return Long.parseLong(volume) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidPrice(String price) {
        try {
            return Double.parseDouble(price) > 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
