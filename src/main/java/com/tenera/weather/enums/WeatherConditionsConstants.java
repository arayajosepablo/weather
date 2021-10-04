package com.tenera.weather.enums;

public enum WeatherConditionsConstants {
  THUNDERSTORM("Thunderstorm"),
  DRIZZLE("Drizzle"),
  RAIN("Rain");

  private String value;

  WeatherConditionsConstants(final String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }

}
