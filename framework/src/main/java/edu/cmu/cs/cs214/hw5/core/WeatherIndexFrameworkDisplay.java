package edu.cmu.cs.cs214.hw5.core;

import java.util.List;

/**
 * Interface for display plugins
 */
public interface WeatherIndexFrameworkDisplay {
    /**
     * Set the display input entries for display plugin
     * @param displayInputEntries display input entries to set
     */
    void setDisplayInputEntries(List<InputEntry> displayInputEntries);

    /**
     * Get the display input entries
     * @return list of display input entries
     */
    List<InputEntry> getDisplayInputEntries();
}
