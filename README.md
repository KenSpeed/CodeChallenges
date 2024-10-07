# VWAP Calculator Application


## Overview

This is a Java implementation of a Volume-Weighted Average Price (VWAP) calculator. The calculator takes in a series of trades and calculates the VWAP for each currency pair.

## Usage

To use the VWAP calculator, simply create a new instance of the `VWAPCalculatorApplication` class and call the `processTrades` method, passing in a 2D array of trades. Each trade is represented as a string array with the following format:
* `time`: The time of the trade in the format "hh:mm a"
* `currency-pair`: The currency pair of the trade (e.g. "EUR/USD")
* `price`: The price of the trade as a decimal value
* `volume`: The quantity of the trade as a decimal value

The `processTrades` method returns a map of currency pairs to their corresponding VWAP values.

## Example Use Case

```java
String[][] trades = {
    {"9:31 AM", "EUR/USD", "1.1000", "100"},
    {"9:31 AM", "EUR/USD", "1.1001", "200"},
    {"10:31 AM", "EUR/USD", "1.1002", "300"}
};

VWAPCalculatorApplication calculator = new VWAPCalculatorApplication();
calculator.processTrades(trades);
Map<String, Double> vwapResults = calculator.getAllVWAP();

```

## Example VWAP Results and Explanation

```
EUR/USD 9:00 AM, 1.1001
EUR/USD 10:00 AM, 1.1001
```

## Testing Cases

The VWAP calculator has been tested with a series of unit tests to ensure its accuracy and reliability. The tests cover a range of scenarios, including:

### Test Case 1: edge case - empty trade

* Input: []
* Expected Output: []

### Test Case 2: edge case - invalid trade stream format

* Input:
    + ["9:31 AM", "EUR/USD", "1.1001", "100", "abc"] (invalid trade stream)
* Expected Output: exception with error message: `Invalid trade stream format`

### Test Case 3: edge case - invalid trade time

* Input:
    + ["abc", "EUR/USD", "1.1001", "100"] (invalid trade time)
* Expected Output: exception with error message: `Invalid trade time format`

### Test Case 4: edge case - invalid currency pair

* Input:
    + ["9:31 AM", "abc", "1.1001", "100"] (invalid currency pair)
* Expected Output: exception with error message: `Invalid currency pair`

### Test Case 5: edge case - zero trade price

* Input:
  + ["9:31 AM", "EUR/USD", "0", "100"] (invalid trade price)
* Expected Output: exception with error message: `Invalid trade price`

### Test Case 6: edge case - invalid trade price

* Input:
  + ["9:31 AM", "EUR/USD", "abc", "100"] (invalid trade price)
* Expected Output: exception with error message: `Invalid trade price`

### Test Case 7: edge case - zero trade volume

* Input:
  + ["9:31 AM", "EUR/USD", "110.002", "0"] (invalid trade volume)
* Expected Output: exception with error message: `Invalid trade volume`

### Test Case 8: edge case - invalid trade volume

* Input:
  + ["9:31 AM", "EUR/USD", "110.002", "abc"] (invalid trade volume)
* Expected Output: exception with error message: `Invalid trade volume`

### Test Case 9: happy path - single trade

* Input:
    + ["9:31 AM", "EUR/USD", "1.1000", "100"]
* Expected Output:
    + ["EUR/USD 9:00 AM", 1.1000] (VWAP for EUR/USD in the first hour)

### Test Case 10: happy path - multiple trades for one currency pair at the same time

* Input:
  + ["9:31 AM", "EUR/USD", "1.1000", "100"]
  + ["9:31 AM", "EUR/USD", "1.1001", "200"]
  + ["9:31 AM", "EUR/USD", "1.1002", "300"]
* Expected Output:
  + ["EUR/USD 9:00 AM", 1.1000] (VWAP for EUR/USD in the first hour)

### Test Case 11: happy path - multiple trades for one currency pair in continuous hours

* Input:
  + ["9:31 AM", "EUR/USD", "1.1000", "100"]
  + ["10:31 AM", "EUR/USD", "1.1001", "200"]
  + ["11:31 AM", "EUR/USD", "1.1002", "300"]
* Expected Output:
  + ["EUR/USD 9:00 AM", 1.1000] (VWAP for EUR/USD in the first hour)
  + ["EUR/USD 10:00 AM", 1.1001] (VWAP for EUR/USD in the second hour)
  + ["EUR/USD 11:00 AM", 1.1002] (VWAP for EUR/USD in the third hour)

