package com.tenera.weather.dto;

import java.util.List;
import lombok.Data;

@Data
public class OpenWeatherResponse {

  private Coord coord;
  private List<Weather> weather;
  private String base;
  private Main main;
  private int visibility;
  private Wind wind;
  private Clouds clouds;
  private int dt;
  private Sys sys;
  private int timezone;
  private int id;
  private String name;
  private int cod;

}
