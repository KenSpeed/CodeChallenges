package com.ken;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

class VWAPCalculator {
    private double priceVolumeSum = 0.0;  // Sum of Price * Volume
    private double volumeSum = 0.0;       // Sum of Volume

    public void addTrade(double price, double volume) {
        priceVolumeSum += price * volume;
        volumeSum += volume;
    }

    public double calculateVWAP() {
        if (volumeSum == 0) {
            return 0.0;
        }
        return priceVolumeSum / volumeSum;
    }
}

public class VWAPCalculatorApplication {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    /**
     * Obtains VWAP for each currency-pair in data stream
     *
     * @param trades: data stream in this format: [Timestamp, Currency-pair, Price, Volume]
     * @return Map<currency-pair: minimum of trade time in hour, VWAP>
     */
    public Map<String, Double> processTrades(String[][] trades) {
        Map<String, Map<LocalTime, VWAPCalculator>> vwapDataMap = new HashMap<>();

        for (String[] trade : trades) {
            validateTradeParams(trade);
            LocalTime tradeTime = LocalTime.parse(trade[0], TIME_FORMATTER);
            String currencyPair = trade[1];
            double price = Double.parseDouble(trade[2]);
            int volume = Integer.parseInt(trade[3]);
            VWAPCalculator vwapCalculator = vwapDataMap.computeIfAbsent(currencyPair, k -> new HashMap<>())
                                                       .computeIfAbsent(tradeTime.withMinute(0), k -> new VWAPCalculator());

            vwapCalculator.addTrade(price, volume);
        }

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
            Integer volumeValue = Integer.parseInt(volume);
            return volumeValue.compareTo(0) > 0;
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
