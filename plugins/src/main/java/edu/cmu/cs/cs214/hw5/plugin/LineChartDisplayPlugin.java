package edu.cmu.cs.cs214.hw5.plugin;

import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.InputEntry;
import edu.cmu.cs.cs214.hw5.core.ProcessedWeatherData;
import edu.cmu.cs.cs214.hw5.core.RawWeatherData;
import edu.cmu.cs.cs214.hw5.core.SelectMultipleInputEntry;
import edu.cmu.cs.cs214.hw5.core.SelectOneInputEntry;
import edu.cmu.cs.cs214.hw5.core.WeatherIndexFrameworkDisplay;
import edu.cmu.cs.cs214.hw5.core.WeatherMetric;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;


/**
 * A display plugin to display line chart
 */
public class LineChartDisplayPlugin implements DisplayPlugin {

    private static final String NAME = "LineChartDisplayPlugin";

    /* Entry Constant */
    private static final String METRIC_ENTRY_LABEL = "Weather metric";
    private static final String METRIC_ENTRY_HELP_MSG = "Input a weather " +
            "metric you want to visualize";
    private static final String CITY_NAME_ENTRY_LABEL = "City name(s)";
    private static final String CITY_NAME_ENTRY_HELP_MSG = "[Select Many] Select city/cities you want to visualize";

    /* Error Message */
    private static final String METRIC_NOT_AVAIL_ERR_MSG = "This metric data " +
            "is not available";

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
     * @return An JPanel to be displayed on the GUI
     * @throws IllegalArgumentException if any user input is invalid
     * @throws IllegalStateException    if any internal error happens during loading data
     */
    @Override
    public JPanel generateGraph(List<InputEntry> inputEntries, List<ProcessedWeatherData> data) throws IllegalArgumentException, IllegalStateException {
        if(inputEntries == null || inputEntries.get(0).getValue() == null ||
            inputEntries.get(1).getValue() == null || inputEntries.get(1).getValue().trim().equals("")) {
            throw new IllegalArgumentException("Invalid Input");
        }
        String metric = inputEntries.get(0).getValue();

        if(!WeatherMetric.contains(metric)) {
            throw new IllegalArgumentException("Invalid metric " + metric);
        }

        List<String> cityNameList =
                Arrays.asList(inputEntries.get(1).getValue().split(";"));

        if(cityNameList.size() == 0) {
            throw new IllegalArgumentException("Please select at least 1 city");
        }

        Map<String, SortedMap<OffsetDateTime, Double>> metricDataMap = getMetricData(cityNameList,
                data, metric);

        // labels: city name
        List<String> labels = new ArrayList<>(metricDataMap.keySet());

        // length of x axis
        int minSize = Integer.MAX_VALUE;

        // x data
        List<Integer> xData = new ArrayList<>();
        for (SortedMap<OffsetDateTime, Double> sortedMap :
                metricDataMap.values()) {
            if (sortedMap.size() < minSize) {
                minSize = sortedMap.size();
            }
        }
        for (int i = 0; i < minSize; i++) {
            xData.add(i);
        }

        // y data
        List<List<Double>> yData = new ArrayList<>();
        for (String label : labels) {
            SortedMap<OffsetDateTime, Double> sortedMap =
                    metricDataMap.get(label);
            List<Double> singleyData = new ArrayList<>(sortedMap.values());
            yData.add(singleyData.subList(0, minSize));
        }

        // Plot

        // Create Chart
        XYChart chart = new XYChartBuilder().width(600).height(600).title(
                "Line Chart for " + metric).xAxisTitle("Time Sequence").yAxisTitle(
                "Value").build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        //chart.getStyler().setYAxisLabelAlignment(Styler.TextAlignment.Right);
        //chart.getStyler().setYAxisDecimalPattern("$ #,###.##");
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(.95);

        for (int i = 0; i < labels.size(); i++) {
            String labelData = labels.get(i);
            chart.addSeries(labelData, xData, yData.get(i));
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

        for (ProcessedWeatherData processedWeatherData : data) {
            RawWeatherData rawWeatherData =
                    new RawWeatherData(processedWeatherData.getRawWeatherData());
            cityNameOption.add(rawWeatherData.getCityInfo().getCityName());
        }

        for (WeatherMetric weatherMetric : WeatherMetric.values()) {
            if (weatherMetric.isNumeric()) {
                metricOption.add(weatherMetric.getDescription());
            }
        }

        inputEntries = new ArrayList<>();

        InputEntry metricInputEntry =
                new SelectOneInputEntry(METRIC_ENTRY_LABEL,
                        METRIC_ENTRY_HELP_MSG);
        InputEntry cityNameInputEntry = new SelectMultipleInputEntry(CITY_NAME_ENTRY_LABEL,
                CITY_NAME_ENTRY_HELP_MSG);

        metricInputEntry.setScope(metricOption);
        cityNameInputEntry.setScope(cityNameOption);

        inputEntries.add(metricInputEntry);
        inputEntries.add(cityNameInputEntry);
    }


    // Get metric data for all the cities
    private Map<String, SortedMap<OffsetDateTime, Double>> getMetricData(List<String> cityNameList,
                                                                         List<ProcessedWeatherData> data, String metric) {
        Map<String, SortedMap<OffsetDateTime, Double>> metricData = new HashMap<>();

        WeatherMetric weatherMetric = WeatherMetric.byDescription(metric.toLowerCase());

        for (String s : cityNameList) {
            for (ProcessedWeatherData processedWeatherData : data) {
                RawWeatherData rawWeatherData =
                        new RawWeatherData(processedWeatherData.getRawWeatherData());
                if (s.equals(rawWeatherData.getCityInfo().getCityName())) {
                    if (rawWeatherData.metricProvided(weatherMetric)) {
                        SortedMap<OffsetDateTime, Double> singleMetricData =
                                rawWeatherData.getMetric(weatherMetric);
                        metricData.put(s, singleMetricData);
                    } else {
                        throw new IllegalStateException(METRIC_NOT_AVAIL_ERR_MSG + "for: " + s);
                    }
                }
            }
        }
        return metricData;
    }
}

