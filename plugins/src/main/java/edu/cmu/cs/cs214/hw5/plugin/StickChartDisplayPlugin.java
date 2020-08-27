package edu.cmu.cs.cs214.hw5.plugin;

import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.InputEntry;
import edu.cmu.cs.cs214.hw5.core.ProcessedWeatherData;
import edu.cmu.cs.cs214.hw5.core.RawWeatherData;
import edu.cmu.cs.cs214.hw5.core.SelectMultipleInputEntry;
import edu.cmu.cs.cs214.hw5.core.SelectOneInputEntry;
import edu.cmu.cs.cs214.hw5.core.TextInputEntry;
import edu.cmu.cs.cs214.hw5.core.WeatherIndexFrameworkDisplay;
import edu.cmu.cs.cs214.hw5.core.WeatherIndexType;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A display plugin to display stick chart
 */
public class StickChartDisplayPlugin implements DisplayPlugin {

    private static final String NAME = "StickChartDisplayPlugin";

    /* Entry Constant */
    private static final String INDEX_ENTRY_LABEL = "Weather Index";
    private static final String INDEX_ENTRY_HELP_MSG = "[Select Many] Select the index (indices)" +
            "that you want tp visualize";
    private static final String CITY_NAME_ENTRY_LABEL = "City name";
    private static final String CITY_NAME_ENTRY_HELP_MSG = "Select one city " +
            "you want to visualize";

    /* Error Message */
    private static final String INDEX_NOT_EXIST_ERR_MSG = "The index that" +
            " you input may not valid or may not available";
    private static final String INTERNAL_ERROR_MSG = "Internal error";

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

        // Get the processed data for this city
        ProcessedWeatherData plotData = getProcessedWeatherData(cityName, data);

        // Get the indexMap
        Map<WeatherIndexType, Double> indexMap = plotData.getWeatherIndexMap();

        // Get all the indexes that the user want to visualize and available
        // to get from our data, this is also the x axis data
        List<WeatherIndexType> indexList = getAvailableIndexList(inputEntries.get(1),
            indexMap);

        List<String> xdata = getXdata(indexList);

        List<Double> ydata = getIndexValue(indexList, indexMap);

        // plot

        // Create Chart
        CategoryChart chart =
                new CategoryChartBuilder().width(600).height(600).title(
                        "Stick plot of weather index(es) for " + cityName).build();

        // Customize Chart
        chart.getStyler().setChartTitleVisible(true);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Stick);

        // add series
        chart.addSeries("Index", xdata, ydata);

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
        List<String> indexTypeOption = new ArrayList<>();

        for (ProcessedWeatherData pwd : data) {
            String city = pwd.getRawWeatherData().getCityInfo().getCityName();
            cityNameOption.add(city);
        }

        ProcessedWeatherData processedWeatherData = data.get(0);

        Map<WeatherIndexType, Double> weatherIndexMap =
                new HashMap<>(processedWeatherData.getWeatherIndexMap());

        for (Map.Entry<WeatherIndexType, Double> entry : weatherIndexMap.entrySet()) {
            String indexTypeName = entry.getKey().getDescription().toLowerCase();
            indexTypeOption.add(indexTypeName);
        }

        inputEntries = new ArrayList<>();

        InputEntry indexInputEntry = new SelectMultipleInputEntry(INDEX_ENTRY_LABEL,
                INDEX_ENTRY_HELP_MSG);
        InputEntry cityNameInputEntry =
                new SelectOneInputEntry(CITY_NAME_ENTRY_LABEL, CITY_NAME_ENTRY_HELP_MSG);

        cityNameInputEntry.setScope(cityNameOption);
        indexInputEntry.setScope(indexTypeOption);

        inputEntries.add(cityNameInputEntry);
        inputEntries.add(indexInputEntry);
    }

    private List<String> getXdata(List<WeatherIndexType> indexList) {
        List<String> xdata = new ArrayList<>();
        for (WeatherIndexType weatherIndexType : indexList) {
            xdata.add(weatherIndexType.getDescription());
        }
        return xdata;
    }

    // Get all index values
    private List<Double> getIndexValue(List<WeatherIndexType> indexList,
                                       Map<WeatherIndexType, Double> indexMap) {
        List<Double> indexValue = new ArrayList<>();
        for (WeatherIndexType weatherIndexType : indexList) {
            indexValue.add(indexMap.get(weatherIndexType));
        }
        return indexValue;
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


    // The output list contains all the indexes with non-null value
    private List<WeatherIndexType> getAvailableIndexList(InputEntry entry,
                                                         Map<WeatherIndexType, Double> indexMap) {
        String input = entry.getValue();

        String[] inputList = input.split(";");

        if(inputList.length == 0) {
            throw new IllegalArgumentException("Please select at least 1 index");
        }

        List<WeatherIndexType> indexList = new ArrayList<>();

        for (String s : inputList) {
            String tmp = s.trim();
            if (indexMap.containsKey(WeatherIndexType.byName(tmp)) && indexMap.get(WeatherIndexType.byName(tmp)) != null) {
                indexList.add(WeatherIndexType.byName(tmp));
            } else {
                throw new IllegalStateException(INDEX_NOT_EXIST_ERR_MSG + ": " + tmp);
            }
        }
        return indexList;
    }
}
