package com.ken;


import java.util.HashMap;
import java.util.Map;

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

    /**
     * Obtains VWAP for each currency-pair in data stream
     *
     * @param trades: data stream in this format: [Timestamp, Currency-pair, Price, Volume]
     * @return Map<currency-pair: minimum of trade time in hour, VWAP>
     */
    public Map<String, Double> processTrades(String[][] trades) {
        Map<String, Double> vwapResults = new HashMap<>();

        return vwapResults;
    }

}
