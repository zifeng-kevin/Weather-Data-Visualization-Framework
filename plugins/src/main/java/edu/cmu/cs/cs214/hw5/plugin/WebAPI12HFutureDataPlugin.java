package edu.cmu.cs.cs214.hw5.plugin;

import edu.cmu.cs.cs214.hw5.core.CityInfo;
import edu.cmu.cs.cs214.hw5.core.DataPlugin;
import edu.cmu.cs.cs214.hw5.core.InputEntry;
import edu.cmu.cs.cs214.hw5.core.RawWeatherData;
import edu.cmu.cs.cs214.hw5.core.SelectOneInputEntry;
import edu.cmu.cs.cs214.hw5.core.State;
import edu.cmu.cs.cs214.hw5.core.TextInputEntry;
import edu.cmu.cs.cs214.hw5.core.WeatherIndexFrameworkData;
import edu.cmu.cs.cs214.hw5.core.WeatherMetric;
import edu.cmu.cs.cs214.hw5.core.WeatherRecord;
import edu.cmu.cs.cs214.hw5.core.WeatherState;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A data plugin based on the web api that returns the weather data in next 12h of a city
 */
public class WebAPI12HFutureDataPlugin implements DataPlugin {
  private static final String NAME = "12HourFuturePredict";

  /* Web API related constant */
  // get api key at developer.accuweather.com
  private static final String API_KEY = "";
  private static final String LOCATIONSEARCH_URL = "http://dataservice.accuweather.com/locations/v1/cities/search";
  /* To get the location id for forcast search. (LOCATIONSEARCH_URL + API_KEY + cityName) */
  private static final String FORMAT_LOCATION_QUERY = "%s?apikey=%s&q=%s";
  private static final String FORECAST12H_URL = "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour";
  /* FORECAST12H_URL + locationId + API_KEY */
  private static final String FORMAT_FORECAST12H_QUERY = "%s/%s?apikey=%s&details=true";

  /* Error Message */
  private static final String URL_ERR_MSG = "Internal Error when querying weather data";
  private static final String CONNECT_ERR_MSG = "Internal Error when connection to web API";
  private static final String STREAM_ERR_MSG = "Internal Error while getting input stream";
  private static final String JSON_PARSE_ERR_MSG = "Internal Error while parsing json";
  private static final int RESPONSE_OK = 200;

  /* entry constant */
  private static final String CITY_ENTRY_LABEL = "City Name";
  private static final String CITY_ENTRY_HELP_MSG = "Input city name you want to know the weather index";

  private static final String STATE_ENTRY_LABEL = "State Name";
  private static final String STATE_ENTRY_HELP_MSG = "Select State of the city";

  private static final Set<WeatherMetric>  METRICS_PROVIDED = Set.of(WeatherMetric.TEMPERATURE, WeatherMetric.WINDSPEED,
  WeatherMetric.WINDDIRECTION, WeatherMetric.HUMIDITY, WeatherMetric.WEATHERSTATE,
  WeatherMetric.RAINPROBABILITY, WeatherMetric.SNOWPROBABILITY, WeatherMetric.VISIBILITY);

