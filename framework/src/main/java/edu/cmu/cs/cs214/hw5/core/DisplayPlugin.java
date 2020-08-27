package edu.cmu.cs.cs214.hw5.core;

import javax.swing.JPanel;
import java.util.List;

/**
 * display plugin interface
 */
public interface DisplayPlugin {
    /**
     * @return the name of this data plugin as a string
     */
    String getName();

    /**
     * Generate graph from given data and user input
     *
     * @param inputEntries list of input entries that is specified by getInputEntries with the user input being set.
     * @param data         list of processed data used by this plugin
     * @return A JPanel to be displayed on the GUI
     * @throws IllegalArgumentException if any user input is invalid
     * @throws IllegalStateException    if any internal error happens during loading data
     */
    JPanel generateGraph(List<InputEntry> inputEntries,
                         List<ProcessedWeatherData> data) throws IllegalArgumentException, IllegalStateException;

    /**
     * Gives information to plugin of the framework
     *
     * @param framework framework that holds this plugin
     */
    void onRegister(WeatherIndexFrameworkDisplay framework);

    /**
     * Called by framework to indicate input entry needed by this plugin
     *
     * @return list of input entry
     */
    List<InputEntry> getInputEntries();

    /**
     * Provides processed data to display plugin
     *
     * @param data list of processed data used by this plugin
     */
    void readData(List<ProcessedWeatherData> data);
}
