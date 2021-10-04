package com.tenera.weather.service;

import com.tenera.weather.dto.History;
import com.tenera.weather.model.WeatherConditions;

public interface WeatherConditionsService {

  WeatherConditions getWeatherConditions(String location);

  History getHistoryByLocation(String location);

}
