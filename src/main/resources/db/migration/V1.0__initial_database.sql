CREATE TABLE weather_conditions (
  request_id        varchar(36)                  NOT NULL,
  location          varchar(50)                  NOT NULL,
  temp              decimal (10,2)               NOT NULL,
  pressure          decimal (10,2)               NOT NULL,
  umbrella          boolean                      NOT NULL,
  created_timestamp timestamp without time zone  DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (request_id)
);

CREATE INDEX location_idx ON weather_conditions (location);