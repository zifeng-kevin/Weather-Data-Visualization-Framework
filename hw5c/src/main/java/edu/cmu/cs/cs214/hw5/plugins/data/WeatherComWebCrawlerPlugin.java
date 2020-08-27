package edu.cmu.cs.cs214.hw5.plugins.data;

import edu.cmu.cs.cs214.hw5.core.DataPlugin;
import edu.cmu.cs.cs214.hw5.core.datastructures.DataSet;
import edu.cmu.cs.cs214.hw5.core.datastructures.TimeSeries;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

import javax.swing.text.DateFormatter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherComWebCrawlerPlugin implements DataPlugin {
    private static final String SOURCE_NAME = "Weather.com Future 10 Day Weather Forecast";
    private static final String PROMPT = "Input zip code of the city (If input multiple zip codes, separate them by \",\")";

    /* Web API related constant */
    private static final String FORECAST_FORMAT = "https://weather.com/weather/tenday/l/%s";

    /* Error Message */
    private static final String CONNECT_ERR_MSG = "Internal Error when connection to Web";
    private static final String INVALID_ZIPCODE_MSG = "Invalid zip code";
    private static final String HTML_PARSE_ERR_MSG = "Internal Error while parsing html";

    private static final String DATASET_NAME_POSTFIX = "Future 10 Day Weather Forecast";
    private static final String DATASET_MAX_TEMP_POSTFIX = "Future 10 Day Max Temperature(F)";
    private static final String DATASET_MIN_TEMP_POSTFIX = "Future 10 Day Min Temperature(F)";
    private static final String DATASET_PRECIPITATION_POSTFIX = "Future 10 Day Precipitation(%)";
    private static final String DATASET_WIND_SPEED_POSTFIX = "Future 10 Day Wind Speed(mph)";
    private static final String DATASET_HUMIDITY_POSTFIX = "Future 10 Day Wind Humidity(%)";


    private LocalDate parseDate(String dateRaw) {
        String[] dateSplit = dateRaw.split(" ");
        if(dateSplit.length != 2) {
            throw new IllegalStateException("Fail to parse date " + dateRaw);
        }
        String month = dateSplit[0];
        String day = dateSplit[1];
        if(day.trim().length() == 1) {
            day = "0" + day;
        }
        int year = Calendar.getInstance().get(Calendar.YEAR);

        String dateStr = String.format("%d-%s-%s", year, month, day);
        DateFormat df = new SimpleDateFormat("yyy-MMM-dd");

        Date date;
        try {
            date = df.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalStateException("Fail to parse date " + dateRaw);
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private List<TimeSeries> parseDoc(Document doc) {
        Elements elements = doc.getElementById("twc-scrollabe").getElementsByAttributeValueMatching("classname", ".* closed");
        String cityRaw = doc.select("#main-PageTitle-1416b712-3440-435f-84a1-ee44d8719c69 > div > div > h1").text();
        String[] cityRawSplit = cityRaw.split(",");
        String cityName = cityRawSplit[0].trim();
        String stateAbbr = cityRawSplit[1].trim().substring(0, 2);

        TimeSeries maxTempSeries = new TimeSeries(cityName + ", " + stateAbbr + " " + DATASET_MAX_TEMP_POSTFIX);
        TimeSeries minTempSeries = new TimeSeries(cityName + ", " + stateAbbr + " " + DATASET_MIN_TEMP_POSTFIX);
        TimeSeries humiditySeries = new TimeSeries(cityName + ", " + stateAbbr + " " + DATASET_HUMIDITY_POSTFIX);
        TimeSeries precipitationSeries = new TimeSeries(cityName + ", " + stateAbbr + " " + DATASET_PRECIPITATION_POSTFIX);
        TimeSeries windSpeedSeries = new TimeSeries(cityName + ", " + stateAbbr + " " + DATASET_WIND_SPEED_POSTFIX);

        for(Element element: elements) {
            String dateRaw = element.getElementsByClass("day-detail clearfix").get(0).text();
            String tempMaxRaw = element.getElementsByAttributeValue("headers", "hi-lo").get(0).getElementsByTag("span").get(0).text();
            String tempMinRaw = element.getElementsByAttributeValue("headers", "hi-lo").get(0).getElementsByTag("span").get(2).text();
            String precipRaw = element.getElementsByAttributeValue("headers", "precip").text();
            String humidityRaw =  element.getElementsByAttributeValue("headers", "humidity").text();
            String windRaw =  element.getElementsByAttributeValue("headers", "wind").text();

            LocalDate localDate = parseDate(dateRaw);
            if(!tempMaxRaw.trim().equals("--")) {
                double maxTemp = Double.parseDouble(tempMaxRaw.substring(0, tempMaxRaw.length()-1));
                maxTempSeries.insert(localDate, maxTemp);
            }

            double minTemp = Double.parseDouble(tempMinRaw.substring(0, tempMinRaw.length()-1));
            minTempSeries.insert(localDate, minTemp);

            double precip = Double.parseDouble(precipRaw.substring(0, precipRaw.length()-1));
            precipitationSeries.insert(localDate, precip);

            double humidity = Double.parseDouble(humidityRaw.substring(0, humidityRaw.length()-1));
            humiditySeries.insert(localDate, humidity);

            double windSpeed = Double.parseDouble(windRaw.split(" ")[1]);
            windSpeedSeries.insert(localDate, windSpeed);
        }

        return List.of(maxTempSeries, minTempSeries, humiditySeries, precipitationSeries, windSpeedSeries);
    }


    private List<TimeSeries> crawlData(String zipCode) {
        String url = String.format(FORECAST_FORMAT, zipCode);

        Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (HttpStatusException e) {
            System.err.println(INVALID_ZIPCODE_MSG + " " +zipCode);
            return new ArrayList<>();
        } catch (IOException e) {
            System.err.println(CONNECT_ERR_MSG);
            return new ArrayList<>();
        }

        try {
            return parseDoc(doc);
        } catch (Exception e) {
            System.err.println(HTML_PARSE_ERR_MSG);
            return new ArrayList<>();
        }
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
     * @param zipCodes the string needed to fetch the data set
     * @return the data set
     */
    @Override
    public DataSet getDataSet(String zipCodes) {
        String[] zipCodeSplit = zipCodes.split(",");

        List<TimeSeries> tsList = new ArrayList<>();
        for(String zipCode: zipCodeSplit) {
            tsList.addAll(crawlData(zipCode.trim()));
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
