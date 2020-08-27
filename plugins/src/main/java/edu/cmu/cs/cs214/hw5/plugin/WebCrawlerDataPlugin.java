package edu.cmu.cs.cs214.hw5.plugin;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.cmu.cs.cs214.hw5.core.CityInfo;
import edu.cmu.cs.cs214.hw5.core.DataPlugin;
import edu.cmu.cs.cs214.hw5.core.InputEntry;
import edu.cmu.cs.cs214.hw5.core.RawWeatherData;
import edu.cmu.cs.cs214.hw5.core.State;
import edu.cmu.cs.cs214.hw5.core.TextInputEntry;
import edu.cmu.cs.cs214.hw5.core.WeatherIndexFrameworkData;
import edu.cmu.cs.cs214.hw5.core.WeatherMetric;
import edu.cmu.cs.cs214.hw5.core.WeatherRecord;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A data plugin from web crawler
 */
public class WebCrawlerDataPlugin implements DataPlugin {
  private static final String NAME = "WebCrawlerHourlyForecast";

  /* Web API related constant */
  private static final String FORECAST_HOURLY_FORMAT = "https://weather.com/weather/hourbyhour/l/%s";

  /* Error Message */
  private static final String CONNECT_ERR_MSG = "Internal Error when connection to Web";
  private static final String INVALID_ZIPCODE_MSG = "Invalid zip code";
  private static final String HTML_PARSE_ERR_MSG = "Internal Error while parsing html";

  /* entry constant */
  private static final String ZIP_CODE_ENTRY_LABEL = "ZIP Code";
  private static final String ZIP_CODE_HELP_MSG = "Input zip code of the city (If input multiple zip codes, separate them by \",\")";

  private static final Set<WeatherMetric> SUPPORT_METIRCS = Set.of(WeatherMetric.FEELTEMPERATURE,
      WeatherMetric.TEMPERATURE, WeatherMetric.RAINPROBABILITY, WeatherMetric.HUMIDITY, WeatherMetric.WINDSPEED);

  private static final int HOUR_RANGE = 12;
  private static final int ENTRYCNT = 1;

  private List<InputEntry> inputEntries;

  /**
   * Constructor
   */
  public WebCrawlerDataPlugin() {
    inputEntries = new ArrayList<>();
    InputEntry zipCodeInputEntry = new TextInputEntry(ZIP_CODE_ENTRY_LABEL, ZIP_CODE_HELP_MSG);
    inputEntries.add(zipCodeInputEntry);
  }

  private OffsetDateTime parseTime(String timeRaw) {
    if(timeRaw.split(" ")[0].length() == 4) {
      timeRaw = "0" + timeRaw;
    }

    String[] timeRawSplitted = timeRaw.split(":");
    timeRaw = timeRawSplitted[0] + ":00" + timeRawSplitted[1].substring(2).toUpperCase();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String dateStr = formatter.format(LocalDateTime.now());
    String dateTimeStr = dateStr + " " + timeRaw;
    LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyy-MM-dd hh:mm a z" , Locale.US));

    return OffsetDateTime.parse(localDateTime + "-04:00");
  }

  private RawWeatherData parseDoc(Document doc) {

    Elements elements = doc.getElementById("twc-scrollabe").getElementsByAttributeValueMatching("classname", ".* closed");

    String cityRaw = doc.select("#main-PageTitle-43cccb23-e507-4efd-af92-94bf48f04be8 > div > div > h1").text();
    String[] cityRawSplit = cityRaw.split(",");
    String cityName = cityRawSplit[0].trim();
    String stateAbbr = cityRawSplit[1].trim().substring(0, 2);
    CityInfo cityInfo = new CityInfo(cityName, State.getByAbbr(stateAbbr).getName(), stateAbbr, 0, 0);

    String timeInfoRaw = doc.select("#main-PageTitle-43cccb23-e507-4efd-af92-94bf48f04be8 > div > div > div > span").text();
    OffsetDateTime startDateTime = parseTime(timeInfoRaw);
    int hourOffset = 0;

    RawWeatherData weatherData = new RawWeatherData(cityInfo, SUPPORT_METIRCS);

    for(Element element: elements) {
      OffsetDateTime curDateTime = startDateTime.plusHours(hourOffset);
      hourOffset++;

      String tempRaw = element.getElementsByAttributeValue("headers", "temp").text();
      String feelTempRaw = element.getElementsByAttributeValue("headers", "feels").text();
      String rainProbRaw = element.getElementsByAttributeValue("headers", "precip").text();
      String humidityRaw =  element.getElementsByAttributeValue("headers", "humidity").text();
      String windRaw =  element.getElementsByAttributeValue("headers", "wind").text();

      double temp = Double.parseDouble(tempRaw.substring(0, tempRaw.length()-1));
      double feelTemp = Double.parseDouble(feelTempRaw.substring(0, feelTempRaw.length()-1));
      double rainProb = Double.parseDouble(rainProbRaw.substring(0, rainProbRaw.length()-1));
      double humidity = Double.parseDouble(humidityRaw.substring(0, humidityRaw.length()-1));
      double windSpeed = Double.parseDouble(windRaw.split(" ")[1]);

      WeatherRecord record = new WeatherRecord(temp, feelTemp, windSpeed, 0,
          humidity, null, rainProb, 0, 0);

      weatherData.addWeatherRecord(curDateTime, record);

      if(hourOffset == HOUR_RANGE) {
        break;
      }
    }
    return weatherData;
  }

  private RawWeatherData crawlData(String zipCode) {
    String url = String.format(FORECAST_HOURLY_FORMAT, zipCode);

    Document doc;
    try {
      doc = Jsoup.connect(url).get();
    } catch (HttpStatusException e) {
      throw new IllegalArgumentException(INVALID_ZIPCODE_MSG + " " +zipCode);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException(CONNECT_ERR_MSG);
    }

    try {
      return parseDoc(doc);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException(HTML_PARSE_ERR_MSG);
    }
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
    String zipCodeRaw = inputEntries.get(0).getValue().trim();
    String[] zipCodes = zipCodeRaw.split(",");
    List<RawWeatherData> rawWeatherDataList = new ArrayList<>();
    for(String zipCode: zipCodes) {
      RawWeatherData rawWeatherData = crawlData(zipCode.trim());
      rawWeatherDataList.add(rawWeatherData);
    }
    return rawWeatherDataList;
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
   * @return list of input entry
   */
  @Override
  public List<InputEntry> getInputEntries() {
    return new ArrayList<>(inputEntries);
  }
}
