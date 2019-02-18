# Warning of Malfunction Indicator Light

## Test Scenario

In this use-case the car sends telemetry data to the Kuksa cloud backend to check for a possible malfunction of the car.
On the occurrence of a malfunction the driver will get notified by email including the next garage to get its car fixed.

For this use-case the malfunction of the car is indicated by the *Malfunction Indicator Light* (MIL) defined in the [w3c VIS draft](https://w3c.github.io/automotive/vehicle_data/vehicle_information_service.html).
A value of `true` or `1` indicates a malfunction in the car and `false` or `0` indicates that the car is running fine.
The car sends periodically telemetry data to the Kuksa cloud including the MIL value.
Those telemetry data is stored in an InfluxDB database series based on the ID of the device.
An InfluxDB REST adapter offers an interface of the data to the application developers.
In this use-case the application simply checks for a change in the MIL value to `true`, searches a garage closeby based on the current location of the car and notifies the user via email about the possible malfunction.
To send an email to the driver this repository contains a simple service with a REST interface to send email from an arbitrary email using SMTP.

## Requirements

To run this use-case there are some requirements to meet.

* A running InfluxDB instance
    * A database with the device IDs
    * Unrestricted access to the query endpoint
* An email address to send notifications from
    * SMTP server accessible
    * Credentials to authorize
* A Google Maps API key retrievable from [this guide](https://developers.google.com/maps/documentation/geocoding/get-api-key)

## Content

````
malfunction-indicator-light
    ├── deploy/
    ├── influxdb-rest/
    ├── mail-notification/
    ├── mil-service/
    ├── README.md
    └── settings.gradle
````

This use-case contains three projects and their respective deployments:

1. *influxdb-rest*: a REST wrapper for an InfluxDB instance
2. *mail-notification*: a REST service to send emails from
3. *mil-service*: sample application that checks for a possible malfunction in the car
4. *deploy*: deployment templates for kubernetes

All of the projects are can be build using the `gradle build` call.