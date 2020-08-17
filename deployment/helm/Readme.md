# Kuksa Cloud Helm Chart
This helm chart can be used to deploy the main components of the Kuksa Cloud to a Kubernetes cluster. The chart is developed for Helm 3. For an overview of the different deployable components see below. To do a deployment you need to perform the following steps:
- 1. build and push the Kuksa specifc containers to a private container registry and add the credentials to the values.yaml file under `imageCredentials`
- 2. fetch the dependencies of the chart by running: 

```bash
helm dep up ./kuksa-cloud
```
- 3. install/release the chart by running:
```bash
helm install <release name> ./kuksa-cloud
```
If you want to deploy to another namespace than `default` use:
```bash 
helm install <release name>  ./kuksa-cloud -n <namespace>
```

## Values and Components
The Kuksa Cloud has multiple dependencies and components. Depending on the use case it is possible that one might not need all of them. In the `values.yaml` it is possible to control that a component should not be deployed by setting the entry `components.<name of component>.enabled` to `false`. 

### Hono-InfluxDB-Connector
On part of the Kuksa Cloud that is developed within the Kuksa project is the Hono-InfluxDB-Connector. More information on the connector can be found in the [respective folder](../../utils/hono-influxdb-connector/README.md). In short, the connector accepts data from Elipse Hono (northbound AMQP 1.0 API) and writes it to an InfluxDB. Within the `values.yaml` of this chart it is possible to configure the connector with the values under 'connector'. Note that the list in `connections` can be extended by maps with similar key as the ones in the default `values.yaml`.

In most cases it takes some time for the InfluxDB to start. Because the connector is not able to connect to the database during that start time, there might happen several restarts of the connector. In most cases the connector pod becomes stable after 3 to 5 restarts. Alternatively, it might help to manually delete the pod once the InfluxDB Pod becomes ready and then to wait for the automatic creation of a new connector pod. 

### App Store
The App Store is an applications that allows owners to manage the applications on their vehicle/device. Most common operations are the command to start the installation or deletion process of an app on the vehicle. The  management of the applications is handled by hawkBit. The users are handled by a Keycloak instance. 

#### Configuring the App Store
One way to open the app store is to access the public ip-address of the respective service (`releasename-appstore`) on port `8080`. Since Keylcoak handles the login, the app store then needs to redirect the user to the Keycloak instance. In the next step the user logs in with Keycloak and then Keycloak redirects the authenticated user back to the application which in this case is the app store. To make this process work a number administration and configuration steps are necessary:

1. Set the admin credentials for Keycloak in the `values.yaml` file of the overall Helm chart under `keycloak` (`username`and `password`). Note that the default username is `keycloak` and that the Keycloak chart generates a random password if no password is explicitly set.
2. To configure Keycloak select "Administration Console" on the Keycloak welcome page and login with the admin credentials from step 1. . One can access the the welcome page by using the public ip-address either of the Ambassdor gateway (see below) or of the Keycloak service (`{releasename}-keycloak-http`) itself. For the second option use 
```kubectl get svc -n {namespace} {releasename}-keycloak-http``` 
to get an overview of all available services. To change the service type of Keycloak and thus get an individual public ip-address for Keycloak one can set the service type to `LoadBalancer`in the `keycloak.keycloak.service.type` value. 
3. Create a new realm in Keycloak (e.g. app-store) with the "Add realm" button which becomes visible when one does a moser over over the name of the current realm. By default the current realm is "Master".
4. Click on "Select file" next to "Import" and import a preconfigured Keycloak configuration (e.g. from `kuksa-cloud/realm-export-app-store.json`). Finish this step by clicking on the "Create" button.
5. Create a user in the created realm and assign it at least the roles: `offline_access`, `ROLE_ADMIN`, `ROLE_USER`, `uma_authorization`. For regular app store user one should leave out the `ROLE_ADMIN` role. To create a new user go to "Users" and then click "Add user" in the top of the user overview. To change the role assignment, go to the user and then select `Role Mappings`. One should also set a password for the user in the "Credentials" tab.
6. Now one needs to set the redirect url to which Keycloak should allow redirects after a user signed in with Keycloak. The respective client has been created as part of the import process in step 4 with the name `app-store`. But one still needs to set some configuration on the client. To do so one can go to to "Clients" in the left menu and then select the client in the table (default "app-store"). In the redirect and the base url field one then needs to replace `localhost` with the actual address (e.g. load balancer ip-address or DNS name) under which the app store is reachable. Do not forget to click "Save" to apply the changes. hThe default port is 8080 and should be adapted accordingly if it has been changed. To get the load balancer ip run `kubectl get svc -n {namespace}  {releasename}-app-store`.
7. For the chosen authorization flow the client (in this case the app store) and the authorization server (in this case Keycloak) need to share a secret. One should generate or set this secret while configuring the client in the `Credentials` tab. 
8. To configure the app store to interact with the Keycloak instance one needs to set the respective values in the `values.yaml`  of the Kuksa chart which are under `appstore.keycloak`. The `realm` should match the name of the newly created realm (default `app-store-realm`) where the client is configured. The field `resource` is the name of the configured client in Keycloak (default `app-store`). The `url` field points to the authorization endpoint of Keycloak and typically ends with `/auth`. For the `credentials.secret` field one uses the secret that has been set or created for the client in Keycloak (see step 7). The `principalAttribute` can remain unchanged to its default value `prefered_username`.
9. Once all configurations have been set, apply the updated information by running: 
```helm upgrade {name of release}```.
10. Restart the app store to let it load the updated configuration for the connection to Keycloak. One way to achieve this is to execute: `kubectl delete pod -n {namespace} {releasename}-app-store-{string}` deleting the current pod for the app store container. After a couple of seconds Kubernetes should then start a new container for the app store.
11. One can now try out the flow by accessing the address of the app store under port `8080`. This should lead to a redirect to Keycloak were one can use the credentials of the newly created user to login. Once the login succeeds the user should get redirected back to the app store. If the user logs in for the first time, Keycloak may ask the user to change the password and to give the consent to share data with app store.

