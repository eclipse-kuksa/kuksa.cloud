#!/bin/bash 
#You need to give 2 arguments for following variables when you run this sh file. 
HAWKBIT_IP_PORT=$1
APPSTORE_IP_PORT=$2


#CREATE_TARGET

echo "\nstart CREATE_TARGET\n"

curl "http://$HAWKBIT_IP_PORT/rest/v1/targets" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4=' -d '[ {
  "securityToken" : "2345678DGGDGFTDzztgf",
  "address" : "https://192.168.0.1",
  "controllerId" : "device01",
  "name" : "device01",
  "description" : "admin"
} ]'

echo "\nend CREATE_TARGET\n"
#-------------

#Create software modules
echo "\nstart Create software modules\n"

curl "http://$HAWKBIT_IP_PORT/rest/v1/softwaremodules" -i -X POST -H 'Content-Type: application/hal+json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "vendor" : "NETAS",
  "name" : "app1",
  "description" : "app for NETAS",
  "type" : "os",
  "version" : "1.0.0"
} ]'

echo "\nend Create software modules\n"
#--------------

#Create Distribution Sets
echo "\nstart Create Distribution Sets\n"
curl "http://$HAWKBIT_IP_PORT/rest/v1/distributionsets/" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "requiredMigrationStep" : false,
  "name" : "app1",
  "description" : "proin erat proin bibendum donec elementum non interdum torquent ultrices",
  "type" : "os_app",
  "version" : "1.0.0",
  "modules" : [ {
    "id" : 1
  } ]
}]'

echo "\nend Create Distribution Sets\n"
#-------------

#Create an app to Appstore
echo "\nstart Create an app to Appstore\n"
curl "http://$APPSTORE_IP_PORT/api/1.0/app" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
"name": "app1",
"hawkbitname": "app1",
"description": "description for app1",
"version": "1.0.0",
"owner": "NETAS",
"downloadcount": 0,
"publishdate": "2018-10-01T06:50:03.000+0000"}'

echo "\nend Create an app to Appstore\n"
