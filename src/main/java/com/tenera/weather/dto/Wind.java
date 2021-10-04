package com.tenera.weather.dto;

import lombok.Data;

@Data
public class Wind {

  private double speed;
  private int deg;
  private double gust;

}
