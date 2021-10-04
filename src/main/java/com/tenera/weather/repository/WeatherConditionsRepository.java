package com.tenera.weather.repository;

import com.tenera.weather.model.WeatherConditions;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherConditionsRepository extends JpaRepository<WeatherConditions, String> {

  List<WeatherConditions> findTop5ByLocationOrderByCreatedTimestampDesc(String location);

}