### Test Case 12: happy path - multiple trades for one currency pair with different times in continuous hours

* Input:
  + ["9:31 AM", "EUR/USD", "1.1000", "100"]
  + ["9:31 AM", "EUR/USD", "1.1001", "200"]
  + ["10:31 AM", "EUR/USD", "1.1005", "300"]
  + ["10:31 AM", "EUR/USD", "1.1006", "400"]
* Expected Output:
  + ["EUR/USD 9:00 AM", 1.1001] (VWAP for EUR/USD in the first hour)
  + ["EUR/USD 10:00 AM", 1.10055] (VWAP for EUR/USD in the second hour)

### Test Case 13: happy path - multiple trades for two currency pairs at the same time

* Input:
  + ["9:31 AM", "EUR/USD", "1.1000", "100"]
  + ["9:31 AM", "EUR/USD", "1.1001", "200"]
  + ["9:31 AM", "USD/JPY", "110.00", "500"]
  + ["9:31 AM", "EUR/USD", "1.1002", "300"]
  + ["9:31 AM", "USD/JPY", "110.01", "600"]
* Expected Output:
  + ["EUR/USD 9:00 AM", 1.1001] (VWAP for EUR/USD)
  + ["USD/JPY 9:00 AM", 110.0054] (VWAP for USD/JPY)

### Test Case 14: happy path - multiple trades for two currency pairs within an hour

* Input:
  + ["9:31 AM", "EUR/USD", "1.1000", "100"]
  + ["9:32 AM", "EUR/USD", "1.1001", "200"]
  + ["9:33 AM", "USD/JPY", "110.00", "500"]
  + ["9:34 AM", "EUR/USD", "1.1002", "300"]
  + ["9:35 AM", "USD/JPY", "110.01", "600"]
* Expected Output:
  + ["EUR/USD 9:00 AM", 1.1001] (VWAP for EUR/USD in an hour)
  + ["USD/JPY 9:00 AM", 110.0054] (VWAP for USD/JPY in an hour)

### Test Case 15: happy path - multiple trades for one currency pair with broken hours

* Input:
  + ["9:31 AM", "EUR/USD", "1.1000", "100"]
  + ["9:31 AM", "EUR/USD", "1.1001", "200"]
  + ["11:31 AM", "EUR/USD", "1.1005", "300"]
* Expected Output:
  + ["EUR/USD 9:00 AM", 1.1001] (VWAP for EUR/USD in the first hour)
  + ["EUR/USD 11:00 AM", 1.1005] (VWAP for EUR/USD in the next hour)

### Test Case 16: edge case - large trades

* Input:
  + ["9:31 AM", "EUR/USD", "1.1001", "200"]
  + .....with 10000 trade data
* Expected Output:
  + ["EUR/USD 9:00 AM", 1.1001] (VWAP for EUR/USD)

### Test Case 17: edge case - boundary trade price

* Input:
  + ["9:31 AM", "EUR/USD", "0.0001", "100"]
  + ["9:31 AM", "EUR/USD", "999999.9999", "100"]
* Expected Output:
  + ["EUR/USD 9:00 AM", 500000.00005] (VWAP for EUR/USD)

### Test Case 18: edge case - concurrent thread for multiple trades

* Input:
10 threads to execute three trades
trade1:
  + ["9:31 AM", "EUR/USD", "1.1000", "100"]
  + ["9:32 AM", "EUR/USD", "1.1001", "200"]
trade2:
  + ["9:31 AM", "GBP/USD", "1.3000", "100"]
  + ["9:32 AM", "GBP/USD", "1.3001", "200"]
trade3:
  + ["9:31 AM", "EUR/USD", "1.1002", "100"]
  + ["9:32 AM", "EUR/USD", "1.1003", "200"]
* Expected Output:
  + ["EUR/USD 9:00 AM", 1.10016] (VWAP for EUR/USD)
  + ["EUR/USD 9:00 AM", 1.30016] (VWAP for GBP/USD)

### Test Case 19: edge case - concurrent thread trade for one trade

* Input:
10 threads to execute one trade
  + ["9:31 AM", "EUR/USD", "1.1000", "100"]
  + ["9:32 AM", "EUR/USD", "1.1001", "200"]
  + ["9:33 AM", "EUR/USD", "1.1002", "300"]
  + ["9:34 AM", "EUR/USD", "1.1003", "400"]
  + ["9:35 AM", "EUR/USD", "1.1004", "500"]
* Expected Output:
  + ["EUR/USD 9:00 AM", 1.1002] (VWAP for EUR/USD)
