# InfluxDB REST Adapter

This repository offers a restful way to query an influxDB database.
The result of a successful query is a JSON string according to the [w3c VIS draft](https://w3c.github.io/automotive/vehicle_data/vehicle_information_service.html).
Entries in the database have to have fully qualified names and be separated by `.`, e.g. `signal.obd.value = 42` to be returned by this service.

## Specify an InfluxDB Database

To query an InfluxDB instance the REST adapter needs to know its destination.
Therefore, change the URL, port and database name in the [application.properties](src/main/resources/application.properties) file accordingly.

````
influxDB.url=my-database.com
influxDB.port=8086
influxDB.db.name=devices
````

Besides the `application.properties` file environment variables can also be used to configure the service.
The following table contains the options available.

| properties name  | environment variable | description                                   |
|------------------|----------------------|-----------------------------------------------|
| influxDB.url     | INFLUXDB_URL         | url of the influxDB to query                  |
| influxDB.port    | INFLUXDB_PORT        | port of the influxDB to query                 |
| influxDB.db.name | INFLUXDB_DB_NAME     | name of the database                          |

## How to Query?

Send a GET request to `example.com:8080/devices` to retrieve a list of all available device IDs.
To access the full data of a device with the `id=42` go to `example.com:8080/devices/42`.
As InfluxDB stores data associated with a timestamp entries can be queried using three ways.
First, without any additional parameters as shown above returns the latest value for every field in the database.
Second, an exact point in time using the `at` parameter, e.g. `example.com:8080/devices/42?at=2018-05-22T09:09:19.299Z`.
Third, a time interval using `from`, `to` or both of them.
For example `example.com:8080/devices/42?from=2018-05-18T09:09:19.299Z&to=2018-05-22T09:09:19.299Z`.
The required format of a timestamp is defined in [RFC3339](https://www.ietf.org/rfc/rfc3339.txt).
