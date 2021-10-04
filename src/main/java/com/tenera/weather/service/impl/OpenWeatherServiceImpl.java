package com.tenera.weather.service.impl;

import com.tenera.weather.dto.OpenWeatherResponse;
import com.tenera.weather.service.OpenWeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class OpenWeatherServiceImpl implements OpenWeatherService {

  private final WebClient webClient;

  private final String apiKey;

  private final String baseUrl;

  private static final String QUERY_PARAM_QUERY = "q";

  private static final String QUERY_PARAM_APPID = "APPID";

  public OpenWeatherServiceImpl(final WebClient webClient,
      @Value("${weather.api.key}") final String apiKey,
      @Value("${weather.base.url}") final String baseUrl) {
    this.webClient = webClient;
    this.apiKey = apiKey;
    this.baseUrl = baseUrl;
  }

  @Override
  public OpenWeatherResponse getWeatherData(final String placeToQuery)
      throws WebClientResponseException {
    log.info("Getting data for: {}", placeToQuery);

    return this.webClient.get()
        .uri(this.baseUrl, uriBuilder -> uriBuilder.pathSegment("")
            .queryParam(QUERY_PARAM_QUERY, placeToQuery)
            .queryParam(QUERY_PARAM_APPID, this.apiKey).build())
        .retrieve()
        .bodyToMono(OpenWeatherResponse.class)
        .block();
  }
}
