package edu.cmu.cs.cs214.hw5.core;

import java.util.HashMap;
import java.util.Map;

/**
 * State names of the US
 */
public enum State {
  ALABAMA("Alabama", "AL"), ALASKA("Alaska", "AK"), AMERICAN_SAMOA("American Samoa", "AS"),
  ARIZONA("Arizona", "AZ"), ARKANSAS("Arkansas", "AR"), CALIFORNIA("California", "CA"),
  COLORADO("Colorado", "CO"), CONNECTICUT("Connecticut", "CT"), DELAWARE("Delaware", "DE"),
  DISTRICT_OF_COLUMBIA("District of Columbia", "DC"), FEDERATED_STATES_OF_MICRONESIA("Federated States of Micronesia", "FM"),
  FLORIDA("Florida", "FL"), GEORGIA("Georgia", "GA"), GUAM("Guam", "GU"), HAWAII("Hawaii", "HI"),
  IDAHO("Idaho", "ID"), ILLINOIS("Illinois", "IL"), INDIANA("Indiana", "IN"), IOWA("Iowa", "IA"),
  KANSAS("Kansas", "KS"), KENTUCKY("Kentucky", "KY"), LOUISIANA("Louisiana", "LA"),
  MAINE("Maine", "ME"), MARYLAND("Maryland", "MD"), MARSHALL_ISLANDS("Marshall Islands", "MH"),
  MASSACHUSETTS("Massachusetts", "MA"), MICHIGAN("Michigan", "MI"), MINNESOTA("Minnesota", "MN"),
  MISSISSIPPI("Mississippi", "MS"), MISSOURI("Missouri", "MO"), MONTANA("Montana", "MT"),
  NEBRASKA("Nebraska", "NE"), NEVADA("Nevada", "NV"), NEW_HAMPSHIRE("New Hampshire", "NH"),
  NEW_JERSEY("New Jersey", "NJ"), NEW_MEXICO("New Mexico", "NM"), NEW_YORK("New York", "NY"),
  NORTH_CAROLINA("North Carolina", "NC"), NORTH_DAKOTA("North Dakota", "ND"), NORTHERN_MARIANA_ISLANDS("Northern Mariana Islands", "MP"),
  OHIO("Ohio", "OH"), OKLAHOMA("Oklahoma", "OK"), OREGON("Oregon", "OR"),
  PALAU("Palau", "PW"), PENNSYLVANIA("Pennsylvania", "PA"), PUERTO_RICO("Puerto Rico", "PR"),
  RHODE_ISLAND("Rhode Island", "RI"), SOUTH_CAROLINA("South Carolina", "SC"),
  SOUTH_DAKOTA("South Dakota", "SD"), TENNESSEE("Tennessee", "TN"), TEXAS("Texas", "TX"),
  UTAH("Utah", "UT"), VERMONT("Vermont", "VT"), VIRGIN_ISLANDS("Virgin Islands", "VI"),
  VIRGINIA("Virginia", "VA"), WASHINGTON("Washington", "WA"),
  WEST_VIRGINIA("West Virginia", "WV"), WISCONSIN("Wisconsin", "WI"), WYOMING("Wyoming", "WY");

  private static final Map<String, State> ABBR_TO_STATE = new HashMap<>();
  private static final Map<String, State> NAME_TO_STATE = new HashMap<>();

  static {
    for(State state: values()) {
      ABBR_TO_STATE.put(state.abbr.toLowerCase(), state);
      NAME_TO_STATE.put(state.name.toLowerCase(), state);
    }
  }

  private String name;
  private String abbr;

  /**
   * Constructor
   * @param name name of state
   * @param abbr abbr of state
   */
  State(String name, String abbr) {
    this.abbr = abbr;
    this.name = name;
  }

  /**
   *
   * @return Abbr of this state
   */
  public String getAbbr() {
    return abbr;
  }

  /**
   *
   * @return fullname of the state
   */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "State{" +
        "name='" + name + '\'' +
        ", abbr='" + abbr + '\'' +
        '}';
  }

  /**
   * Check if contain this state by the abbr
   *
   * @param  abbr abbreviation of the state
   * @return true if it has, false otherwise
   */
  public static boolean containsByAbbr(String abbr) {
    return ABBR_TO_STATE.containsKey(abbr.toLowerCase());
  }

  /**
   * Check if contain this state by the name
   *
   * @param  name name of the state
   * @return true if it has, false otherwise
   */
  public static boolean containsByName(String name) {
    return NAME_TO_STATE.containsKey(name.toLowerCase());
  }

  /**
   * Get state by its name
   * @param name name of the state
   * @return state queried
   * @throws IllegalArgumentException if the name is invalid
   */
  public static State getByName(String name) {
    State state = NAME_TO_STATE.get(name.toLowerCase());
    if(state == null) {
      throw new IllegalArgumentException("Invalid state name " + name);
    }
    return state;
  }

  /**
   * Get state by its name
   * @param abbr abbreviation of the state
   * @return state queried
   * @throws IllegalArgumentException if the abbr is invalid
   */
  public static State getByAbbr(String abbr) {
    State state = ABBR_TO_STATE.get(abbr.toLowerCase());
    if(state == null) {
      throw new IllegalArgumentException("Invalid state abbreviation " + abbr);
    }
    return state;
  }
}
