package com.tenera.weather.dto;

import lombok.Data;

@Data
public class Weather {

  private int id;
  private String main;
  private String description;
  private String icon;

}
