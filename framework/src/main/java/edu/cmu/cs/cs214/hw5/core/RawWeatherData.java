package edu.cmu.cs.cs214.hw5.core;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This represent a raw weather data of a city
 */
public class RawWeatherData {
    private static final String METRIC_DATA_NOT_PROVIDED_ERR_MEG = "Metric data not provided";
    /* Internal field to record data */
    private CityInfo cityInfo;
    private SortedMap<OffsetDateTime, WeatherRecord> weatherRecordMap;

    private Set<WeatherMetric> metricsProvided;

    private static final Comparator<OffsetDateTime> DATE_TIME_COMPARATOR = OffsetDateTime::compareTo;

    /**
     * Constructor from all parameters
     *
     * @param cityName city name
     * @param stateFullName state full name (e.g. Pennsylvania)
     * @param stateAbbrName state abbr. name (e.g. PA)
     * @param latitude latitude
     * @param longitude longitude
     * @param metricsProvided indicate which weather metrics are provided
     */
    public RawWeatherData(String cityName, String stateFullName, String stateAbbrName, double latitude, double longitude, Set<WeatherMetric> metricsProvided) {
        cityInfo = new CityInfo(cityName, stateFullName, stateAbbrName, latitude, longitude);
        weatherRecordMap = new TreeMap<>(DATE_TIME_COMPARATOR);
        this.metricsProvided = new HashSet<>(metricsProvided);
    }

    /**
     * Constructor based on a city info
     *
     * @param cityInfo city info
     * @param metricsProvided indicate which weather metrics are provided
     */
    public RawWeatherData(CityInfo cityInfo, Set<WeatherMetric> metricsProvided) {
        this.cityInfo = new CityInfo(cityInfo);
        weatherRecordMap = new TreeMap<>(DATE_TIME_COMPARATOR);
        this.metricsProvided = new HashSet<>(metricsProvided);
    }

    /**
     * Make a copy of a raw weather data object
     *
     * @param rawWeatherData make a copy of it
     */
    public RawWeatherData(RawWeatherData rawWeatherData) {
        this.cityInfo = new CityInfo(rawWeatherData.cityInfo);
        this.weatherRecordMap = new TreeMap<>(rawWeatherData.weatherRecordMap);
        this.metricsProvided = new HashSet<>(rawWeatherData.metricsProvided);
    }

    /**
     * Insert a weather recrod to dataset
     * @param dateTime date time of the record
     * @param weatherRecord record data
     */
    public void addWeatherRecord(OffsetDateTime dateTime, WeatherRecord weatherRecord) {
        weatherRecordMap.put(dateTime, weatherRecord);
    }

    /**
     * @return city information
     */
    public CityInfo getCityInfo() {
        return cityInfo;
    }

    /**
     * Get if a metric is provided by the record.
     * @param weatherMetric weather metric provided.
     * @return true if the metric presents in this data set
     */
    public boolean metricProvided(WeatherMetric weatherMetric) {
        return metricsProvided.contains(weatherMetric);
    }

    /**
     * Get data of the data set (need to call metricProvided to check for availability of the metric)
     *
     * @return the sorted metric data (from smaller timestamp to larger timestamp)
     * @throws IllegalArgumentException if this metric is not provided in this dataset
     */
    public SortedMap<OffsetDateTime, Double> getMetric(WeatherMetric weatherMetric) {
        if(!weatherMetric.isNumeric()) {
            throw new  IllegalArgumentException("Cannot get value for a non-numeric metric " + weatherMetric.getDescription());
        }
        SortedMap<OffsetDateTime, Double> metricVals = new TreeMap<>();

        for(Map.Entry<OffsetDateTime, WeatherRecord> entry: weatherRecordMap.entrySet()) {
            metricVals.put(entry.getKey(), entry.getValue().getNumeric(weatherMetric));
        }
        return metricVals;
    }

    /**
     * Get if the weather state is provided
     *
     * @return true if weather state data is provided in this data set
     */
    public boolean weatherStateProvided() {
        return metricsProvided.contains(WeatherMetric.WEATHERSTATE);
    }

    /**
     * Get weather state data of the data set
     * @return the sorted weather state data (from smaller timestamp to larger timestamp)
     * @throws IllegalStateException if weather state is not provided in this dataset
     */
    public SortedMap<OffsetDateTime, WeatherState> getWeatherState() {
        SortedMap<OffsetDateTime, WeatherState> weatherStates = new TreeMap<>();
        if(!weatherStateProvided()) {
            throw new IllegalStateException(METRIC_DATA_NOT_PROVIDED_ERR_MEG);
        } else {
            for(Map.Entry<OffsetDateTime, WeatherRecord> entry: weatherRecordMap.entrySet()) {
                weatherStates.put(entry.getKey(), entry.getValue().getWeatherState());
            }
        }
        return weatherStates;
    }

    /**
     * @return string info of the data
     */
    @Override
    public String toString() {
        return "RawWeatherData{" +
            "cityInfo=" + cityInfo +
            ", weatherRecordMap=" + weatherRecordMap +
            '}';
    }
}

