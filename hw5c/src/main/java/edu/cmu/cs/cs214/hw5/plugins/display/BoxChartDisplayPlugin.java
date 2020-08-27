package edu.cmu.cs.cs214.hw5.plugins.display;

import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.datastructures.TimeSeries;
import edu.cmu.cs.cs214.hw5.core.datastructures.TimePoint;
import org.knowm.xchart.BoxChart;
import org.knowm.xchart.BoxChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.BoxPlotStyler;
import org.knowm.xchart.style.Styler;

import javax.swing.JPanel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Display plugin to plot a Box chart.
 */
public class BoxChartDisplayPlugin implements DisplayPlugin {
    /**
     * Gets the chart type name that the display plugin supports. For example,
     * a display plugin that implements line chart should return "Line Chart".
     *
     * @return The name of the chart type that the display plugin supports
     */
    @Override
    public String getChartTypeName() {
        return "Box Chart";
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
        return true;
    }

    /**
     * Gets an empty chart with no data.
     *
     * @return an empty chart
     */
    @Override
    public JPanel getEmptyChart() {
        return new XChartPanel<>(getEmptyChartHelper("Untitled Chart"));
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
        return new XChartPanel<>(getChartHelper(timeSeriesList));
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
        return null;
    }

    private XYChart getEmptyChartHelper(String title) {
        // Create Chart
        XYChart chart = new XYChartBuilder().width(800).height(600).title(title)
                .xAxisTitle("Series").yAxisTitle("Value").build();
        return chart;
    }

    private BoxChart getChartHelper(List<TimeSeries> timeSeriesList) {
        BoxChart chart =
                new BoxChartBuilder().width(800).height(600).title(
                        "BoxChartDisplayPlugin").xAxisTitle("Series").yAxisTitle("Value").
                        theme(Styler.ChartTheme.GGPlot2).build();
        chart.getStyler().setBoxplotCalCulationMethod(BoxPlotStyler.BoxplotCalCulationMethod.N_LESS_1_PLUS_1);
        chart.getStyler().setToolTipsEnabled(true);
        for(TimeSeries series: timeSeriesList){
            List<Double> seriesValues = new ArrayList<>();
            for (Map.Entry<LocalDate,Double> timePoint:series){
                seriesValues.add(timePoint.getValue());
            }
            chart.addSeries(series.getName(),seriesValues);
        }
        return chart;
    }
}
