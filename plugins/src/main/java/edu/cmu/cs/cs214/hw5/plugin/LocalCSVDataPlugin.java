package edu.cmu.cs.cs214.hw5.plugin;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import edu.cmu.cs.cs214.hw5.core.CityInfo;
import edu.cmu.cs.cs214.hw5.core.DataPlugin;
import edu.cmu.cs.cs214.hw5.core.InputEntry;
import edu.cmu.cs.cs214.hw5.core.PathInputEntry;
import edu.cmu.cs.cs214.hw5.core.RawWeatherData;
import edu.cmu.cs.cs214.hw5.core.SelectOneInputEntry;
import edu.cmu.cs.cs214.hw5.core.State;
import edu.cmu.cs.cs214.hw5.core.TextInputEntry;
import edu.cmu.cs.cs214.hw5.core.WeatherIndexFrameworkData;
import edu.cmu.cs.cs214.hw5.core.WeatherMetric;
import edu.cmu.cs.cs214.hw5.core.WeatherRecord;
import edu.cmu.cs.cs214.hw5.core.WeatherState;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * A data plugin from local csv
 */
public class LocalCSVDataPlugin implements DataPlugin {
    private static final String NAME = "LocalCSVHourlyData";

    private static final String PATH_ENTRY_LABEL = "File Path";
    private static final String PATH_ENTRY_HELP_MSG = "Path of the csv file";

    private static final String CITY_ENTRY_LABEL = "City Name";
    private static final String CITY_ENTRY_HELP_MSG = "Input city name of loaded CSV";

    private static final String STATE_ENTRY_LABEL = "State Name";
    private static final String STATE_ENTRY_HELP_MSG = "Select State of the city";

    private static final int ENTRYCNT = 3;
    private List<InputEntry> inputEntries;

    /**
     * Constructor
     */
    public LocalCSVDataPlugin() {
        inputEntries = new ArrayList<>();
        InputEntry pathInputEntry = new PathInputEntry(PATH_ENTRY_LABEL, PATH_ENTRY_HELP_MSG);
        InputEntry cityNameEntry = new TextInputEntry(CITY_ENTRY_LABEL, CITY_ENTRY_HELP_MSG);
        InputEntry stateNameEntry = new SelectOneInputEntry(STATE_ENTRY_LABEL, STATE_ENTRY_HELP_MSG);

        List<String> options = new ArrayList<>();

        for(State state: State.values()) {
            options.add(state.getName());
        }
        stateNameEntry.setScope(options);

        inputEntries.add(pathInputEntry);
        inputEntries.add(cityNameEntry);
        inputEntries.add(stateNameEntry);
    }

    private RawWeatherData loadCSV(String path, String cityName, String stateName) {
        CSVReader csvReader;
        try {
            FileReader fileReader = new FileReader(path);
            csvReader = new CSVReader(fileReader);
        } catch (FileNotFoundException ex) {
            throw new IllegalArgumentException("File not found " + path);
        }
        String[] headers;
        try {
            headers = csvReader.readNext();
        } catch (CsvValidationException | IOException e) {
            throw new IllegalStateException("Fail to parse csv file");
        }

        List<WeatherMetric> providedMetrics = new ArrayList<>();
        for(int i=1; i<headers.length; i++) {
            if(!WeatherMetric.contains(headers[i])) {
                throw new IllegalStateException("Invalid Metric " + headers[i]);
            } else {
                providedMetrics.add(WeatherMetric.byDescription(headers[i]));
            }
        }

        State state = State.getByName(stateName);
        RawWeatherData rawWeatherData = new RawWeatherData(new CityInfo(cityName, state.getName(), state.getAbbr(), 0, 0), new HashSet<>(providedMetrics));

        try {
            String[] recordStr;
            while((recordStr = csvReader.readNext()) != null) {
                // read a new line
                if(recordStr.length != providedMetrics.size() + 1) {
                    throw new IllegalStateException("Invalid CSV file");
                }

                Map<WeatherMetric, Double> metricDataMap = new HashMap<>();
                WeatherState weatherState = null;
                OffsetDateTime dateTime = OffsetDateTime.parse(recordStr[0]);

                for(int i=1; i<recordStr.length; i++) {
                    String dataStr = recordStr[i];
                    WeatherMetric metric = providedMetrics.get(i-1);
                    if(!metric.equals(WeatherMetric.WEATHERSTATE)) {
                        metricDataMap.put(metric, Double.parseDouble(dataStr));
                    } else {
                        if(!WeatherState.contains(dataStr)) {
                            throw new IllegalStateException("Invalid weather state " + dataStr);
                        } else {
                            weatherState = WeatherState.byDescription(dataStr);
                        }
                    }
                }
                WeatherRecord record = new WeatherRecord(metricDataMap, weatherState);
                rawWeatherData.addWeatherRecord(dateTime, record);
            }
        } catch (CsvValidationException | IOException e) {
            throw new IllegalStateException("Fail to parse csv file");
        }

        return rawWeatherData;
    }

    /**
     * @return the name of this data plugin as a string
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Load data from its source
     *
     * @param inputEntries list of input entries that is specified by getInputEntries with the user input being set.
     * @return raw weather data list (each RawWeatherData represents a series of data for a city)
     * @throws IllegalArgumentException if any user input is invalid
     * @throws IllegalStateException    if any internal error happens during loading data
     */
    @Override
    public List<RawWeatherData> loadData(List<InputEntry> inputEntries) throws IllegalArgumentException, IllegalStateException {
        if(inputEntries.size() != ENTRYCNT) {
            throw new IllegalStateException("Frame work did not give enough parameters to plugin");
        }
        String path = inputEntries.get(0).getValue().trim();
        String cityName = inputEntries.get(1).getValue().trim();
        String stateName = inputEntries.get(2).getValue().trim();

        RawWeatherData rawWeatherData = loadCSV(path, cityName, stateName);
        return List.of(rawWeatherData);
    }

    /**
     * Gives information to plugin of the framework
     *
     * @param framework framework that holds this plugin
     */
    @Override
    public void onRegister(WeatherIndexFrameworkData framework) {

    }

    /**
     * Called by framework to indicate input entry needed by this plugin
     *
     * @return list of input entry needed by the plugin
     */
    @Override
    public List<InputEntry> getInputEntries() {
        return new ArrayList<>(inputEntries);
    }
}
