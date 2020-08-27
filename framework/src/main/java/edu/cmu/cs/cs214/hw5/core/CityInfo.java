package edu.cmu.cs.cs214.hw5.core;

/**
 * Represent base information about a city, this is immutable
 */
public class CityInfo {
  private final String cityName;
  private final String stateFullName;
  private final String stateAbbrName;
  private final double latitude;
  private final double longitude;

  /**
   * Constructor
   *
   * @param cityName city name
   * @param stateFullName state full name (e.g. Pennsylvania)
   * @param stateAbbrName state abbr. name (e.g. PA)
   * @param latitude latitude
   * @param longitude longitude
   */
  public CityInfo(String cityName, String stateFullName, String stateAbbrName, double latitude, double longitude) {
    this.cityName = cityName;
    this.stateFullName = stateFullName;
    this.stateAbbrName = stateAbbrName;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  /**
   * Copy constructor
   * @param anotherCityInfo another city info
   */
  CityInfo(CityInfo anotherCityInfo) {
    cityName = anotherCityInfo.cityName;
    stateFullName = anotherCityInfo.stateFullName;
    stateAbbrName = anotherCityInfo.stateAbbrName;
    latitude = anotherCityInfo.latitude;
    longitude = anotherCityInfo.longitude;
  }

  /**
   * @return city name
   */
  public String getCityName() {
    return cityName;
  }

  /**
   * @return full name of the state
   */
  public String getStateFullName() {
    return stateFullName;
  }

  /**
   * @return abbr. name of the state
   */
  public String getStateAbbrName() {
    return stateAbbrName;
  }

  /**
   * @return get latitude
   */
  public double getLatitude() {
    return latitude;
  }

  /**
   * @return get longitude
   */
  public double getLongitude() {
    return longitude;
  }

  /**
   * @return city info in string
   */
  @Override
  public String toString() {
    return "CityInfo{" +
        "cityName='" + cityName + '\'' +
        ", stateFullName='" + stateFullName + '\'' +
        ", stateAbbrName='" + stateAbbrName + '\'' +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        '}';
  }
}
