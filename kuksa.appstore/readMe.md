# KUKSA APPSTORE

## Getting Started

### Infrastructure

##### Java 8
##### Maven
##### Spring-Boot and other dependencies(data-jpa, feign client,pagination)
##### Vaadin
##### H2 in-memory db

### Prerequisites
Just run `AppStoreApplication.java` class.Spring boot has an embedded Tomcat instance. Spring boot uses **Tomcat7** by default, if you change Tomcat version, you have to define these configuration in **pom.xml**. But you have a few options to have embedded web server deployment instead of Tomcat like Jetty(HTTP (Web) server and Java Servlet container) or Java EE Application Server. You have to configure these replacements from default to new ones in **pom.xml**

## Clone and build App Store

git clone <repo link>
cd <file>
mvn clean install

## Start App Store
java -jar <jarPath>
## Notes
The app store has user authentication enabled in cloud profile. Default credentials:

username : admin
passwd : admin

This can be configured/disabled by data.sql file 

The property hawkbit.host in application.properties is set to localhost in the repository. It should be changed to the IP address of Hawkbit Update Server used.

Default debug mode in application.properties is set to false.
