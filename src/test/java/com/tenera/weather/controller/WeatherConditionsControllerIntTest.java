package com.tenera.weather.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenera.weather.dto.History;
import com.tenera.weather.dto.OpenWeatherResponse;
import com.tenera.weather.model.WeatherConditions;
import com.tenera.weather.service.OpenWeatherService;
import java.io.IOException;
import java.nio.file.Paths;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class WeatherConditionsControllerIntTest {

  private static final String BASE_URL = "/v1/weather/%s";

  final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private OpenWeatherService openWeatherService;

  private static final int NOT_FOUND = 404;

  @Test
  public void test_getCurrentLocation_whenHappyPathIncludingCountry() throws Exception {

    final OpenWeatherResponse responseFromExternalService = this.getOpenWeatherData(
        "src/test/resources/mocks/openWeatherResponse_happyPath_notUmbrella.json");

    final WeatherConditions expectedResponse = this.getWeatherConditions(
        "src/test/resources/mocks/weatherConditionsServiceResponse_happyPathNotUmbrella.json");

    Mockito.when(this.openWeatherService.getWeatherData("london,uk"))
        .thenReturn(responseFromExternalService);

    this.mockMvc.perform(
            MockMvcRequestBuilders.get(String.format(BASE_URL, "/current?location=london,uk"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath("temp", Matchers.is(expectedResponse.getTemp())))
        .andExpect(MockMvcResultMatchers.jsonPath("pressure",
            Matchers.is(expectedResponse.getPressure())))
        .andExpect(MockMvcResultMatchers.jsonPath("umbrella",
            Matchers.is(expectedResponse.isUmbrella())));
  }

  @Test
  public void test_getCurrentLocation_whenHappyPathWithoutCountry() throws Exception {

    final OpenWeatherResponse responseFromExternalService = this.getOpenWeatherData(
        "src/test/resources/mocks/openWeatherResponse_happyPath_withUmbrella.json");

    final WeatherConditions expectedResponse = this.getWeatherConditions(
        "src/test/resources/mocks/weatherConditionsServiceResponse_happyPathWithUmbrella.json");

    Mockito.when(this.openWeatherService.getWeatherData("berlin"))
        .thenReturn(responseFromExternalService);

    this.mockMvc.perform(
            MockMvcRequestBuilders.get(String.format(BASE_URL, "/current?location=berlin"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath("temp", Matchers.is(expectedResponse.getTemp())))
        .andExpect(MockMvcResultMatchers.jsonPath("pressure",
            Matchers.is(expectedResponse.getPressure())))
        .andExpect(MockMvcResultMatchers.jsonPath("umbrella",
            Matchers.is(expectedResponse.isUmbrella())));
  }

  @Test
  public void test_getCurrentLocation_whenNonExistingCity() throws Exception {
    Mockito.when(this.openWeatherService.getWeatherData("xyz,de"))
        .thenThrow(WebClientResponseException.class);

    this.mockMvc.perform(
            MockMvcRequestBuilders.get(String.format(BASE_URL, "/current?location=xyz,de"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().is(NOT_FOUND));

  }

  @Test
  public void test_getTop5ByLocation_whenException() throws Exception {
    this.mockMvc.perform(
            MockMvcRequestBuilders.get(String.format(BASE_URL, "/history?location=abc,de"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().is(NOT_FOUND));
  }

  @Test
  public void test_getTop5ByLocation_whenHappyPath() throws Exception {

    final History expectedResult = this.getHistoryData(
        "src/test/resources/mocks/historyResponse_happyPath.json");

    final String result = this.mockMvc.perform(
            MockMvcRequestBuilders.get(String.format(BASE_URL, "/history?location=barcelona"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn().getResponse().getContentAsString();

    Assert.assertEquals(this.objectMapper.writeValueAsString(expectedResult), result);

  }

  @Test
  public void test_getTop5ByLocation_whenOnlyOneRecordPerCity() throws Exception {

    final History expectedResult = this.getHistoryData(
        "src/test/resources/mocks/historyResponse_happyPathWhenOnlyOneCity.json");

    final String result = this.mockMvc.perform(
            MockMvcRequestBuilders.get(String.format(BASE_URL, "/history?location=moscow"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn().getResponse().getContentAsString();

    Assert.assertEquals(this.objectMapper.writeValueAsString(expectedResult), result);

  }

  private WeatherConditions getWeatherConditions(final String file) throws IOException {
    return this.objectMapper.readValue(Paths.get(file).toFile(), WeatherConditions.class);
  }

  private OpenWeatherResponse getOpenWeatherData(final String file) throws IOException {
    return this.objectMapper.readValue(Paths.get(file).toFile(), OpenWeatherResponse.class);
  }

  private History getHistoryData(final String file) throws IOException {
    return this.objectMapper.readValue(Paths.get(file).toFile(), History.class);
  }

}
