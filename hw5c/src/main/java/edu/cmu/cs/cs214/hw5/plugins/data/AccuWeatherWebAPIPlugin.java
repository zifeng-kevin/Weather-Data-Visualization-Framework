package edu.cmu.cs.cs214.hw5.plugins.data;


import edu.cmu.cs.cs214.hw5.core.DataPlugin;
import edu.cmu.cs.cs214.hw5.core.datastructures.DataSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.cs214.hw5.core.datastructures.TimeSeries;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;


/**
 * Data plugin to get future 5 day max, min temperature in F
 */
public class AccuWeatherWebAPIPlugin implements  DataPlugin {
    /* Web API related constant */
    // get api key at developer.accuweather.com
    private static final String API_KEY = "Ll7v2qLyKq26kByvt4pT14h6IpHy3yxl";
    private static final String LOCATIONSEARCH_URL = "http://dataservice.accuweather.com/locations/v1/cities/search";
    /* To get the location id for forcast search. (LOCATIONSEARCH_URL + API_KEY + cityName) */
    private static final String FORMAT_LOCATION_QUERY = "%s?apikey=%s&q=%s";
    private static final String FORECAST_URL = "http://dataservice.accuweather.com/forecasts/v1/daily/5day";
    /* FORECAST12H_URL + locationId + API_KEY */
    private static final String FORMAT_FORECAST_QUERY = "%s/%s?apikey=%s&details=true";

    private static final String SOURCE_NAME = "Accu Weather Future 5 Day Weather Temperature";
    private static final String PROMPT = "Enter City Name and State Abbreviation (e.g. Pittsburgh, PA)";
    private static final String DATASET_NAME_POSTFIX = "Future 5 Day Temperature(F)";
    private static final String DATASET_MAX_TEMP_POSTFIX = "Future 5 Day Max Temperature(F)";
    private static final String DATASET_MIN_TEMP_POSTFIX = "Future 5 Day Min Temperature(F)";

    /* Error Message */
    private static final String URL_ERR_MSG = "Internal Error when querying weather data";
    private static final String CONNECT_ERR_MSG = "Internal Error when connection to web API";
    private static final String STREAM_ERR_MSG = "Internal Error while getting input stream";
    private static final String JSON_PARSE_ERR_MSG = "Internal Error while parsing json";
    private static final int RESPONSE_OK = 200;

    /**
     * Object to represent info from web api regarding this city
     */
    private static class QueryInfo {
        private final String cityFullName;
        private final String stateAbbr;
        private final int locationId;

        QueryInfo(String cityFullName, String stateAbbr, int locationId) {
            this.cityFullName = cityFullName;
            this.stateAbbr = stateAbbr;
            this.locationId = locationId;
        }
    }

    private String httpGetRequest(String urlStr) throws IllegalStateException {
        HttpURLConnection connection;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(URL_ERR_MSG);
        } catch (IOException e) {
            throw new IllegalStateException(CONNECT_ERR_MSG);
        }

        StringBuilder sb = new StringBuilder();

        try {
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if(responseCode != RESPONSE_OK) {
                throw new IllegalStateException("HTTP Request Fail " + responseCode);
            }
            /* read response body */
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new IllegalStateException(STREAM_ERR_MSG);
        }