Notes:
- All addresses (app store and Keycloak) need to be accessible from the user's machine which in most scnearios means that they should be reachable from the internet.
- If the redirect after the login back to the app store fails with an error like `too many redirects`, the reason could be that the app store has a wrong secret configured. 

# Ambassador Gateway
In theory, one could provision an ip-address for each service of the Kuksa cloud which one wants to access from the internet. However, ip-addresses are limited and may add addtional cost. To enable users to access the Kuksa Cloud services with a single IP-address, the Cloud makes use of the [Ambassador](https://www.getambassador.io) project as API gateway. 
The Ambassador allows the routing on the TCP and the HTTP layer. The Kuksa Cloud requires the TCP based routing because it uses a number of non-HTTP protocols such as MQTT or AMQP 1.0. To signal which service one wants to call on the TCP layer one can specify the port. The default configuration has the following port mappings. Note, that some of the ports are not a standard port because some of the services share the same default port. 


| Service               | Port on Ambassador (external)  | Port on Pod (internal) |
|-----------------------|-------|-------|
| Grafana | 3000  | 3000 |
| InfluxDB | 8086  | 8086 |
| Hono Dispatch Router | 5671 | 15671 |
| MQTT Adapter | 1883 | 1883 |
| HTTP Adapter | 18080 | 18080 |
| Hono Device Registry | 28080 | 28080 |
| hawkBit | 38080 | 80 |
| Keycloak | 48080 | 80 |
| App Store | 58080 | 8080 |

To make the access to the Ambassador more user-friendly one can also obtain a domain. The process of buying and managing such a domain is beyond the scope of the documentation. 
Once, one has a domain availale one can specify it with the value `dns.domain`.
For the HTTP based services it then also possible to access the services through a subdomain if the public IP of the Ambassador gateway can be accessed with that host name. For example, if you registered and set the domain example.org the subdomain for the app store would be app-store.example.org. By the default the following subdomains exist:

| Service | Subdomain |
|----------|----------|
| Grafana | grafana |
| HTTP Adapter | http-adapter |
| Hono Device Registry | device-registry |
| hawkBit | hawkbit |
| Keycloak | keycloak |
| App Store | app-store |


# TLS 
To encrypt the traffic from and to the Kuksa cloud one needs to make use of TLS e.g. by using HTTPS. 

## Cert Manager
For the enablement of TLS one needs valid certificates for the domain under which the endpoints are hosted. In the Kuksa Cloud we use an instance of the [cert-manager](https://cert-manager.io). This tool is able to fetch new certificates and renew them by interacting with an certificate authority e.g. in the case of the standard deployment Let's encrypt.  

## enable and install Cert Manager
The cert-manager is not enabled by default because we try to make the deployment possible even when no domain or CA is available. To include the cert-manager in the deployment go to the `Chart.yaml` file of the chart and uncomment the dependency `cert-manager`. The cert-manager further requires so-called custom resource definitions (CRD). These are installed with the cert-manager if one sets cert-manager.installCRDs to true. One can also install the CRDs by running: `kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/v0.15.2/cert-manager.crds.yaml`

## Cert Manager: getting certificates - ACME
The cert-manager is able to automatically request and manage certificates from Let's encrypt through the ACME challenge. The Kuksa Cloud uses the [DNS-01 challenge](https://cert-manager.io/docs/configuration/acme/dns01/). As part of the DNS-01 challenge the cert-manager needs to prove to the CA (e.g. Let's encrypt) that it has control over the DNS entries for the domain for which it requests new certificates. The configuration and execution of the ACME challenge heavily depends on the provider of the DNS entries for the used domain. 

Currently, the cert-manager setup of the Eclipse Kuksa cloud only support the Azure DNS Zone service. However, we especially welcome contributions to add support for other DNS providers. 
### ACME and Azure
Within Azure the DNS entries are managed in a `DNS Zone` resource. So to enable the ACME challenge and thus the certificate request the cert-manager needs access to the DNS-Zone for the used domain. Further steps are explained in the [documentation of the cert-manager](https://cert-manager.io/docs/configuration/acme/dns01/azuredns/). In short, you need to create a new Azure service principal, give it the role "DNS Zone Contributor" for the DNS Zone and give the credentials as secret to the cert-manager. In the Helm chart one can set the credentials of the service principal with the values `certificates.azureSpId` and `certificates.azureSpPassword`.

To actually enable the usage of certificates one further needs to set the values `dns.sslForTcpMappings` (for TLS termination on TCP layer) or `dns.sslForHttpMappings` (for TLS termination for HTTP connections) to true.

In our setup we used an Azure `App Service Domain` to buy an domain. But one could also get the domain somewhere else and point it to an DNS Zone in Azure. In either case the situation is so specific to the use case that we decided to not include the creation of the DNS Zone or even a domain into the Azure specific Terraform deployment.  

### Note regarding TLS termination for Keycloak over TCP
We experienced issues for the connection to Keycloak when terminating the SSL connection on TCP layer on a non-standard port (e.g. 48080). Due to that we decided to disable SSL for the Keycloak connection even if 'sslForTcpMappings' is set to true. You can still enable TLS for the connection to Keycloak when using the HTTP specific subdomain under `keycloak.your-domain` and having `sslForHttpMapping` enabled. If TLS is enabled with the subdomain we then recommend to completly disable the TCP mapping for Keycloak by setting `keycloak.tcpMappingActive` to false to avoid unencypted traffic.
