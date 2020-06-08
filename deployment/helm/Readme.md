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

### App Store
The App Store is an applications that allows owners to manage the applications on their vehicle. Most common operations are the command to start the installation or deletion process of an app on the vehicle. The  management of the applications is handled by hawkBit. The users are handled by a Keycloak instance. 

#### Configuring the App Store
One way to open the app store is to access the public ip-address of the respective service (`releasename-appstore`) on port `8080`. The app store then redirects the owners to the Keycloak instance. In the next step the user logs in with Keycloak and then Keycloak redirects the authenticated user back to the application which in this case is the app store. To make this process work a number administration steps are necessary:

1. Set the admin credentials for Keycloak in the `values.yaml` file of the overall Helm chart under `keycloak` (`username`and `password`). Note that the default username is `keycloak` and that the Keycloak chart generates a random password if no password is explicitly set.
2. To configure Keycloak open the Admin-UI of Keycloak. This can be done by using the public ip-address of the Keycloak service (`{releasename}-keycloak-http`). Use 
```kubectl get svc -n {namespace} {releasename}-keycloak-http``` 
to get an overview of all available services. To change the service type of Keycloak and thus get a public ip-address for Keycloak one can set the service type to `LoadBalancer`in the `keycloak.keycloak.service.type` value. After opening the Keycloak endpoint select "Administration Console" on the Keycloak welcome page and login with the admin credentials from step 1.
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