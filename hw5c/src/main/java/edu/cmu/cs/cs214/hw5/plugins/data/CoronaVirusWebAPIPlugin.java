package edu.cmu.cs.cs214.hw5.plugins.data;

import edu.cmu.cs.cs214.hw5.core.DataPlugin;
import edu.cmu.cs.cs214.hw5.core.datastructures.DataSet;
import edu.cmu.cs.cs214.hw5.core.datastructures.TimeSeries;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.SystemTray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Data plugin to get corona virus related data of different countries in the world
 */
public class CoronaVirusWebAPIPlugin implements DataPlugin {
    private static final int QUERY_LASTDAYS = 100;

    private static final String SOURCE_NAME = "Corona Virus Historical " +  QUERY_LASTDAYS + "Days Data by Country";
    private static final String PROMPT = "Input Country Name or ISO 3166-1 Country Standards (If input multiple country, separate them by \",\")";

    /* Web API related constant */
    private static final String FORECAST_FORMAT = "https://corona.lmao.ninja/v2/historical/%s?lastdays=%d";

    /* Error Message */
    private static final String CONNECT_ERR_MSG = "Internal Error when connection to Web";
    private static final String INVALID_URL_MSG = "Invalid Url";
    private static final String INVALID_COUNTRY_MSG = "Invalid Country Name";
    private static final String HTML_PARSE_ERR_MSG = "Internal Error while parsing html";
    private static final String STREAM_ERR_MSG = "Internal Error while getting input stream";
    private static final int RESPONSE_OK = 200;


    private static final String DATASET_NAME_POSTFIX = "Past " + QUERY_LASTDAYS + " Day Statistics";
    private static final String DATASET_CASE_POSTFIX = "Past " + QUERY_LASTDAYS + " Day Total Cases";
    private static final String DATASET_DEATH_POSTFIX = "Past " + QUERY_LASTDAYS + " Day Deaths";
    private static final String DATASET_RECOVER_POSTFIX = "Past " + QUERY_LASTDAYS + " Day Recovered";


    private String httpGetRequest(String urlStr) throws IllegalStateException {
        HttpURLConnection connection;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(INVALID_URL_MSG + urlStr);
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

    private LocalDate parseDate(String dateRaw) {
        DateFormat df = new SimpleDateFormat("M/d/yy");
        Date date;
        try {
            date = df.parse(dateRaw);
        } catch (ParseException e) {
            throw new IllegalStateException("Fail to parse date " + dateRaw);
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private List<TimeSeries> queryData(String countryStr) {
        String url = String.format(FORECAST_FORMAT, countryStr, QUERY_LASTDAYS);
        String jsonStr;
        try {
            jsonStr = httpGetRequest(url);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }

        JSONObject dataJson;
        String countryName;
        try {
            dataJson = new JSONObject(jsonStr);
            countryName = dataJson.getString("country");
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }

        String caseSeriesName = countryName + " " + DATASET_CASE_POSTFIX;
        String deathSeriesName = countryName + " " + DATASET_DEATH_POSTFIX;
        String recoverSeriesName = countryName + " " + DATASET_RECOVER_POSTFIX;

        TimeSeries caseSeries = new TimeSeries(caseSeriesName);
        TimeSeries deathSeries = new TimeSeries(deathSeriesName);
        TimeSeries recoveredSeries = new TimeSeries(recoverSeriesName);

        try {
            JSONObject caseJson = dataJson.getJSONObject("timeline").getJSONObject("cases");
            JSONObject deathJson = dataJson.getJSONObject("timeline").getJSONObject("deaths");
            JSONObject recoverJson = dataJson.getJSONObject("timeline").getJSONObject("recovered");
            for(String dateStr: caseJson.keySet()) {
                LocalDate date = parseDate(dateStr);
                double cases = caseJson.getDouble(dateStr);
                caseSeries.insert(date, cases);
            }

            for(String dateStr: deathJson.keySet()) {
                LocalDate date = parseDate(dateStr);
                double deaths = deathJson.getDouble(dateStr);
                deathSeries.insert(date, deaths);
            }

            for(String dateStr: recoverJson.keySet()) {
                LocalDate date = parseDate(dateStr);
                double recovered = recoverJson.getDouble(dateStr);
                recoveredSeries.insert(date, recovered);
            }
        } catch (JSONException | IllegalStateException e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        } catch (NumberFormatException e) {
            System.out.println("Fail to parse number");
            return new ArrayList<>();
        }
        return List.of(caseSeries, deathSeries, recoveredSeries);
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
     * @param countries the string needed to fetch the data set
     * @return the data set
     */
    @Override
    public DataSet getDataSet(String countries) {
        String[] countriesSplit = countries.split(",");

        List<TimeSeries> tsList = new ArrayList<>();
        for(String country: countriesSplit) {
            tsList.addAll(queryData(country.trim()));
        }
        return new DataSet(tsList, new ArrayList<>(), DATASET_NAME_POSTFIX);
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
