package edu.cmu.cs.cs214.hw5.core;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Generate various weather indices
 */
public class WeatherIndexGenerator {
    private RawWeatherData rawWeatherData;
    private Map<WeatherIndexType, Double> indexValMap;

    /**
     * Constructor
     *
     * @param rawWeatherData raw weather data of a city
     */
    public WeatherIndexGenerator(RawWeatherData rawWeatherData) {
        this.rawWeatherData = new RawWeatherData(rawWeatherData);
        this.indexValMap = new HashMap<>();

        for (WeatherIndexType indexType : WeatherIndexType.values()) {
            indexValMap.put(indexType, null);
        }

        calculateIndex();
    }

    /**
     * Return all indices of this city
     *
     * @return Return all indices of this city, if the value of this index is invalid, it is set to null
     */
    public Map<WeatherIndexType, Double> generate() {
        return new HashMap<>(indexValMap);
    }

    private double sum(List<Double> vals) {
       return vals.stream().mapToDouble(Double::doubleValue).sum();
    }

    private double avg(List<Double> vals) {
        return sum(vals) / vals.size();
    }

    private double vairance(List<Double> vals) {
        double variance = 0;
        double avg = avg(vals);
        for (double val: vals) {
            variance += (val - avg) * (val - avg);
        }
        variance /= vals.size();
        return variance;
    }

    private double scoreLRBound(double val, double range, double best) {
        double diff = Math.abs(val - best);
        /* between [0, 1] */
        if(diff > range) {
            return 0;
        } else {
            return 1 - diff / range;
        }
    }

    private double scoreLeftBound(double val, double range, double bestMin) {
        if(val > bestMin) {
            return 1;
        }
        if(val < bestMin - range) {
            return 0;
        }
        return 1 - (bestMin - val) / range;
    }

    private double scoreRightBound(double val, double range, double bestMax) {
        if(val < bestMax) {
            return 1;
        }
        if(val > bestMax + range) {
            return  0;
        }
        return  1 - (val-bestMax) /range;
    }


    private double getRate(List<WeatherState> weatherStates, String keyword) {
        int cnt = 0;

        for(WeatherState weatherState: weatherStates) {
            if(weatherState.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                cnt++;
            }
        }
        return cnt / (double)weatherStates.size();
    }

    private List<Double> extractValList(SortedMap<OffsetDateTime, Double> sortedMap) {
        List<Double> res = new ArrayList<>();
        for(Map.Entry<OffsetDateTime, Double> entry: sortedMap.entrySet()) {
            res.add(entry.getValue());
        }
        return res;
    }

    private List<WeatherState> extractStateList(SortedMap<OffsetDateTime, WeatherState> stateMap) {
        List<WeatherState> res = new ArrayList<>();
        for(Map.Entry<OffsetDateTime, WeatherState> entry: stateMap.entrySet()) {
            res.add(entry.getValue());
        }
        return res;
    }

    private void calculateIndex() {
        List<Double> temperatures = null;
        List<Double> windSpeeds = null;
        List<Double> rainProbabilities = null;
        List<Double> visibilities = null;
        List<WeatherState> weatherStates = null;

        if(rawWeatherData.metricProvided(WeatherMetric.TEMPERATURE)) {
            temperatures = extractValList(rawWeatherData.getMetric(WeatherMetric.TEMPERATURE));
        }

        if(rawWeatherData.metricProvided(WeatherMetric.WINDSPEED)) {
            windSpeeds = extractValList(rawWeatherData.getMetric(WeatherMetric.WINDSPEED));
        }

        if(rawWeatherData.metricProvided(WeatherMetric.RAINPROBABILITY)) {
            rainProbabilities = extractValList(rawWeatherData.getMetric(WeatherMetric.RAINPROBABILITY));
        }

        if(rawWeatherData.metricProvided(WeatherMetric.VISIBILITY)) {
            visibilities = extractValList(rawWeatherData.getMetric(WeatherMetric.VISIBILITY));
        }

        if(rawWeatherData.metricProvided(WeatherMetric.WEATHERSTATE)) {
            weatherStates = extractStateList(rawWeatherData.getWeatherState());
        }

        setSportIndex(temperatures, windSpeeds, rainProbabilities, visibilities);
        setHeatIndex(temperatures, weatherStates);
        setCarWashIndex(rainProbabilities, weatherStates);
        setIllnessIndex(temperatures);
        setClothingIndex(temperatures);
        setSkyDivingIndex(temperatures, windSpeeds, rainProbabilities, visibilities);
        setFishingIndex(temperatures, rainProbabilities, weatherStates);
        setBoatingIndex(temperatures, windSpeeds);
        setGameIndex();
    }

