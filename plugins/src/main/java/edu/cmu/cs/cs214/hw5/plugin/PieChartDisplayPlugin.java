package edu.cmu.cs.cs214.hw5.plugin;

import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.InputEntry;
import edu.cmu.cs.cs214.hw5.core.ProcessedWeatherData;
import edu.cmu.cs.cs214.hw5.core.RawWeatherData;
import edu.cmu.cs.cs214.hw5.core.SelectOneInputEntry;
import edu.cmu.cs.cs214.hw5.core.WeatherIndexFrameworkDisplay;
import edu.cmu.cs.cs214.hw5.core.WeatherMetric;
import edu.cmu.cs.cs214.hw5.core.WeatherState;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * A display plugin to display pie chart
 */
public class PieChartDisplayPlugin implements DisplayPlugin {

    private static final String NAME = "PieChartDisplayPlugin";

    /* Entry Constant */
    private static final String METRIC_ENTRY_LABEL = "Weather metric";
    private static final String METRIC_ENTRY_HELP_MSG = "Select a weather " +
            "metric" +
            "you want to visualize";
    private static final String CITY_NAME_ENTRY_LABEL = "City name";
    private static final String CITY_NAME_ENTRY_HELP_MSG = "Select a city " +
            "you want to visualize";

    /* Error Message */
    private static final String INTERNAL_ERROR_MSG = "Internal error";
    private static final String METRIC_NOT_FIT_ERROR_MSG = "This metric is " +
            "not available to plot because data source does not provide this metric";

    private List<InputEntry> inputEntries;


    /**
     * @return the name of this data plugin as a string
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Generate graph from given data and user input
     *
     * @param inputEntries list of input entries that is specified by getInputEntries with the user input being set.
     * @param data         list of processed data used by this plugin
     * @return A JPanel to be displayed on the GUI
     * @throws IllegalArgumentException if any user input is invalid
     * @throws IllegalStateException    if any internal error happens during loading data
     */
    @Override
    public JPanel generateGraph(List<InputEntry> inputEntries, List<ProcessedWeatherData> data) throws IllegalArgumentException, IllegalStateException {
        if(inputEntries == null || inputEntries.get(0).getValue() == null || inputEntries.get(1).getValue() == null) {
            throw new IllegalArgumentException("Invalid Input");
        }

        String cityName = inputEntries.get(0).getValue();

        if(!WeatherMetric.contains(inputEntries.get(1).getValue().trim())) {
            throw new IllegalArgumentException("Invalid metric " + inputEntries.get(1).getValue().trim());
        }

        // Get the processed data for this city
        ProcessedWeatherData processedWeatherDataData = getProcessedWeatherData(cityName,
                data);

        // Get raw data
        RawWeatherData rawWeatherData = processedWeatherDataData.getRawWeatherData();

        WeatherMetric metric =
                getWeatherMetric(inputEntries.get(1).getValue().trim());

        // Get metric data
        SortedMap<OffsetDateTime, WeatherState> metricData =
                getMetricData(metric, rawWeatherData);

        // Get plot data
        Map<String, Integer> plotData = getPlotData(metricData);

        // plot
        PieChart chart =
                new PieChartBuilder().width(600).height(600).title(
                        "Pie Chart of " + metric.getDescription() + " for " + cityName).build();

        // Customize Chart
        Color[] sliceColors = new Color[plotData.size()];

        for (int i = 0; i < sliceColors.length; i++) {
            sliceColors[i] = new Color((int) (Math.random() * 256),
                    (int) (Math.random() * 256), (int) (Math.random() * 256));
        }

        chart.getStyler().setSeriesColors(sliceColors);

        // Series
        for (Map.Entry<String, Integer> entry : plotData.entrySet()) {
            chart.addSeries(entry.getKey(), entry.getValue());
        }

        JPanel resPanel = new JPanel();
        resPanel.add(new XChartPanel<>(chart), BorderLayout.CENTER);

        return resPanel;
    }

    /**
     * Gives information to plugin of the framework
     *
     * @param framework framework that holds this plugin
     */
    @Override
    public void onRegister(WeatherIndexFrameworkDisplay framework) {

    }

    /**
     * Called by framework to indicate input entry needed by this plugin
     *
     * @return list of input entry
     */
    @Override
    public List<InputEntry> getInputEntries() {
        return new ArrayList<>(inputEntries);
    }

    /**
     * Provides processed data to display plugin
     *
     * @param data list of processed data used by this plugin
     */
    @Override
    public void readData(List<ProcessedWeatherData> data) {
        List<String> cityNameOption = new ArrayList<>();
        List<String> metricOption = new ArrayList<>();

        for (ProcessedWeatherData pwd : data) {
            String city = pwd.getRawWeatherData().getCityInfo().getCityName();
            cityNameOption.add(city);
        }

        for (WeatherMetric weatherMetric : WeatherMetric.values()) {
            if (!weatherMetric.isNumeric()) {
                metricOption.add(weatherMetric.getDescription().toLowerCase());
            }
        }

        inputEntries = new ArrayList<>();

        InputEntry metricInputEntry = new SelectOneInputEntry(METRIC_ENTRY_LABEL,
                METRIC_ENTRY_HELP_MSG);
        InputEntry cityNameInputEntry =
                new SelectOneInputEntry(CITY_NAME_ENTRY_LABEL,
                        CITY_NAME_ENTRY_HELP_MSG);

        metricInputEntry.setScope(metricOption);
        cityNameInputEntry.setScope(cityNameOption);

        inputEntries.add(cityNameInputEntry);
        inputEntries.add(metricInputEntry);
    }

    private Map<String, Integer> getPlotData(SortedMap<OffsetDateTime, WeatherState> metricData) {
        Map<String, Integer> plotData = new HashMap<>();
        for (Map.Entry<OffsetDateTime, WeatherState> entry : metricData.entrySet()) {
            if (!plotData.containsKey(entry.getValue().getDescription())) {
                plotData.put(entry.getValue().getDescription(), 1);
            } else {
                plotData.put(entry.getValue().getDescription(),
                        plotData.get(entry.getValue().getDescription()) + 1);
            }
        }
        return plotData;
    }


    private SortedMap<OffsetDateTime, WeatherState> getMetricData(WeatherMetric metric, RawWeatherData rawWeatherData) throws IllegalArgumentException {
        if (rawWeatherData.weatherStateProvided()) {
            return rawWeatherData.getWeatherState();
        } else {
            throw new IllegalArgumentException(METRIC_NOT_FIT_ERROR_MSG);
        }
    }

    private WeatherMetric getWeatherMetric(String value) {
        return WeatherMetric.byDescription(value);
    }


    // Get the processed weather data for the city
    private ProcessedWeatherData getProcessedWeatherData(String cityName,
                                                         List<ProcessedWeatherData> data) throws IllegalStateException {
        for (ProcessedWeatherData pd : data) {
            if (cityName.equals(pd.getRawWeatherData().getCityInfo().getCityName())) {
                return new ProcessedWeatherData(pd);
            }
        }
        throw new IllegalStateException(INTERNAL_ERROR_MSG);
    }
}
