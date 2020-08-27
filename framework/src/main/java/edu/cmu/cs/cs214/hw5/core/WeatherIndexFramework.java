package edu.cmu.cs.cs214.hw5.core;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Framework that handles loading data from data plugin, process and generate weather index and display
 * visualized data via display plugin.
 */
public class WeatherIndexFramework implements WeatherIndexFrameworkData, WeatherIndexFrameworkDisplay {
    private ChangeListener changeListener;
    private JPanel graphPanel;
    private boolean isDataPluginLoaded;
    private boolean isDisplayPluginSelected;
    private DataPlugin currentDataPlugin;
    private DisplayPlugin currentDisplayPlugin;
    private List<RawWeatherData> cityWeatherDataList;
    private List<InputEntry> dataInputEntries;
    private List<InputEntry> displayInputEntries;
    private List<ProcessedWeatherData> processedWeatherDataList;

    /**
     * Constructs a framework object.
     */
    public WeatherIndexFramework() {
        isDataPluginLoaded = false;
        isDisplayPluginSelected = false;
    }

    /**
     * Set the change listener for change notifications.
     * @param changeListener the observer interface of GUI
     */
    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    /**
     * Set the data plugin.
     * @param plugin selected data plugin.
     */
    public void setCurrentDataPlugin(DataPlugin plugin) {
        if(currentDataPlugin != plugin){
            isDataPluginLoaded = false;
        }
        currentDataPlugin = plugin;
    }

    /**
     * Set the display plugin
     * @param plugin selected display plugin.
     */
    public void setCurrentDisplayPlugin(DisplayPlugin plugin) {
        currentDisplayPlugin = plugin;
        isDisplayPluginSelected = true;
        if(isDataPluginLoaded){
            currentDisplayPlugin.readData(processedWeatherDataList);
        }
    }

    /**
     * Load data from data plugin. Process the raw data to generate weather indices.
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    public void processData() throws IllegalArgumentException, IllegalStateException {
        cityWeatherDataList = currentDataPlugin.loadData(dataInputEntries);

        /* process data and generate index */
        processedWeatherDataList = new ArrayList<>();
        for(RawWeatherData rawWeatherData: cityWeatherDataList) {
            WeatherIndexGenerator indexGenerator = new WeatherIndexGenerator(rawWeatherData);
            Map<WeatherIndexType, Double> indexValMap = indexGenerator.generate();
            processedWeatherDataList.add(new ProcessedWeatherData(rawWeatherData, indexValMap));
        }

        isDataPluginLoaded = true;
        if (isDisplayPluginSelected) {
            currentDisplayPlugin.readData(processedWeatherDataList);
        }
    }

    /**
     * Generate the graph given by display plugin.
     * @return panel showing the graph.
     */
    public JPanel generateGraph() {
        return currentDisplayPlugin.generateGraph(displayInputEntries, processedWeatherDataList);
    }

    /**
     * Get the input parameters for the data plugin
     * @return list of input entries
     */
    public List<InputEntry> getDataInputEntries() {
        dataInputEntries = currentDataPlugin.getInputEntries();
        return Collections.unmodifiableList(dataInputEntries);
    }

    /**
     * Get the input parameters for the display plugin
     * @return list of input entries
     */
    public List<InputEntry> getDisplayInputEntries() {
        displayInputEntries = currentDisplayPlugin.getInputEntries();
        return Collections.unmodifiableList(displayInputEntries);
    }

    /**
     * Set the input parameter values for the data plugin
     * @param dataInputEntries updated data input entries
     */
    public void setDataInputEntries(List<InputEntry> dataInputEntries) {
        this.dataInputEntries = new ArrayList<>();
        this.dataInputEntries.addAll(dataInputEntries);
    }

    /**
     * Set the input parameter values for the display plugin
     * @param displayInputEntries updated display input entries
     */
    public void setDisplayInputEntries(List<InputEntry> displayInputEntries) {
        this.displayInputEntries = new ArrayList<>();
        this.displayInputEntries.addAll(displayInputEntries);
    }

    /**
     * Get whether the data plugin loaded data.
     * @return true if the data plugin already loaded data.
     */
    public boolean isDataPluginLoaded() {
        return isDataPluginLoaded;
    }

    /**
     * Get whether the display plugin is selected.
     * @return true if display plugin is selected.
     */
    public boolean isDisplayPluginSelected() {
        return isDisplayPluginSelected;
    }

    /**
     * Register a data plugin to the framework.
     * @param plugin plugin to be registered.
     */
    public void registerDataPlugin(DataPlugin plugin) {
        plugin.onRegister(this);
        notifyDataPluginRegistered(plugin);
    }

    /**
     * Register a display plugin to the framework.
     * @param plugin plugin to be registered.
     */
    public void registerDisplayPlugin(DisplayPlugin plugin) {
        plugin.onRegister(this);
        notifyDisplayPluginRegistered(plugin);
    }

    /**
     * Notify the listener that a data plugin is registered.
     * @param plugin data plugin registered.
     */
    private void notifyDataPluginRegistered(DataPlugin plugin) {
        changeListener.onDataPluginRegistered(plugin);
    }
    /**
     * Notify the listener that a display plugin is registered.
     * @param plugin display plugin registered.
     */
    private void notifyDisplayPluginRegistered(DisplayPlugin plugin) {
        changeListener.onDisplayPluginRegistered(plugin);
    }
}