    private void setSportIndex(List<Double> temperatures, List<Double> windSpeeds, List<Double> rainProbabilities, List<Double> visibilities) {
        double tempScore = 0;
        double windSpeedScore = 0;
        double rainScore = 0;
        double visibilityScore = 0;
        int validMetricCnt = 0;

        if(temperatures != null) {
            tempScore = scoreLRBound(avg(temperatures), 25, 70);
            validMetricCnt++;
        }

        if(windSpeeds != null) {
            windSpeedScore = scoreRightBound(avg(windSpeeds), 10, 7);
            validMetricCnt++;
        }

        if(rainProbabilities != null) {
            rainScore = scoreRightBound(avg(rainProbabilities), 30, 50);
            validMetricCnt++;
        }

        if(visibilities != null) {
            visibilityScore = scoreLeftBound(avg(visibilities), 4, 5);
            validMetricCnt++;
        }

        if(validMetricCnt >= 1) {
            double score = (tempScore + windSpeedScore + rainScore + visibilityScore) / validMetricCnt;
            indexValMap.put(WeatherIndexType.SPORTINDEX, score * 10);
        }
    }

    private void setHeatIndex(List<Double> temperatures, List<WeatherState> weatherStates) {
        if(temperatures != null) {
            double factor = 1;
            double tempScore = 1 - scoreRightBound(avg(temperatures), 25, 70);
            if(weatherStates != null) {
                factor = getRate(weatherStates, "sunny");
            }
            indexValMap.put(WeatherIndexType.HEATINDEX, factor * tempScore * 10);
        }
    }

    private void setCarWashIndex(List<Double> rainProbabilities, List<WeatherState> weatherStates) {
        double rainScore = 0;
        double stateScore = 0;
        int validMetricCnt = 0;

        if(rainProbabilities != null) {
            rainScore = scoreRightBound(avg(rainProbabilities), 30, 50);
            validMetricCnt++;
        }

        if(weatherStates != null) {
            stateScore = getRate(weatherStates, "rain");
            validMetricCnt++;
        }

        if(validMetricCnt >= 1) {
            double score = (rainScore + stateScore) / validMetricCnt;
            indexValMap.put(WeatherIndexType.CARWASHINDEX, score * 10);
        }
    }

    private void setIllnessIndex(List<Double> temperatures) {
        if(temperatures != null) {
            double tempScore = 1 - scoreLRBound(avg(temperatures), 50, 70);
            indexValMap.put(WeatherIndexType.ILLNESSINDEX, tempScore * 10);
        }
    }

    private void setClothingIndex(List<Double> temperatures) {
        if(temperatures != null) {
            double tempScore = scoreRightBound(avg(temperatures), 50, 30);
            indexValMap.put(WeatherIndexType.CLOTHINGINDEX, tempScore * 10);
        }
    }

    private void setSkyDivingIndex(List<Double> temperatures, List<Double> windSpeeds, List<Double> rainProbabilities, List<Double> visibilities) {
        double tempScore = 0;
        double windSpeedScore = 0;
        double rainScore = 0;
        double visibilityScore = 0;
        int validMetricCnt = 0;

        if(temperatures != null) {
            tempScore = scoreLRBound(avg(temperatures), 30, 65);
            validMetricCnt++;
        }

        if(windSpeeds != null) {
            windSpeedScore = scoreLRBound(avg(windSpeeds), 5, 10);
            validMetricCnt++;
        }

        if(rainProbabilities != null) {
            rainScore = scoreRightBound(avg(rainProbabilities), 30, 40);
            validMetricCnt++;
        }

        if(visibilities != null) {
            visibilityScore = scoreLeftBound(avg(visibilities), 3, 8);
            validMetricCnt++;
        }

        if(validMetricCnt >= 1) {
            double score = (tempScore + windSpeedScore + rainScore + visibilityScore) / validMetricCnt;
            indexValMap.put(WeatherIndexType.SKYDIVINGINDEX, score * 10);
        }
    }

    private void setFishingIndex(List<Double> temperatures, List<Double> rainProbabilities, List<WeatherState> weatherStates) {
        double tempScore = 0;
        double rainScore = 0;
        double stateScore = 0;
        int validMetricCnt = 0;

        if(temperatures != null) {
            tempScore = scoreLRBound(avg(temperatures), 20, 65);
            validMetricCnt++;
        }

        if(rainProbabilities != null) {
            rainScore = scoreRightBound(avg(rainProbabilities), 30, 50);
            validMetricCnt++;
        }

        if(weatherStates != null) {
            stateScore = getRate(weatherStates, "cloud");
            validMetricCnt++;
        }

        if(validMetricCnt >= 1) {
            double score = (tempScore + rainScore + stateScore) / validMetricCnt;
            indexValMap.put(WeatherIndexType.FISHINGINDEX, score * 10);
        }
    }

    private void setBoatingIndex(List<Double> temperatures, List<Double> windSpeeds) {
        double tempScore = 0;
        double windSpeedScore = 0;
        int validMetricCnt = 0;

        if(temperatures != null) {
            tempScore = scoreLRBound(avg(temperatures), 30, 65);
            validMetricCnt++;
        }

        if(windSpeeds != null) {
            windSpeedScore = scoreRightBound(avg(windSpeeds), 7, 5);
            validMetricCnt++;
        }

        if(validMetricCnt >= 1) {
            double score = (tempScore + windSpeedScore) / validMetricCnt;
            indexValMap.put(WeatherIndexType.BOATINGINDEX, score * 10);
        }
    }

    private void setGameIndex() {
        int min = 6;
        int max = 10;
        Random r = new Random();
        double score = min + (max - min) * r.nextDouble();
        indexValMap.put(WeatherIndexType.GAMINGINDEX, score);
    }

}
