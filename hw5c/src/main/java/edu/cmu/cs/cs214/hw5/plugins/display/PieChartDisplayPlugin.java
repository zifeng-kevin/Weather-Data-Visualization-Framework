package edu.cmu.cs.cs214.hw5.plugins.display;

import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.datastructures.TimePoint;
import edu.cmu.cs.cs214.hw5.core.datastructures.TimeSeries;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;

import javax.swing.JPanel;
import java.awt.Color;
import java.util.List;

/**
 * Display plugin to plot a pie chart.
 */
public class PieChartDisplayPlugin implements DisplayPlugin {

    /**
     * Gets the chart type name that the display plugin supports. For example,
     * a display plugin that implements line chart should return "Line Chart".
     *
     * @return The name of the chart type that the display plugin supports
     */
    @Override
    public String getChartTypeName() {
        return "Pie Chart";
    }

    /**
     * Returns a boolean flag indicating whether the display plugins' chart type
     * supports time series or not. For example, a line chart display plugin
     * should return true (since line chart plots time series), while a bar chart
     * display plugin should return false (since bar chart plots time points).
     *
     * @return true if the display plugin's chart type supports time series, false if
     * the display plugin's chart type supports time points.
     */
    @Override
    public Boolean isTimeSeriesChart() {
        return false;
    }

    /**
     * Gets an empty chart with no data.
     *
     * @return an empty chart
     */
    @Override
    public JPanel getEmptyChart() {
        return new XChartPanel<>(getEmptyChartHelper("PieChartDisplayPlugin"));
    }


    /**
     * Plots the time series on the chart and return the chart as a JPanel.
     * If the display plugin's chart type does not support time series,
     * then return null. (for example, a display plugin implementing
     * heat map should return the empty chart since heat map cannot plot time
     * series)
     *
     * @param timeSeriesList the list of time series that needs to be plotted
     *                       It is guaranteed that every time series in this
     *                       list has the same time unit. (For example, it could
     *                       be a list of day-based time series)
     * @return the plotted chart showing all time series
     */
    @Override
    public JPanel getTimeSeriesChart(List<TimeSeries> timeSeriesList) {
        return null;
    }

    /**
     * Plots the time points on the chart and return the chart as a JPanel
     * If the display plugin's chart type does not support time points,
     * then return null. (for example, a display plugin implementing
     * line chart should return the empty chart since line chart is meant to
     * plot time series)
     *
     * @param timePointList the list of time points that needs to be plotted
     * @return the plotted chart showing all time points
     */
    @Override
    public JPanel getTimePointChart(List<TimePoint> timePointList) {
        return new XChartPanel<>(getChartHelper(timePointList));
    }

    private PieChart getEmptyChartHelper(String title) {
        PieChart chart =
                new PieChartBuilder().width(800).height(600).title(title).build();
        return chart;
    }

    private PieChart getChartHelper(List<TimePoint> timePointList) {
        // Create Chart
        PieChart chart =
                getEmptyChartHelper("PieChartDisplayPlugin");

        // Generate color
        Color[] sliceColors = new Color[timePointList.size()];

        for (int i = 0; i < sliceColors.length; i++) {
            sliceColors[i] = new Color((int) (Math.random() * 256),
                    (int) (Math.random() * 256), (int) (Math.random() * 256));
        }

        chart.getStyler().setSeriesColors(sliceColors);

        // Series
        for (TimePoint tp : timePointList) {
            chart.addSeries(tp.getName(), tp.getValue());
        }

        return chart;
    }

}
