package com.tenera.weather.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenera.weather.dto.History;
import com.tenera.weather.dto.OpenWeatherResponse;
import com.tenera.weather.model.WeatherConditions;
import com.tenera.weather.repository.WeatherConditionsRepository;
import com.tenera.weather.service.impl.WeatherConditionsServiceImpl;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class WeatherConditionsServiceTest {

  @Mock
  private OpenWeatherService openWeatherService;

  @Mock
  private WeatherConditionsRepository weatherConditionsRepository;

  @InjectMocks
  private WeatherConditionsServiceImpl subject;

  private ObjectMapper objectMapper = new ObjectMapper();

  private static final String NON_EXISTING_LOCATION = "ABC123";

  private static final String BERLIN = "berlin";

  private static final String LONDON = "london";

  private static final double DELTA = 0;

  @Before
  public void init() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(subject, "conversionValue", 273.15);

    Mockito.when(this.weatherConditionsRepository.save(Mockito.any(WeatherConditions.class)))
        .thenReturn(null);
  }

  @Test
  public void test_getWeatherConditions_whenHappyPathAndNotUmbrellaRequired()
      throws IOException {
    final WeatherConditions expectedResult = this.buildWeatherConditions(LONDON, false,
        19.31, 1006.0);

    final OpenWeatherResponse responseFromExternalService = this.objectMapper.readValue(
        Paths.get("src/test/resources/mocks/openWeatherResponse_happyPath_notUmbrella.json")
            .toFile(), OpenWeatherResponse.class);

    Mockito.when(this.openWeatherService.getWeatherData(LONDON))
        .thenReturn(responseFromExternalService);

    final WeatherConditions result = this.subject.getWeatherConditions(LONDON);

    this.assertCommonAsserts(expectedResult, result);
    Assert.assertFalse("It does not require umbrella", result.isUmbrella());

  }

  @Test
  public void test_getWeatherConditions_whenHappyPathAndUmbrellaIsRequired()
      throws IOException {
    final WeatherConditions expectedResult = this.buildWeatherConditions(BERLIN, true,
        8.31, 1018.0);

    final OpenWeatherResponse responseFromExternalService = this.objectMapper.readValue(
        Paths.get("src/test/resources/mocks/openWeatherResponse_happyPath_withUmbrella.json")
            .toFile(), OpenWeatherResponse.class);

    Mockito.when(this.openWeatherService.getWeatherData(BERLIN))
        .thenReturn(responseFromExternalService);

    final WeatherConditions result = this.subject.getWeatherConditions(BERLIN);

    this.assertCommonAsserts(expectedResult, result);
    Assert.assertTrue("It requires umbrella", result.isUmbrella());

  }

  @Test(expected = WebClientResponseException.class)
  public void test_getWeatherConditions_whenNonExistingCity() {
    Mockito.when(this.openWeatherService.getWeatherData(NON_EXISTING_LOCATION))
        .thenThrow(WebClientResponseException.class);

    this.subject.getWeatherConditions(NON_EXISTING_LOCATION);

  }

  @Test
  public void test_getWeatherConditions_whenSendingCityAndCountry() throws IOException {
    final String locationToQuery = "london,uk";

    final WeatherConditions expectedResult = this.buildWeatherConditions("london", false,
        19.31, 1006.0);

    final OpenWeatherResponse responseFromExternalService = this.objectMapper.readValue(
        Paths.get("src/test/resources/mocks/openWeatherResponse_happyPath_notUmbrella.json")
            .toFile(), OpenWeatherResponse.class);

    Mockito.when(this.openWeatherService.getWeatherData(locationToQuery))
        .thenReturn(responseFromExternalService);

    final WeatherConditions result = this.subject.getWeatherConditions(locationToQuery);

    this.assertCommonAsserts(expectedResult, result);
    Assert.assertFalse("It does not require umbrella", result.isUmbrella());

    // It should send the city without the country to the repository
    Mockito.verify(this.weatherConditionsRepository).save(expectedResult);

    // It should send the city and country to the external service
    Mockito.verify(this.openWeatherService).getWeatherData(locationToQuery);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getHistoryByLocation_whenNotData() {
    Mockito.when(this.weatherConditionsRepository.findTop5ByLocationOrderByCreatedTimestampDesc(
        NON_EXISTING_LOCATION)).thenReturn(Collections.emptyList());

    this.subject.getHistoryByLocation(NON_EXISTING_LOCATION);
  }

  @Test
  public void test_getHistoryByLocation_whenHappyPath() {
    final List<WeatherConditions> weatherConditionsList = new ArrayList<>();
    weatherConditionsList.add(this.buildWeatherConditions(BERLIN, true, 8.17, 1008));
    weatherConditionsList.add(this.buildWeatherConditions(BERLIN, false, 18.43, 1025));
    weatherConditionsList.add(this.buildWeatherConditions(BERLIN, false, 15.68, 1001));
    weatherConditionsList.add(this.buildWeatherConditions(BERLIN, true, 15.67, 1002));
    weatherConditionsList.add(this.buildWeatherConditions(BERLIN, true, 5.67, 1007));

    final History expectedHistory = new History();
    expectedHistory.setHistory(weatherConditionsList);
    expectedHistory.setAveragePressure(1008.6);
    expectedHistory.setAverageTemp(12.724);

    Mockito.when(
            this.weatherConditionsRepository.findTop5ByLocationOrderByCreatedTimestampDesc(BERLIN))
        .thenReturn(weatherConditionsList);

    final History result = this.subject.getHistoryByLocation(BERLIN);

    Assert.assertEquals("Average Pressure should be equals", expectedHistory.getAveragePressure(),
        result.getAveragePressure(), DELTA);
    Assert.assertEquals("Average Temp should be equals", expectedHistory.getAverageTemp(),
        result.getAverageTemp(), DELTA);
    Assert.assertEquals("Should contain 5 elements", expectedHistory.getHistory().size(),
        result.getHistory().size());
    Assert.assertTrue(result.getHistory().contains(expectedHistory.getHistory().get(0)));
    Assert.assertTrue(result.getHistory().contains(expectedHistory.getHistory().get(1)));
    Assert.assertTrue(result.getHistory().contains(expectedHistory.getHistory().get(2)));
    Assert.assertTrue(result.getHistory().contains(expectedHistory.getHistory().get(3)));
    Assert.assertTrue(result.getHistory().contains(expectedHistory.getHistory().get(4)));

  }

  private void assertCommonAsserts(final WeatherConditions expectedResult,
      final WeatherConditions result) {
    Assert.assertEquals("Pressure should be equals", expectedResult.getPressure(),
        result.getPressure(), DELTA);
    Assert.assertEquals("Temp should be equals", expectedResult.getTemp(), result.getTemp(), DELTA);
    Assert.assertEquals("Location should be equals", expectedResult.getLocation().toLowerCase(),
        result.getLocation().toLowerCase());
  }

  private WeatherConditions buildWeatherConditions(final String location, final boolean umbrella,
      final double temp, final double pressure) {
    final WeatherConditions weatherConditions = new WeatherConditions();
    weatherConditions.setLocation(location);
    weatherConditions.setUmbrella(umbrella);
    weatherConditions.setTemp(temp);
    weatherConditions.setPressure(pressure);

    return weatherConditions;
  }

}