  private static final Map<Integer, WeatherState> WEATHER_STATE_MAP = new HashMap<>();
  static {
    WEATHER_STATE_MAP.put(1, WeatherState.SUNNY);
    WEATHER_STATE_MAP.put(2, WeatherState.MOSTLY_SUNNY);
    WEATHER_STATE_MAP.put(3, WeatherState.PARTLY_SUNNY);
    WEATHER_STATE_MAP.put(4, WeatherState.INTERMITTENT_CLOUDS);
    WEATHER_STATE_MAP.put(5, WeatherState.HAZY_SUNSHINE);
    WEATHER_STATE_MAP.put(6, WeatherState.MOSTLY_CLOUDY);
    WEATHER_STATE_MAP.put(7, WeatherState.CLOUDY);
    WEATHER_STATE_MAP.put(8, WeatherState.DREARY);
    WEATHER_STATE_MAP.put(11, WeatherState.FOG);
    WEATHER_STATE_MAP.put(12, WeatherState.SHOWERS);
    WEATHER_STATE_MAP.put(13, WeatherState.MOSTLY_CLOUDY_WITH_SHOWERS);
    WEATHER_STATE_MAP.put(14, WeatherState.PARTLY_SUNNY_WITH_SHOWERS);
    WEATHER_STATE_MAP.put(15, WeatherState.T_STORMS);
    WEATHER_STATE_MAP.put(16, WeatherState.MOSTLY_CLOUDY_WITH_T_STORMS);
    WEATHER_STATE_MAP.put(17, WeatherState.PARTLY_SUNNY_WITH_T_STORMS);
    WEATHER_STATE_MAP.put(18, WeatherState.RAIN);
    WEATHER_STATE_MAP.put(19, WeatherState.FLURRIES);
    WEATHER_STATE_MAP.put(20, WeatherState.MOSTLY_CLOUDY_WITH_FLURRIES);
    WEATHER_STATE_MAP.put(21, WeatherState.PARTLY_SUNNY_WITH_FLURRIES);
    WEATHER_STATE_MAP.put(22, WeatherState.SNOW);
    WEATHER_STATE_MAP.put(23, WeatherState.MOSTLY_CLOUDY_WITH_SNOW);
    WEATHER_STATE_MAP.put(24, WeatherState.ICE);
    WEATHER_STATE_MAP.put(25, WeatherState.SLEET);
    WEATHER_STATE_MAP.put(26, WeatherState.FREEZING_RAIN);
    WEATHER_STATE_MAP.put(29, WeatherState.RAIN_AND_SNOW);
    WEATHER_STATE_MAP.put(30, WeatherState.HOT);
    WEATHER_STATE_MAP.put(31, WeatherState.COLD);
    WEATHER_STATE_MAP.put(32, WeatherState.WINDY);
    WEATHER_STATE_MAP.put(33, WeatherState.CLEAR);
    WEATHER_STATE_MAP.put(34, WeatherState.MOSTLY_CLEAR);
    WEATHER_STATE_MAP.put(35, WeatherState.PARTLY_CLOUDY);
    WEATHER_STATE_MAP.put(36, WeatherState.INTERMITTENT_CLOUDS);
    WEATHER_STATE_MAP.put(37, WeatherState.HAZY_MOONLIGHT);
    WEATHER_STATE_MAP.put(38, WeatherState.MOSTLY_CLOUDY);
    WEATHER_STATE_MAP.put(39, WeatherState.PARTLY_CLOUDY_WITH_SHOWERS);
    WEATHER_STATE_MAP.put(40, WeatherState.MOSTLY_CLOUDY_WITH_SHOWERS);
    WEATHER_STATE_MAP.put(41, WeatherState.PARTLY_CLOUDY_WITH_T_STORMS);
    WEATHER_STATE_MAP.put(42, WeatherState.MOSTLY_CLOUDY_WITH_T_STORMS);
    WEATHER_STATE_MAP.put(43, WeatherState.MOSTLY_CLOUDY_WITH_FLURRIES);
    WEATHER_STATE_MAP.put(44, WeatherState.MOSTLY_CLOUDY_WITH_SNOW);
  }

  private List<InputEntry> inputEntries;
  private static final int ENTRYCNT = 2;

  /**
   * Object to represent info from web api regarding this city
   */
  private static class QueryInfo {
    private final int locationId;
    private final CityInfo cityInfo;

    QueryInfo(int locationId, CityInfo cityInfo) {
      this.locationId = locationId;
      this.cityInfo = cityInfo;
    }
  }

  /**
   * Constructor
   */
  public WebAPI12HFutureDataPlugin() {
    inputEntries = new ArrayList<>();

    InputEntry cityInputEntry = new TextInputEntry(CITY_ENTRY_LABEL, CITY_ENTRY_HELP_MSG);
    InputEntry stateInputEntry = new SelectOneInputEntry(STATE_ENTRY_LABEL, STATE_ENTRY_HELP_MSG);
    List<String> options = new ArrayList<>();

    for(State state: State.values()) {
      options.add(state.getName());
    }
    stateInputEntry.setScope(options);

    inputEntries.add(cityInputEntry);
    inputEntries.add(stateInputEntry);
  }

