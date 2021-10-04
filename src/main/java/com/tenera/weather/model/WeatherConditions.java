package com.tenera.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

@Data
@Entity
@Table(name = "weather_conditions")
public class WeatherConditions extends Auditable {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  @Column(name = "request_id", nullable = false, unique = true)
  @JsonIgnore
  private String requestId;

  @Column(name = "location", nullable = false)
  @JsonIgnore
  private String location;

  @Column(name = "temp", nullable = false)
  private double temp;

  @Column(name = "pressure", nullable = false)
  private double pressure;

  @Column(name = "umbrella", nullable = false)
  private boolean umbrella;

}
