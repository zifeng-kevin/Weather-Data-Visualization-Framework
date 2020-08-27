package edu.cmu.cs.cs214.hw5.core;

import java.util.List;

/**
 * Data plugin interface for data plugins
 */
public interface WeatherIndexFrameworkData {
    /**
     * Set the InputEntries for the data plugin
     * @param dataInputEntries input entries for data
     */
    void setDataInputEntries(List<InputEntry> dataInputEntries);

    /**
     * Get the input entries of data plugin
     * @return list of input entries
     */
    List<InputEntry> getDataInputEntries();
}
