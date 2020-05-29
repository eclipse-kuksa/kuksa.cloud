# Kuksa Cloud Helm Chart
This helm chart can be used to deploy components of the Kuksa Cloud to Kubernetes cluster. The chart is developed for Helm 3. To do a deployment you need to perform the following steps:
- 1. build and push the Kuksa components to a private container registry and add the credentials to the values.yaml file under `imageCredentials`
- 2. fetch the dependencies of the chart by running: 

```bash
helm dep up ./kuksa-cloud
```
- 3. install/release the chart by running:
```bash
helm install <release name> ./kuksa-cloud
```
If you want to deploy to another namespace than default use:
```bash 
helm install <release name>  ./kuksa-cloud -n <namespace>
```

## Values
The Kuksa Cloud has multiple dependencies and components. Depending on the use case it is possible that one might not need all of them. In the `values.yaml` it is possible to control that a component should not be deployed by setting the entry `components.<name of component>.enabled` to `false`. 

### Hono-InfluxDB-Connector
On part of the Kuksa Cloud that is developed within the Kuksa project is the Hono-InfluxDB-Connector. More information on the connector can be found in the [respective folder](../../utils/hono-influxdb-connector/README.md). Within the `values.yaml` of this chart it is possible to configure the connector with the values under 'connector'. Note that the list in `connections` can be extended by maps with similar key as the ones in the default `values.yaml`.

In most cases it takes some time for the InfluxDB to start. Because the connector is not able to connect to the database during that start time, there might happen several restarts of the connector. In most cases the connector pod becomes stable after 3 to 5 restarts. Alternatively, it might help to manually delete the pod once the InfluxDB Pod becomes ready and then to wait for the automatic creation of a new connector pod. 