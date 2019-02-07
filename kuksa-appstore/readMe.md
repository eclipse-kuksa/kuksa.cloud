# KUKSA APPSTORE

## Getting Started

### Infrastructure

- Java 8
- Maven
- Spring-Boot and other dependencies(data-jpa, feign client,pagination)
- Vaadin
- Swagger (for Rest API documentation)

### Prerequisites
Just run `AppStoreApplication.java` class.Spring boot has an embedded Tomcat instance. Spring boot uses **Tomcat7** by default, if you change Tomcat version, you have to define these configuration in **pom.xml**. But you have a few options to have embedded web server deployment instead of Tomcat like Jetty(HTTP (Web) server and Java Servlet container) or Java EE Application Server. You have to configure these replacements from default to new ones in **pom.xml**

## Key Features for this version
* Vaadin UI based Spring Boot application
* Used H2 as DBMS. 
* Supported Kuksa App Store account types are system admin and normal user and group user
* Kuksa App Store user accounts are unique and must be same as vehicle platform and cloud user accounts
* User and app CRUD operations by system admin account
* Device centric app installation by system admin, regular users and group users
* Listing of all apps or owned/installed apps by system admin, regular users and group users
* REST API (including Swagger documentation) for 3rd parties such as insurance companies, car rentals, OEM producers, * public authorities etc.
* Vaadin is the default UI, however it is possible to integrate app store backend services with other UI technologies
* Feign HTTP client to communicate with Hawkbit
* Description field of target instances in Hawkbit must include vehicle platform user accounts
* Software artifacts are uploaded to Hawkbit repository
* App instances in Kuksa App Store associate to the distributions provisioned in Hawkbit and must be synchronized
* Category of Application
* OEM for group users
* Adding or removing member users to GroupUser
* Purchasing Applicaton
* No necessary Hawkbit's UI dependency for App operations
* Supported Application Uninstalling 

## Deployment Steps

- Clone the project:

> git clone https://github.com/eclipse/kuksa.cloud.git  


- Go to the Kuksa App Store repo:

> cd kuksa.cloud/kuksa.appstore
 
- Update H2 DBMS properties in application.properties file:

> spring.datasource.url=jdbc:h2:file:C:/data/sample;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE  
> spring.datasource.driverClassName=org.h2.Driver  
> spring.datasource.username=sa  
> spring.datasource.password=  

 - Please note that the H2 database is stored in 'C:/data/sample' file which is defined in spring.datasource.url property in application.properties. This path and file name can be changed according to hosting file system before starting Kuksa App Store in previous step.


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
- The app store has user authentication enabled in cloud profile. Default admin user credentials for Kuksa Appstore:

> appstore.username= admin   
> appstore.password= admin

- If you want to add default users and apps to your DB. You can use data.txt that is resource file (it is optional).

- The property hawkbit.host in application.properties is set to localhost in the repository. It should be changed to the IP address of Hawkbit Update Server used.

- Default debug mode in application.properties is set to false. This can be enabled/disabled by application.properties file.

- It can be accessed to the Swagger UI of Kuksa App Store API by following link;

> http://{appstore-ip}:8082/swagger-ui.html#/

- Each distribution has DUMMY_SOFTWARE_MODULE that can not be installed software module. We add this DUMMY_SOFTWARE_MODULE to each distributions because of Hawkbit wants to assign at least software module to a distribution. When we want to uninstall all device's applications, we need to delete all software modules that are in the distribution exclude DUMMY_SOFTWARE_MODULE.

 
### Default Data for Hawkbit and Kuksa App Store 
- Kuksa App Store repository introduces a default data for Hawkbit and App Store to test the software which adds a set of device targets, software distribution sets, software modules into Hawkbit instance as well as a set of apps and users (regular, group and OEM) into App Store. 

- It includes relationship between regular users and group/OEM users. The apps in App Store are compatible with software distribution sets in Hawkbit.

