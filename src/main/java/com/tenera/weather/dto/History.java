package com.tenera.weather.dto;

import com.tenera.weather.model.WeatherConditions;
import java.util.List;
import lombok.Data;

@Data
public class History {

  private double averageTemp;
  private double averagePressure;
  private List<WeatherConditions> history;

}
