package edu.cmu.cs.cs214.hw5.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Different weather related metrics
 */
public enum WeatherMetric {
  TEMPERATURE("Temperature", true, "F"),
  FEELTEMPERATURE("Feel Temperature", true, "F"),
  WINDSPEED("Wind Speed", true, "mile/h"),
  WINDDIRECTION("Wind Direction", true, "degree"),
  HUMIDITY("Humidity", true, "%"),
  WEATHERSTATE("Weather State", false, ""),
  RAINPROBABILITY("Rain Probability", true, "%"),
  SNOWPROBABILITY("Snow Probability", true, "%"),
  VISIBILITY("Visibility", true, "mile(s)");

  private static final Map<String, WeatherMetric> NAME_TO_METRIC = new HashMap<>();

  static {
    for(WeatherMetric metric: values()) {
      NAME_TO_METRIC.put(metric.getDescription().toLowerCase(), metric);
    }
  }

  private final String description;
  private final String unit;
  private final boolean isNumeric;

  /**
   * Constructor
   * @param description name of the metric
   * @param isNumeric if it is Numeric
   * @param unit unit of the metric
   */
  WeatherMetric(String description, boolean isNumeric, String unit) {
    this.isNumeric = isNumeric;
    this.description = description;
    this.unit = unit;
  }

  /**
   * @return name of the metric
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return true if it is a numeric value
   */
  public boolean isNumeric() {
    return isNumeric;
  }

  /**
   * Check if contain this weather metric by the description
   *
   * @param description description of the weather metric
   * @return true if it has, false otherwise
   */
  public static boolean contains(String description) {
    return NAME_TO_METRIC.containsKey(description.toLowerCase());
  }

  /**
   * Get a weather metric by its description
   *
   * @param description description of the metric
   * @return weather metric
   * @throws IllegalArgumentException if description is invalid
   */
  public static WeatherMetric byDescription(String description) {
    WeatherMetric metric = NAME_TO_METRIC.get(description.toLowerCase());
    if(metric == null) {
      throw new IllegalArgumentException("Invalid metric " + description);
    }
    return metric;
  }

  /**
   * Get unit of this metric
   * @return unit of this metric
   */
  public String getUnit() {
    return unit;
  }

  @Override
  public String toString() {
    return "WeatherMetric{" +
        "description='" + description + '\'' +
        ", isNumeric=" + isNumeric +
        '}';
  }


}
