package edu.cmu.cs.cs214.hw5;

import edu.cmu.cs.cs214.hw5.core.DataPlugin;
import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.WeatherIndexFramework;
import edu.cmu.cs.cs214.hw5.gui.WeatherIndexFrameworkGui;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Main class to run the framework.
 */
public class WeatherIndexFrameworkRunner {
    /**
     * Main function to run the framework
     * @param args arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherIndexFrameworkRunner::createAndStartFramework);
    }

    /**
     * Creates and starts a framework
     */
    public static void createAndStartFramework(){
        WeatherIndexFramework framework = new WeatherIndexFramework();
        WeatherIndexFrameworkGui gui = new WeatherIndexFrameworkGui(framework);
        framework.setChangeListener(gui);
        List<DataPlugin> dataPlugins = loadDataPlugins();
        List<DisplayPlugin> displayPlugins = loadDisplayPlugins();
        dataPlugins.forEach(framework::registerDataPlugin);
        displayPlugins.forEach(framework::registerDisplayPlugin);
    }

    /**
     * Loads data plugins from the directory.
     * @return a list of data plugins.
     */
    private static List<DataPlugin> loadDataPlugins(){
        ServiceLoader<DataPlugin> plugins = ServiceLoader.load(DataPlugin.class);
        List<DataPlugin> result = new ArrayList<>();
        for (DataPlugin plugin : plugins) {
            System.out.println("Loaded plugin " + plugin.getName());
            result.add(plugin);
        }
        return result;
    }

    /**
     * Loads the display plugins from the directory
     * @return a list of display plugins
     */
    private static List<DisplayPlugin> loadDisplayPlugins(){
        ServiceLoader<DisplayPlugin> plugins = ServiceLoader.load(DisplayPlugin.class);
        List<DisplayPlugin> result = new ArrayList<>();
        for (DisplayPlugin plugin : plugins) {
            System.out.println("Loaded plugin " + plugin.getName());
            result.add(plugin);
        }
        return result;
    }
}
