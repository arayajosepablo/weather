package com.tenera.weather.service.impl;

import com.tenera.weather.dto.History;
import com.tenera.weather.dto.OpenWeatherResponse;
import com.tenera.weather.enums.WeatherConditionsConstants;
import com.tenera.weather.model.WeatherConditions;
import com.tenera.weather.repository.WeatherConditionsRepository;
import com.tenera.weather.service.OpenWeatherService;
import com.tenera.weather.service.WeatherConditionsService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class WeatherConditionsServiceImpl implements WeatherConditionsService {

  private final OpenWeatherService openWeatherService;

  private final WeatherConditionsRepository weatherConditionsRepository;

  @Value("${weather.kelvin.celsius}")
  private double conversionValue;

  private static final String SPLITTER = ",";

  @Autowired
  public WeatherConditionsServiceImpl(final OpenWeatherService openWeatherService,
      final WeatherConditionsRepository weatherConditionsRepository) {
    this.openWeatherService = openWeatherService;
    this.weatherConditionsRepository = weatherConditionsRepository;
  }

  @Override
  public WeatherConditions getWeatherConditions(final String location)
      throws WebClientResponseException {

    final OpenWeatherResponse response = this.openWeatherService.getWeatherData(location);

    final WeatherConditions conditions = this.convertResponse(response);
    conditions.setLocation(this.getLocationWithoutCountry(location.toLowerCase()));
    this.weatherConditionsRepository.save(conditions);

    return conditions;
  }

  @Override
  public History getHistoryByLocation(final String location)
      throws IllegalArgumentException {

    final List<WeatherConditions> weatherConditionsList =
        this.weatherConditionsRepository.findTop5ByLocationOrderByCreatedTimestampDesc(location);

    if (weatherConditionsList.isEmpty()) {
      throw new IllegalArgumentException();
    }

    final History averageResponse = new History();
    averageResponse.setHistory(weatherConditionsList);

    averageResponse.setAverageTemp(weatherConditionsList.stream()
        .map(WeatherConditions::getTemp)
        .reduce(0.0, Double::sum)/weatherConditionsList.size());

    averageResponse.setAveragePressure(weatherConditionsList.stream()
        .map(WeatherConditions::getPressure)
        .reduce(0.0, Double::sum)/weatherConditionsList.size());

    return averageResponse;
  }

  private WeatherConditions convertResponse(final OpenWeatherResponse response) {

    final WeatherConditions conditions = new WeatherConditions();

    if (response.getMain() != null) {
      conditions.setPressure(response.getMain().getPressure());
      conditions.setTemp(this.fromKelvinToCelsius(response.getMain().getTemp()));
    }

    if (response.getWeather() != null) {
      conditions.setUmbrella(
          response.getWeather().stream()
              .anyMatch(weather ->
                  WeatherConditionsConstants.THUNDERSTORM.getValue().equals(weather.getMain()) ||
                      WeatherConditionsConstants.DRIZZLE.getValue().equals(weather.getMain()) ||
                      WeatherConditionsConstants.RAIN.getValue().equals(weather.getMain())
              ));
    }

    return conditions;
  }

  private double fromKelvinToCelsius(final double tempInKelvin) {
    double result = (tempInKelvin - this.conversionValue) * Math.pow(10, 2);
    result = Math.floor(result);
    return result / Math.pow(10, 2);
  }

  private String getLocationWithoutCountry(final String location) {
    return location.contains(SPLITTER) ? location.split(SPLITTER)[0] : location;
  }

}
