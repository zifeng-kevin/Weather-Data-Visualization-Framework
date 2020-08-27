package edu.cmu.cs.cs214.hw5.core;

/**
 * Observer interface for the core to notify GUI.
 */
public interface ChangeListener {
    /**
     * Make changes when a data plugin is registered.
     * @param plugin the registered data plugin.
     */
    void onDataPluginRegistered(DataPlugin plugin);

    /**
     * Make changes when a display plugin is registered.
     * @param plugin the registered display plugin.
     */
    void onDisplayPluginRegistered(DisplayPlugin plugin);
}