  private String httpGetRequest(String urlStr) {
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


  private QueryInfo queryCityInfo(String cityName, String stateInfo) {
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
        stateFullName = jsonObject.getJSONObject("AdministrativeArea").getString("EnglishName");

        if(stateAbbrName.toLowerCase().equals(stateInfo.toLowerCase()) ||
            stateFullName.toLowerCase().equals(stateInfo.toLowerCase())) {
          foundCityFlg = true;
          locationId = jsonObject.getInt("Key");
          latitude = jsonObject.getJSONObject("GeoPosition").getDouble("Latitude");
          longitude = jsonObject.getJSONObject("GeoPosition").getDouble("Longitude");
          cityFullName = jsonObject.getString("EnglishName");
          break;
        }
      }
    } catch (JSONException e) {
      throw new IllegalStateException(JSON_PARSE_ERR_MSG);
    }

    if(!foundCityFlg) {
      throw new IllegalArgumentException("Invalid location: " + cityName + ", " + stateInfo);
    }

    CityInfo cityInfo = new CityInfo(cityFullName, stateFullName, stateAbbrName, latitude, longitude);
    return new QueryInfo(locationId, cityInfo);
  }

  private RawWeatherData getWeatherDataNext12H(QueryInfo queryInfo) {
    String urlStr = String.format(FORMAT_FORECAST12H_QUERY, FORECAST12H_URL, queryInfo.locationId, API_KEY);
    String jsonStr = httpGetRequest(urlStr);

    RawWeatherData rawWeatherData = new RawWeatherData(queryInfo.cityInfo, METRICS_PROVIDED);

    try {
      JSONArray jsonArray = new JSONArray(jsonStr);
      for(int i=0; i<jsonArray.length(); i++) {
        JSONObject jsonObject = jsonArray.getJSONObject(i);

        OffsetDateTime offsetDateTime = OffsetDateTime.parse(jsonObject.getString("DateTime"));
        double temperature = jsonObject.getJSONObject("Temperature").getDouble("Value");
        double windSpeed = jsonObject.getJSONObject("Wind").getJSONObject("Speed").getDouble("Value");
        double windDirection = jsonObject.getJSONObject("Wind").getJSONObject("Direction").getDouble("Degrees");
        double humidity = jsonObject.getDouble("RelativeHumidity");
        WeatherState weatherState = WEATHER_STATE_MAP.get(jsonObject.getInt("WeatherIcon"));
        int rainProbability = jsonObject.getInt("RainProbability");
        int snowProbability = jsonObject.getInt("SnowProbability");
        int visibility = jsonObject.getJSONObject("Visibility").getInt("Value");

        WeatherRecord weatherRecord = new WeatherRecord(temperature, 0, windSpeed, windDirection,
                humidity, weatherState, rainProbability, snowProbability, visibility);
        rawWeatherData.addWeatherRecord(offsetDateTime, weatherRecord);
      }
    } catch (JSONException e) {
      throw new IllegalStateException(JSON_PARSE_ERR_MSG);
    }

    return rawWeatherData;
  }

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
    if (API_KEY.equals("")) {
      throw new IllegalStateException("API KEY not set, check readme.md to get a key");
    }
    if(inputEntries.size() != ENTRYCNT) {
      throw new IllegalStateException("Frame work did not give enough parameters to plugin");
    }
    String cityName = inputEntries.get(0).getValue().trim();
    String stateInfo = inputEntries.get(1).getValue().trim();
    /* queryInfo is a formatted representation of city related information */
    QueryInfo queryInfo = queryCityInfo(cityName, stateInfo);
    RawWeatherData rawWeatherData = getWeatherDataNext12H(queryInfo);

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
   * Override get input entry
   *
   * @return input entry
   */
  @Override
  public List<InputEntry> getInputEntries() {
    return new ArrayList<>(inputEntries);
  }
}
