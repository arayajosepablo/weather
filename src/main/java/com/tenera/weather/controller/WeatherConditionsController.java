package com.tenera.weather.controller;

import com.tenera.weather.dto.History;
import com.tenera.weather.model.WeatherConditions;
import com.tenera.weather.service.WeatherConditionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
@RequestMapping("v1/weather")
@Slf4j
public class WeatherConditionsController {

  private final WeatherConditionsService weatherService;

  public WeatherConditionsController(final WeatherConditionsService weatherService) {
    this.weatherService = weatherService;
  }

  @GetMapping(path = "/current")
  public ResponseEntity<WeatherConditions> getWeather(@RequestParam final String location) {

    try {
      return new ResponseEntity<>(this.weatherService.getWeatherConditions(location),
          HttpStatus.OK);
    } catch (WebClientResponseException ex) {
      log.error("Location not found: {}", location);
      return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping(path = "/history")
  public ResponseEntity<History> getTop5ByLocation(@RequestParam final String location) {
    try {
      return new ResponseEntity<>(this.weatherService.getHistoryByLocation(location),
          HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      log.error("Location not found: {}", location);
      return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
  }
}
