FROM openjdk:11

RUN ls /
ADD target/*.jar /
RUN ls /
ADD target/weather-0.0.1.jar /weather.jar

RUN ls -la /

ENTRYPOINT ["java", "-jar", "weather.jar"]