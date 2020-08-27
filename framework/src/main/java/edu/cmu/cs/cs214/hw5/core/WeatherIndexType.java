package edu.cmu.cs.cs214.hw5.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Types of weather indices.
 */
public enum WeatherIndexType {
    SPORTINDEX("Sport Index"),
    HEATINDEX("Heat Index"),
    CARWASHINDEX("Carwash Index"),
    ILLNESSINDEX("Illness Index"),
    CLOTHINGINDEX("Clothing Index"),
    SKYDIVINGINDEX("Skydiving Index"),
    FISHINGINDEX("Fishing Index"),
    BOATINGINDEX("Boating Index"),
    GAMINGINDEX("Gaming Index");

    private static final Map<String, WeatherIndexType> NAME_TO_INDEX_TYPE  =
            new HashMap<>();

    static {
        for(WeatherIndexType indexType : values()) {
            NAME_TO_INDEX_TYPE.put(indexType.getDescription().toLowerCase(),
                    indexType);
        }
    }

    private final String description;

    /**
     * Constructor
     * @param description name of the metric
     */
    WeatherIndexType(String description) {
        this.description = description;
    }

    /**
     * @return name of the metric
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if contain this index by the description
     *
     * @param  description name of the index type
     * @return true if it has, false otherwise
     */
    public static boolean contains(String description) {
        return NAME_TO_INDEX_TYPE.containsKey(description.toLowerCase());
    }

    /**
     * Get a weather index type by its name
     *
     * @param description name of the index type
     * @return weatherIndexType
     * @throws IllegalArgumentException if name is invalid
     */
    public static WeatherIndexType byName(String description) {
        WeatherIndexType weatherIndexType = NAME_TO_INDEX_TYPE.get(description.toLowerCase());
        if(weatherIndexType == null) {
            throw new IllegalArgumentException("Invalid index type name: " + description);
        }
        return weatherIndexType;
    }

    @Override
    public String toString() {
        return "WeatherIndexType{" + "description='" + description + "}";
    }

}
