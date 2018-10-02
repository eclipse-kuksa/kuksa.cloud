# KUKSA APPSTORE

## Getting Started

### Infrastructure

- Java 8
- Maven
- Spring-Boot and other dependencies(data-jpa, feign client,pagination)
- Vaadin
- H2 in-memory db

### Prerequisites
Just run `AppStoreApplication.java` class.Spring boot has an embedded Tomcat instance. Spring boot uses **Tomcat7** by default, if you change Tomcat version, you have to define these configuration in **pom.xml**. But you have a few options to have embedded web server deployment instead of Tomcat like Jetty(HTTP (Web) server and Java Servlet container) or Java EE Application Server. You have to configure these replacements from default to new ones in **pom.xml**

## Key Features for this version
* Vaadin UI based Spring Boot application
* Used H2 as DBMS
* Supported Kuksa App Store account types are system admin and regular user
* Kuksa App Store user accounts are unique and must be same as vehicle platform and cloud user accounts
* User and app CRUD operations by system admin account
* Device centric app installation by system admin and regular users
* Listing of all apps or owned/installed apps by system admin and regular users
* REST API for 3rd parties such as insurance companies, car rentals, OEM producers, public authorities etc.
* Vaadin is the default UI, however it is possible to integrate app store backend services with other UI technologies
* Feign HTTP client to communicate with Hawkbit
* Description field of target instances in Hawkbit must include vehicle platform user accounts 
* Software artifacts are uploaded to Hawkbit repository
* App instances in Kuksa App Store associate to the distributions provisioned in Hawkbit and must be synchronized


## Deployment Instructions

- Clone the project:

> git clone https://github.com/eclipse/kuksa.cloud.git  


- Go to the Kuksa App Store repo:

> cd kuksa.cloud/kuksa.appstore
 

- Update hawkbit update server IP, port and credential properly in src\main\resources\application.properties file:

> hawkbit.host= {hawkbit-ip}  //default ip is localhost  
> hawkbit.port= {hawkbit-port} //default port is 8080  
> hawkbit.username= {hawkbit-user-name} // default user name is admin  
> hawkbit.password= {hawkbit-pwd} // default password is admin 

- Build Kuksa App Store jar file:

> mvn clean install

- Execute Kuksa App Store jar file:

> java -jar target/kuksa.appstore-{version}.jar



## Notes
- The app store has user authentication enabled in cloud profile. Default credentials:

> username : admin  
> passwd : admin

- This can be configured/disabled by data.sql file 

- The property hawkbit.host in application.properties is set to localhost in the repository. It should be changed to the IP address of Hawkbit Update Server used.

- Default debug mode in application.properties is set to false.
