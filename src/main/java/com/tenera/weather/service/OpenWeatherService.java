package com.tenera.weather.service;

import com.tenera.weather.dto.OpenWeatherResponse;

public interface OpenWeatherService {

  OpenWeatherResponse getWeatherData(String placeToQuery);

}