        connection.disconnect();
        return sb.toString();
    }


    private QueryInfo queryCityInfo(String cityName, String stateAbbr) throws IllegalStateException {
        String urlStr = String.format(FORMAT_LOCATION_QUERY, LOCATIONSEARCH_URL, API_KEY, cityName);
        String jsonStr = httpGetRequest(urlStr);

        boolean foundCityFlg = false;
        int locationId = -1;
        double latitude = -1;
        double longitude = -1;

        String cityFullName = "";
        String stateFullName = "";
        String stateAbbrName = "";

        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for(int i=0; i<jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                stateAbbrName = jsonObject.getJSONObject("AdministrativeArea").getString("ID");

                if(stateAbbrName.toLowerCase().equals(stateAbbr.toLowerCase())) {
                    foundCityFlg = true;
                    locationId = jsonObject.getInt("Key");
                    cityFullName = jsonObject.getString("EnglishName");
                    break;
                }
            }
        } catch (JSONException e) {
            throw new IllegalStateException(JSON_PARSE_ERR_MSG);
        }

        if(!foundCityFlg) {
            throw new IllegalStateException("Invalid location: " + cityName + ", " + stateAbbr);
        }

        return new QueryInfo(cityFullName, stateAbbrName, locationId);
    }

    private DataSet getWeatherDataFuture(QueryInfo queryInfo) {
        String urlStr = String.format(FORMAT_FORECAST_QUERY, FORECAST_URL, queryInfo.locationId, API_KEY);
        String jsonStr = httpGetRequest(urlStr);

        String maxTemperatureSeriesName = queryInfo.cityFullName + ", " + queryInfo.stateAbbr + " " + DATASET_MAX_TEMP_POSTFIX;
        String minTemperatureSeriesName = queryInfo.cityFullName + ", " + queryInfo.stateAbbr + " " + DATASET_MIN_TEMP_POSTFIX;

        String dataSetName = queryInfo.cityFullName + ", " + queryInfo.stateAbbr + " " + DATASET_NAME_POSTFIX;
        TimeSeries maxTemperatureSeries = new TimeSeries(maxTemperatureSeriesName);
        TimeSeries minTemperatureSeries = new TimeSeries(minTemperatureSeriesName);
        try {
            JSONObject weatherJson = new JSONObject(jsonStr);
            JSONArray jsonArray = weatherJson.getJSONArray("DailyForecasts");
            for(int i=0; i<jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                OffsetDateTime dateTime = OffsetDateTime.parse(jsonObject.getString("Date"));
                LocalDate localDate = dateTime.toLocalDate();
                double minTemperature = jsonObject.getJSONObject("Temperature").getJSONObject("Minimum").getDouble("Value");
                double maxTemperature = jsonObject.getJSONObject("Temperature").getJSONObject("Maximum").getDouble("Value");
                maxTemperatureSeries.insert(localDate, maxTemperature);
                minTemperatureSeries.insert(localDate, minTemperature);
            }
        } catch (JSONException e) {
            throw new IllegalStateException(JSON_PARSE_ERR_MSG);
        }

        List<TimeSeries> tsList = new ArrayList<>();
        tsList.add(maxTemperatureSeries);
        tsList.add(minTemperatureSeries);
        return new DataSet(tsList, new ArrayList<>(), dataSetName);
    }


    /**
     * Gets the names of all data sets that the data plugin provides.
     * (These names will be displayed on the GUI). Return empty list
     * if the data plugin requires user input (e.g. file path, data
     * name, etc.) to fetch data set
     *
     * @return the list containing the names of all data sets that the
     * data plugin provides
     */
    @Override
    public List<String> getAvailableDataSetNames() {
        return new ArrayList<>();
    }

    /**
     * Get a data set from the data plugin according to the string representation
     * of the data set. For example, the string could be file path, data name, etc.
     *
     * @param cityInfo the string needed to fetch the data set of format "cityname, state abbr"
     * @return the data set
     */
    @Override
    public DataSet getDataSet(String cityInfo) {
        String[] cityInfoSplit = cityInfo.split(",");
        if(cityInfoSplit.length != 2) {
            System.err.println("Invalid parameter");
            return DataSet.EMPTY_DATASET;
        }

        String cityName = cityInfoSplit[0].trim();
        String stateAbbr = cityInfoSplit[1].trim();

        try {
            QueryInfo queryInfo = queryCityInfo(cityName, stateAbbr);
            return getWeatherDataFuture(queryInfo);
        } catch (IllegalStateException e) {
            System.err.println("Failed to get data for " + cityInfo);
            return DataSet.EMPTY_DATASET;
        }
    }

    /**
     * Gets the name of the data source that that data plugin uses. For example,
     * a CSV data plugin should return "CSV". Yahoo Finance Web API data plugin
     * should return "Yahoo Finance Web API". This name will be displayed in GUI
     * when the user chooses data plugin
     *
     * @return the name of the data source that the data plugin uses
     */
    @Override
    public String getDataSourceName() {
        return SOURCE_NAME;
    }

    /**
     * Gets the user input prompt for fetching data set from the data plugin. For example,
     * a CSV data plugin should have the prompt "Enter File Path". Yahoo Finance Web
     * API data plugin should have the prompt "Enter Stock Symbol". If the data plugin
     * DOES NOT require user input to fetch data, simply return the empty string "".
     *
     * @return the user input prompt for fetching data set from the data plugin
     */
    @Override
    public String getPrompt() {
        return PROMPT;
    }
}
