package edu.cmu.cs.cs214.hw5.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent different weather states, reference: https://developer.accuweather.com/weather-icons
 */
public enum WeatherState {
  SUNNY("Sunny"), MOSTLY_SUNNY("Mostly Sunny"), PARTLY_SUNNY("Partly Sunny"),
  INTERMITTENT_CLOUDS("Intermittent Clouds"), HAZY_SUNSHINE("Hazy Sunshine"),
  MOSTLY_CLOUDY("Mostly Cloudy"), CLOUDY("Cloudy"), DREARY("Dready"), FOG("Fog"),
  SHOWERS("Showers"), MOSTLY_CLOUDY_WITH_SHOWERS("Mostly Cloudy With Showers"),
  PARTLY_SUNNY_WITH_SHOWERS("Partly Sunny With Showers"),
  T_STORMS("Thunder Storms"), MOSTLY_CLOUDY_WITH_T_STORMS("Mostly Cloudy With Thunder Storms"),
  PARTLY_SUNNY_WITH_T_STORMS("Partly Sunny With Thunder Storms"),
  RAIN("Rain"), FLURRIES("Flurries"), MOSTLY_CLOUDY_WITH_FLURRIES("Mostly Cloudy With Flurries"),
  PARTLY_SUNNY_WITH_FLURRIES("Partly Sunny With Flurries"), SNOW("Snow"),
  MOSTLY_CLOUDY_WITH_SNOW("Mostly Cloudy With Snow"), ICE("Ice"), SLEET("Sleet"),
  FREEZING_RAIN("Freezing Rain"), RAIN_AND_SNOW("Rain and Snow"),
  HOT("Hot"), COLD("Cold"), WINDY("Windy"),
  CLEAR("Clear"), MOSTLY_CLEAR("Mostly Clear"), PARTLY_CLOUDY("Partly Cloudy"),
  HAZY_MOONLIGHT("Hazy Moonlight"),  PARTLY_CLOUDY_WITH_SHOWERS("Partly Cloudy With Showers"),
  PARTLY_CLOUDY_WITH_T_STORMS("Partly Cloudy With Thunder Storms");

  private final String description;

  private static final Map<String, WeatherState> NAME_TO_STATE = new HashMap<>();

  static {
    for(WeatherState state: values()) {
      NAME_TO_STATE.put(state.getDescription().toLowerCase(), state);
    }
  }

  /**
   * Constructor
   * @param description description of the state
   */
  WeatherState(String description) {
    this.description = description;
  }

  /**
   *
   * @return description of the weather state
   */
  public String getDescription() {
    return description;
  }

  /**
   * Check if contain this weather state by the description
   *
   * @param description description of the weather state
   * @return true if it has, false otherwise
   */
  public static boolean contains(String description) {
    return NAME_TO_STATE.containsKey(description.toLowerCase());
  }

  /**
   * Get a weather state by its description
   *
   * @param description description of the weather state
   * @return weather state
   * @throws IllegalArgumentException if description is invalid
   */
  public static WeatherState byDescription(String description) {
    WeatherState state = NAME_TO_STATE.get(description.toLowerCase());
    if(state == null) {
      throw new IllegalArgumentException("Invalid weather state " + description);
    }
    return state;
  }

  @Override
  public String toString() {
    return "WeatherState{" +
        "description='" + description + '\'' +
        '}';
  }
}
