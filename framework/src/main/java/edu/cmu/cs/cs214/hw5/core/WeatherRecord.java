package edu.cmu.cs.cs214.hw5.core;

import java.util.Map;

/**
 * Represent a single record at a time
 */
public class WeatherRecord {
  /* Uint: F */
  private final double temperature;
  private final double feelTemp;

  /* Unit: miles per hour */
  private final double windSpeed;
  /* Unit: degrees */
  private final double windDirection;
  /* Percentage */
  private final double humidity;
  /* weather state */
  private final WeatherState weatherState;
  /* percentage */
  private final double rainProbability;
  private final double snowProbability;
  /* Visibility : Mile */
  private final double visibility;

  /**
   * Constructor
   *
   * @param temperature temperature
   * @param feelTemp feel temperature
   * @param windSpeed wind speed
   * @param windDirection wind direction
   * @param humidity humidity
   * @param weatherState weather state
   * @param rainProbability rain probability
   * @param snowProbability snow probability
   * @param visibility visibility
   */
  public WeatherRecord(double temperature, double feelTemp, double windSpeed, double windDirection, double humidity, WeatherState weatherState, double rainProbability, double snowProbability, double visibility) {
    this.temperature = temperature;
    this.feelTemp = feelTemp;
    this.windSpeed = windSpeed;
    this.windDirection = windDirection;
    this.humidity = humidity;
    this.weatherState = weatherState;
    this.rainProbability = rainProbability;
    this.snowProbability = snowProbability;
    this.visibility = visibility;
  }

  /**
   * Construct weather record from a map(numeric value) and its weather state
   *
   * @param weatherMetricMap map that records data
   * @param weatherState weather state
   */
  public WeatherRecord(Map<WeatherMetric, Double> weatherMetricMap, WeatherState weatherState) {
    this.temperature = weatherMetricMap.get(WeatherMetric.TEMPERATURE) != null ?  weatherMetricMap.get(WeatherMetric.TEMPERATURE) : 0;
    this.feelTemp =  weatherMetricMap.get(WeatherMetric.FEELTEMPERATURE) != null ?  weatherMetricMap.get(WeatherMetric.FEELTEMPERATURE) : 0;
    this.windSpeed = weatherMetricMap.get(WeatherMetric.WINDSPEED) != null ?  weatherMetricMap.get(WeatherMetric.WINDSPEED) : 0;
    this.windDirection =weatherMetricMap.get(WeatherMetric.WINDDIRECTION) != null ?  weatherMetricMap.get(WeatherMetric.WINDDIRECTION) : 0;
    this.humidity = weatherMetricMap.get(WeatherMetric.HUMIDITY) != null ?  weatherMetricMap.get(WeatherMetric.HUMIDITY) : 0;
    this.rainProbability = weatherMetricMap.get(WeatherMetric.RAINPROBABILITY) != null ?  weatherMetricMap.get(WeatherMetric.RAINPROBABILITY) : 0;
    this.snowProbability = weatherMetricMap.get(WeatherMetric.SNOWPROBABILITY) != null ?  weatherMetricMap.get(WeatherMetric.SNOWPROBABILITY) : 0;
    this.visibility = weatherMetricMap.get(WeatherMetric.VISIBILITY) != null ?  weatherMetricMap.get(WeatherMetric.VISIBILITY) : 0;
    this.weatherState = weatherState;
  }

  /**
   * Copy constructor
   *
   * @param weatherRecord copy it
   */
  public WeatherRecord(WeatherRecord weatherRecord) {
    this.temperature = weatherRecord.temperature;
    this.feelTemp = weatherRecord.feelTemp;
    this.windSpeed = weatherRecord.windSpeed;
    this.windDirection = weatherRecord.windSpeed;
    this.humidity = weatherRecord.humidity;
    this.weatherState = weatherRecord.weatherState;
    this.rainProbability = weatherRecord.rainProbability;
    this.snowProbability = weatherRecord.snowProbability;
    this.visibility = weatherRecord.visibility;

  }

  /**
   * Get numeric value of a weather metric
   *
   * @param weatherMetric weather metric
   * @return the value of the weather metric
   * @throws IllegalArgumentException if weatherMetric is not a numeric value or is invalid
   */
  public double getNumeric(WeatherMetric weatherMetric) {
    if(!weatherMetric.isNumeric()) {
      throw new IllegalArgumentException("Cannot get value for a non-numeric metric " + weatherMetric.getDescription());
    }

    double val;
    switch (weatherMetric) {
      case VISIBILITY: {val = visibility; break;}
      case HUMIDITY: {val = humidity; break;}
      case WINDSPEED: {val = windSpeed; break;}
      case TEMPERATURE: {val = temperature; break;}
      case FEELTEMPERATURE: { val = feelTemp; break;}
      case WINDDIRECTION: {val = windDirection; break;}
      case RAINPROBABILITY: {val = rainProbability; break;}
      case SNOWPROBABILITY: {val = snowProbability; break;}
      default: throw new IllegalArgumentException("Cannot get value for " + weatherMetric.getDescription());
    }

    return val;
  }

  /**
   * @return weather state
   */
  public WeatherState getWeatherState() {
    return weatherState;
  }

  /**
   * @return string representation
   */
  @Override
  public String toString() {
    return "WeatherRecord{" +
        "temperature=" + temperature +
        ", feelTemp=" + feelTemp +
        ", windSpeed=" + windSpeed +
        ", windDirection=" + windDirection +
        ", humidity=" + humidity +
        ", weatherState=" + weatherState +
        ", rainProbability=" + rainProbability +
        ", snowProbability=" + snowProbability +
        ", visibility=" + visibility +
        '}';
  }
}