- The device targets in Hawkbit include proper user information in the description field and their names and controllerids include OEM names. Kuksa App Store recognizes the OEM of any device target from this naming notation as a work around solution.

- src\main\resources\load_test_data.sh curl shell script file is added into  to load test data to Hawkbit and Appstore. It includes a sample load data for one app, one user and one device and the file can be extended with new curl commands according to test scenario. The instruction set to exucute the script is given below:

> chmod +x load_test_data.sh  
> ./load_test_data.sh {hawkbit_ip:port} {appstore_ip:port}
If You run load_test_data.sh ; It will do following steps to Hawkbit and AppStoreDB.

#### The following statements describe the content of load_test_data.sh

 - Device Targets created in Hawkbit instance:
  1. OPEL_device1 is connected to OEM of OPEL.It's ownwers are user1,user2. 
  2. VW_device2 is connected to OEM of VW.It's ownwer is user2.
  3. VW_device3 is connected to OEM of VW.It's ownwer is user2.
  4. BMW_device4 is connected to OEM of BMW.It's ownwer is user3.

- Software Modules created in Hawkbit instance:

| Software Modules Set |
| :-------------:|  
|  App1 |  
|  App2 |  
|  App3 |  
|  App4 |
|  App5 |
|  App6 |
|  App7 |
|  App8 |
|  App9 |



- A Category created in Kuksa App Store:

| ID          | NAME          |
| :----------:|:-------------:|
| 1           | Maintenance   |

- Apps created in Kuksa App Store:

| ID        | APP NAME          |  
| :----------:|:-------------:|  
| 1     | App1 |  
| 2     | App2 |  
| 3     | App3 |  
| 4     | App4 |
| 5     | App5 |
| 6     | App6 |
| 7     | App7 |
| 8     | App8 |
| 9     | App9 |

- OEMs created in Kuksa App Store:

| ID        | NAME          |
| :----------:|:-------------:|
| 1     | OEM1 |
| 2     | OEM2 |
| 3     | OEM3 |


- Regular (normal) and group users created in Kuksa App Store


| ID | USER_NAME | PASSWORD | USERTYPE    | OEM_ID |
| :----------:|:----------:|:----------:|:----------:|:----------:|
| 1  | admin     | admin    | SystemAdmin | null    |
| 2  | user1     | user1    | Normal      | null    |
| 3  | user2     | user2    | Normal      | null    |
| 4  | user3     | user3    | Normal      | null    |
| 5  | org1      | org1     | GroupAdmin  | null    |
| 6  | org2      | org2     | GroupAdmin  | null    |
| 7  | org3      | org3     | GroupAdmin  | null    |
| 8  | org4      | org4     | GroupAdmin  | 1(OEM1)   |
| 9  | org5      | org5     | GroupAdmin  | 2(OEM2) |
| 10 | org6      | org6     | GroupAdmin  | 3(OEM3)  |


- Relationship between regular users and group users in Kuksa App Store:

| USER        | MEMBER          |
| :----------:|:-------------:|
| org1 | user2  |
| org2 | user3  |
| org2 | org1   |
| org3 | org2   |


- Purchased apps by users in Kuksa App Store:

| USERID | USER_NAME | APPID | NAME   |
| :----------:|:----------:|:----------:|:----------:|
| 2      | user1     | 1     | app1 |
| 3      | user2     | 1     | app1 |
| 3      | user2     | 2     | app2 |
| 3      | user2     | 3     | app3 |
| 4      | user3     | 3     | app3 |
| 5      | org1      | 1     | app1 |
| 5      | org1      | 4     | app4 |
| 6      | org2      | 2     | app2 |
| 6      | org2      | 5     | app5 |
| 7      | org3      | 6     | app6 |
| 8      | org4      | 7     | app7 |
| 9      | org5      | 8     | app8 |
| 10     | org6      | 9     | app9 |

 

 