package edu.cmu.cs.cs214.hw5.core;

import java.util.Map;

/**
 * The type Processed weather data.
 */
public class ProcessedWeatherData {

    private RawWeatherData rawWeatherData;
    private Map<WeatherIndexType, Double> weatherIndexMap;


    /**
     * Constructor
     *
     * @param rawWeatherData rawWeatherData
     * @param weatherIndexMap weatherIndexMap
     */
    public ProcessedWeatherData(RawWeatherData rawWeatherData,
                                Map<WeatherIndexType, Double> weatherIndexMap) {
        this.rawWeatherData = rawWeatherData;
        this.weatherIndexMap = weatherIndexMap;
    }


    /**
     * Copy constructor
     *
     * @param processedWeatherData processedWeatherData
     */
    public ProcessedWeatherData(ProcessedWeatherData processedWeatherData) {
        this.rawWeatherData = processedWeatherData.rawWeatherData;
        this.weatherIndexMap = processedWeatherData.weatherIndexMap;
    }

    /**
     * Gets raw weather data.
     *
     * @return the raw weather data
     */
    public RawWeatherData getRawWeatherData() {
        return rawWeatherData;
    }

    /**
     * Sets raw weather data.
     *
     * @param rawWeatherData the raw weather data
     */
    public void setRawWeatherData(RawWeatherData rawWeatherData) {
        this.rawWeatherData = rawWeatherData;
    }

    /**
     * Gets weather index map.
     *
     * @return the weather index map
     */
    public Map<WeatherIndexType, Double> getWeatherIndexMap() {
        return weatherIndexMap;
    }

    /**
     * Sets weather index map.
     *
     * @param weatherIndexMap the weather index map
     */
    public void setWeatherIndexMap(Map<WeatherIndexType, Double> weatherIndexMap) {
        this.weatherIndexMap = weatherIndexMap;
    }
}
