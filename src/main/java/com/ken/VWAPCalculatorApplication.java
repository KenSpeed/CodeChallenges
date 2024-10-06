package com.ken;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
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
        if (volume == 0) {
            return 0.0;
        }
        return priceVolumeSum.sum() / volume;
    }

    public void reset() {
        priceVolumeSum.reset();
        volumeSum.set(0);
    }
}

public class VWAPCalculatorApplication {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private final ConcurrentHashMap<String, Map<LocalTime, VWAPCalculator>> vwapDataMap = new ConcurrentHashMap<>();

    /**
     * Obtains VWAP for each currency-pair in data stream
     *
     * @param trades: data stream in this format: [Timestamp, Currency-pair, Price, Volume]
     * @return Map<currency-pair: minimum of trade time in hour, VWAP>
     */
    public void processTrades(String[][] trades) {

        for (String[] trade : trades) {
            validateTradeParams(trade);
            LocalTime tradeTime = LocalTime.parse(trade[0], TIME_FORMATTER);
            String currencyPair = trade[1];
            double price = Double.parseDouble(trade[2]);
            long volume = Long.parseLong(trade[3]);
            // tradeTime.withMinute(0): set the output format, which can be decided by business owner to show
            vwapDataMap.computeIfAbsent(currencyPair, k -> new ConcurrentHashMap<>())
                       .computeIfAbsent(tradeTime.withMinute(0), k -> new VWAPCalculator())
                       .addTrade(price, volume);
        }

    }

    public Map<String, Double> getAllVWAP() {
        Map<String, Double> vwapResults = new HashMap<>();

        for (Map.Entry<String, Map<LocalTime, VWAPCalculator>> entry : vwapDataMap.entrySet()) {
            String currencyPair = entry.getKey();
            Map<LocalTime, VWAPCalculator> vwapDataForCurrencyPair = entry.getValue();

            for (Map.Entry<LocalTime, VWAPCalculator> vwapEntry : vwapDataForCurrencyPair.entrySet()) {
                LocalTime time = vwapEntry.getKey();
                VWAPCalculator vwapDataForTime = vwapEntry.getValue();

                double vwap = vwapDataForTime.calculateVWAP();
                vwapResults.put(currencyPair + " " + time.format(TIME_FORMATTER), vwap);
            }
        }

        return vwapResults;
    }

    public double getVWAP(String currencyPair, String time) {
        if (!isValidCurrency(currencyPair)) {
            throw new IllegalArgumentException("Invalid currency pair");
        }

        if (!isValidTime(time)) {
            throw new IllegalArgumentException("Invalid trade time format");
        }

        LocalTime tradeTime = LocalTime.parse(time, TIME_FORMATTER).withMinute(0);
        VWAPCalculator data = vwapDataMap.getOrDefault(currencyPair, new ConcurrentHashMap<>()).get(tradeTime);
        return data == null ? 0.0 : data.calculateVWAP();
    }

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

    private boolean isValidTime(String time) {
        try {
            LocalTime.parse(time, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidCurrency(String currency) {
        // add currency pairs manually
        // it can be initiated by application file with cloud platform
        Set<String> validCurrencies = new HashSet<>();
        validCurrencies.add("EUR/USD");
        validCurrencies.add("USD/JPY");
        validCurrencies.add("GBP/USD");
        validCurrencies.add("USD/CHF");
        validCurrencies.add("AUD/USD");
        validCurrencies.add("USD/CAD");
        validCurrencies.add("NZD/USD");
        validCurrencies.add("EUR/GBP");
        validCurrencies.add("EUR/JPY");
        validCurrencies.add("GBP/JPY");
        validCurrencies.add("AUD/JPY");
        validCurrencies.add("EUR/AUD");
        validCurrencies.add("CHF/JPY");
        validCurrencies.add("GBP/CHF");
        validCurrencies.add("USD/TRY");
        validCurrencies.add("USD/ZAR");
        validCurrencies.add("USD/SGD");
        validCurrencies.add("USD/MXN");
        validCurrencies.add("USD/PLN");
        validCurrencies.add("EUR/TRY");
        validCurrencies.add("EUR/HUF");

        return validCurrencies.contains(currency);
    }

    private boolean isValidVolume(String volume) {
        try {
            Long volumeValue = Long.parseLong(volume);
            return volumeValue.compareTo(0L) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidPrice(String price) {
        try {
            Double priceValue = Double.parseDouble(price);
            return priceValue.compareTo(0.0) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
