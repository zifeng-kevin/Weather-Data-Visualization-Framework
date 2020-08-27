package edu.cmu.cs.cs214.hw5.plugins.display;

import edu.cmu.cs.cs214.hw5.core.DisplayPlugin;
import edu.cmu.cs.cs214.hw5.core.datastructures.TimePoint;
import edu.cmu.cs.cs214.hw5.core.datastructures.TimeSeries;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import javax.swing.JPanel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Display plugin to plot a grouped bar chart.
 */
public class GroupedBarChartDisplayPlugin implements DisplayPlugin {
    /**
     * Gets the chart type name that the display plugin supports. For example,
     * a display plugin that implements line chart should return "Line Chart".
     *
     * @return The name of the chart type that the display plugin supports
     */
    @Override
    public String getChartTypeName() {
        return "Grouped Bar Chart";
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
                .xAxisTitle("Time").yAxisTitle("Value").build();
        return chart;
    }

    private CategoryChart getChartHelper(List<TimeSeries> timeSeriesList) {

        // Create Chart
        CategoryChart chart =
                new CategoryChartBuilder().width(800).height(600).title(
                        "GroupedBarChartDisplayPlugin").xAxisTitle("Time").yAxisTitle("Value").theme(Styler.ChartTheme.GGPlot2).build();

        // Series
        TimeSeries timeSeries = timeSeriesList.get(0);
        List<LocalDate> localDateList = new ArrayList<>(timeSeries.getTimeSpan());


        List<LocalDate> xData = new ArrayList<>();
        for (int i = 0; i < localDateList.size(); i++) {
            boolean flag = true;
            for (int j = 1; j < timeSeriesList.size(); j++) {
                if (!timeSeriesList.get(j).getTimeSpan().contains(localDateList.get(i))) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                xData.add(localDateList.get(i));
            }
        }

        List<List<Double>> yDataList = new ArrayList<>();
        for (TimeSeries ts : timeSeriesList) {
            List<Double> yList = new ArrayList<>();
            for (LocalDate ld : xData) {
                yList.add(ts.getValue(ld));
            }
            yDataList.add(yList);
        }

        List<String> xPlot = new ArrayList<>();
        for (int i = 0; i < xData.size(); i++) {
            xPlot.add(xData.get(i).toString());
        }

        for (int i = 0; i < timeSeriesList.size(); i++) {
            String name = timeSeriesList.get(i).getName();
            if (yDataList.get(i).size() == 0) {
                continue;
            }
            chart.addSeries(name, xPlot, yDataList.get(i));
        }

        return chart;
    }

}
