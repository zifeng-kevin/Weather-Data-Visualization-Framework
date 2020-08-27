package edu.cmu.cs.cs214.hw5.core;

import java.util.List;

/**
 * data plugin interface
 */
public interface DataPlugin {
  /**
   * @return the name of this data plugin as a string
   */
  String getName();

  /**
   * Load data from its source
   * @param inputEntries list of input entries that is specified by getInputEntries with the user input being set.
   * @return raw weather data list (each RawWeatherData represents a series of data for a city)
   * @throws IllegalArgumentException if any user input is invalid
   * @throws IllegalStateException if any internal error happens during loading data
   */
  List<RawWeatherData> loadData(List<InputEntry> inputEntries) throws IllegalArgumentException, IllegalStateException;

  /**
   * Gives information to plugin of the framework
   * @param framework framework that holds this plugin
   */
  void onRegister(WeatherIndexFrameworkData framework);

  /**
   * Called by framework to indicate input entry needed by this plugin
   * @return list of input entry
   */
  List<InputEntry> getInputEntries();
}