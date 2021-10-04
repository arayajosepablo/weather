# Weather Application

## Assumptions
- Temperature and pressure are considered doubles

## How to run the microservice
- First: the jar file should be created, run the command `mvn clean install`.
- Second: the Docker image should be built, run the command `docker build -t tenera .`.
- Last: run the created image, use the command: `docker run -p 8080:8080 tenera`.

## Endpoints:
In order the get the data from the microservice the next endpoints can be used with the included CURL commands.
### 1. GET /current?location=Berlin
Gets the data from Open Weather external service and parses it to a more readable format.
Notes:
- The location could include the country, for example: `/current?location=Berlin,de`
- The location is not case-sensitive.
```
curl --location --request GET 'http://localhost:8080/v1/weather/current?location=berlin' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json'
```

### 2. GET /history?location=London
Gets the historical data of the latest 5 requests per city, also displays the average of these records.
```
curl --location --request GET 'localhost:8080/v1/weather/history?location=berlin' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json'
```

## Approach to build the solution

### Table
- weather_conditions

Column Name  | Column Type
------------- | ------------- 
request_id        | varchar (36)                |
location          | varchar (50)                |
temp              | decimal (10,2)              |
pressure          | decimal (10,2)              |
umbrella          | boolean                     |
created_timestamp | timestamp without time zone |

### Explanation
- I decided to use `WebFlux` instead of `RestTemplate`, because the second one is going to be deprecated and `WebFlux` is going to be the standard for SpringBoot applications. Also in case that in the future we need to build some reactive endpoints we already have some required setup.
- `Flyway` is being used to manage the database scripts, and history.
- There is one service which unique responsibility is to communicate with the OpenWeather API: `OpenWeatherService.java`.
- There is a second service that handles the business logic, and communicates with the repository: `WeatherConditionsService.java`.
- Just for the sake of the simplicity an in-memory database is used: `H2`.
- I used Mockito for unit testing, and Spring for integration testing.
- The field `request_id` is `varchar` because it's using `UUID` as the ID.
- The field `created_timestamp` is used to sort the records, and get the latest 5 by `location`.
- The field `location` only saved the city, and in lower case to keep it consistent.
- `404 HTTP status` is sent when the city does not exist for the first endpoint, and when there is not historical data for the second endpoint.  

### Things that can be improved
- Use a persistent DB instead of H2 to run the application.
- Setup Swagger for the documentation of the endpoints.
- More testing can be added.
- Use a cache service for the historical data.
- Do not expose the OpenWeather API key as plain text.

## Production ready micro service
- It should be containerized, using tools as Docker.
- It should have a defined CI/CD pipeline where the tests are runs, and if everything is OK the image is built, and deploy.
- The CI/CD pipeline should contain define steps, for instance: first deploy to dev, then promote to stage, then promote to production (the number of envs could vary).
- It should have tools such as SonarQube to evaluate the source code.
- It should use a persistent DB such as Postgre or MySQL, among others. An in-memory DB can be used for integration testing.
- Properly handling the logging, for instance turning off debug logs and hibernate logs.
- To have an application.properties file for each of the environments.

## Deploying the application
- The application should be containerized.
- The environment is on EKS.
- Initialize Terraform instance and run the defined plan: 
    ```
    terraform init
    terraform plan -out planfile
    ```
  Terraform plan should include the AWS resources, such as: VPC, EKS cluster, and so on.
- If everything went fine, then apply configuration using: `terraform apply`.
