#!/bin/bash 
#You need to give 2 arguments for following variables when you run this sh file. 
HAWKBIT_IP_PORT=$1
APPSTORE_IP_PORT=$2


#CREATE_TARGET

echo "\nstart CREATE_TARGET \n"

curl "http://$HAWKBIT_IP_PORT/rest/v1/targets" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4=' -d '[ {
  "securityToken" : "2345678DGGDGFTDzztgf",
  "address" : "https://192.168.0.1",
  "controllerId" : "OEM2_device1",
  "name" : "OEM2_device1",
  "description" : "user1,user2"
} ]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/targets" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4=' -d '[ {
  "securityToken" : "2345678DGGDGFTDzztgf",
  "address" : "https://192.168.0.1",
  "controllerId" : "OEM1_device2",
  "name" : "OEM1_device2",
  "description" : "user2"
} ]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/targets" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4=' -d '[ {
  "securityToken" : "2345678DGGDGFTDzztgf",
  "address" : "https://192.168.0.1",
  "controllerId" : "OEM1_device3",
  "name" : "OEM1_device3",
  "description" : "user2"
} ]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/targets" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4=' -d '[ {
  "securityToken" : "2345678DGGDGFTDzztgf",
  "address" : "https://192.168.0.1",
  "controllerId" : "OEM3_device4",
  "name" : "OEM3_device4",
  "description" : "user3"
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

curl "http://$HAWKBIT_IP_PORT/rest/v1/softwaremodules" -i -X POST -H 'Content-Type: application/hal+json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "vendor" : "NETAS",
  "name" : "app2",
  "description" : "app for NETAS",
  "type" : "os",
  "version" : "1.0.0"
} ]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/softwaremodules" -i -X POST -H 'Content-Type: application/hal+json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "vendor" : "NETAS",
  "name" : "app3",
  "description" : "app for NETAS",
  "type" : "os",
  "version" : "1.0.0"
} ]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/softwaremodules" -i -X POST -H 'Content-Type: application/hal+json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "vendor" : "NETAS",
  "name" : "app4",
  "description" : "app for NETAS",
  "type" : "os",
  "version" : "1.0.0"
} ]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/softwaremodules" -i -X POST -H 'Content-Type: application/hal+json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "vendor" : "NETAS",
  "name" : "app5",
  "description" : "app for NETAS",
  "type" : "os",
  "version" : "1.0.0"
} ]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/softwaremodules" -i -X POST -H 'Content-Type: application/hal+json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "vendor" : "NETAS",
  "name" : "app6",
  "description" : "app for NETAS",
  "type" : "os",
  "version" : "1.0.0"
} ]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/softwaremodules" -i -X POST -H 'Content-Type: application/hal+json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "vendor" : "NETAS",
  "name" : "app7",
  "description" : "app for NETAS",
  "type" : "os",
  "version" : "1.0.0"
} ]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/softwaremodules" -i -X POST -H 'Content-Type: application/hal+json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "vendor" : "NETAS",
  "name" : "app8",
  "description" : "app for NETAS",
  "type" : "os",
  "version" : "1.0.0"
} ]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/softwaremodules" -i -X POST -H 'Content-Type: application/hal+json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "vendor" : "NETAS",
  "name" : "app9",
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
  "description" : "This is a description of app1.",
  "type" : "os_app",
  "version" : "1.0.0",
  "modules" : [ {
    "id" : 1
  } ]
}]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/distributionsets/" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "requiredMigrationStep" : false,
  "name" : "app2",
  "description" : "This is a description of app2.",
  "type" : "os_app",
  "version" : "1.0.0",
  "modules" : [ {
    "id" : 2
  } ]
}]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/distributionsets/" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "requiredMigrationStep" : false,
  "name" : "app3",
  "description" : "This is a description of app3.",
  "type" : "os_app",
  "version" : "1.0.0",
  "modules" : [ {
    "id" : 3
  } ]
}]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/distributionsets/" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "requiredMigrationStep" : false,
  "name" : "app4",
  "description" : "This is a description of app4.",
  "type" : "os_app",
  "version" : "1.0.0",
  "modules" : [ {
    "id" : 4
  } ]
}]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/distributionsets/" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "requiredMigrationStep" : false,
  "name" : "app5",
  "description" : "This is a description of app5.",
  "type" : "os_app",
  "version" : "1.0.0",
  "modules" : [ {
    "id" : 5
  } ]
}]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/distributionsets/" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "requiredMigrationStep" : false,
  "name" : "app6",
  "description" : "This is a description of app6.",
  "type" : "os_app",
  "version" : "1.0.0",
  "modules" : [ {
    "id" : 6
  } ]
}]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/distributionsets/" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "requiredMigrationStep" : false,
  "name" : "app7",
  "description" : "This is a description of app7.",
  "type" : "os_app",
  "version" : "1.0.0",
  "modules" : [ {
    "id" : 7
  } ]
}]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/distributionsets/" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "requiredMigrationStep" : false,
  "name" : "app8",
  "description" : "This is a description of app8.",
  "type" : "os_app",
  "version" : "1.0.0",
  "modules" : [ {
    "id" : 8
  } ]
}]'

curl "http://$HAWKBIT_IP_PORT/rest/v1/distributionsets/" -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Authorization: Basic YWRtaW46YWRtaW4='  -d '[ {
  "requiredMigrationStep" : false,
  "name" : "app9",
  "description" : "This is a description of app9.",
  "type" : "os_app",
  "version" : "1.0.0",
  "modules" : [ {
    "id" : 9
  } ]
}]'
echo "\nend Create Distribution Sets\n"
#-------------
#Create an app category to Appstore
echo "\nstart Create an app category to Appstore\n"
curl "http://$APPSTORE_IP_PORT/api/1.0/appcategory" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
"name": "Maintenance"
}'

echo "\nend Create an app to Appstore\n"

#Create an app to Appstore
echo "\nstart Create an app to Appstore\n"
curl "http://$APPSTORE_IP_PORT/api/1.0/app" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
"name": "app1",
"hawkbitname": "app1",
"description": "description for app1",
"version": "1.0.0",
"owner": "NETAS",
"downloadcount": 0,
"publishdate": "2018-10-01T06:50:03.000+0000",
"appcategory": {
  "id": 1
  }
}'

curl "http://$APPSTORE_IP_PORT/api/1.0/app" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
"name": "app2",
"hawkbitname": "app2",
"description": "description for app2",
"version": "1.0.0",
"owner": "NETAS",
"downloadcount": 0,
"publishdate": "2018-10-01T06:50:03.000+0000",
"appcategory": {
  "id": 1
  }
}'

curl "http://$APPSTORE_IP_PORT/api/1.0/app" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
"name": "app3",
"hawkbitname": "app3",
"description": "description for app3",
"version": "1.0.0",
"owner": "NETAS",
"downloadcount": 0,
"publishdate": "2018-10-01T06:50:03.000+0000",
"appcategory": {
  "id": 1
  }
}'

curl "http://$APPSTORE_IP_PORT/api/1.0/app" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
"name": "app4",
"hawkbitname": "app4",
"description": "description for app4",
"version": "1.0.0",
"owner": "NETAS",
"downloadcount": 0,
"publishdate": "2018-10-01T06:50:03.000+0000",
"appcategory": {
  "id": 1
  }
}'

curl "http://$APPSTORE_IP_PORT/api/1.0/app" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
"name": "app5",
"hawkbitname": "app5",
"description": "description for app5",
"version": "1.0.0",
"owner": "NETAS",
"downloadcount": 0,
"publishdate": "2018-10-01T06:50:03.000+0000",
"appcategory": {
  "id": 1
  }
}'

curl "http://$APPSTORE_IP_PORT/api/1.0/app" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
"name": "app6",
"hawkbitname": "app6",
"description": "description for app6",
"version": "1.0.0",
"owner": "NETAS",
"downloadcount": 0,
"publishdate": "2018-10-01T06:50:03.000+0000",
"appcategory": {
  "id": 1
  }
}'

curl "http://$APPSTORE_IP_PORT/api/1.0/app" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
"name": "app7",
"hawkbitname": "app7",
"description": "description for app7",
"version": "1.0.0",
"owner": "NETAS",
"downloadcount": 0,
"publishdate": "2018-10-01T06:50:03.000+0000",
"appcategory": {
  "id": 1
  }
}'

curl "http://$APPSTORE_IP_PORT/api/1.0/app" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
"name": "app8",
"hawkbitname": "app8",
"description": "description for app8",
"version": "1.0.0",
"owner": "NETAS",
"downloadcount": 0,
"publishdate": "2018-10-01T06:50:03.000+0000",
"appcategory": {
  "id": 1
  }
}'

curl "http://$APPSTORE_IP_PORT/api/1.0/app" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{
"name": "app9",
"hawkbitname": "app9",
"description": "description for app9",
"version": "1.0.0",
"owner": "NETAS",
"downloadcount": 0,
"publishdate": "2018-10-01T06:50:03.000+0000",
"appcategory": {
  "id": 1
  }
}'

echo "\nend Create an app to Appstore\n"
#-------------
#Create an OEM to Appstore
echo "\nstart Create an OEM to Appstore\n"
curl "http://$APPSTORE_IP_PORT/api/1.0/oem" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
   "name": "OEM1"
 }'

curl "http://$APPSTORE_IP_PORT/api/1.0/oem" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
   "name": "OEM2"
 }'

curl "http://$APPSTORE_IP_PORT/api/1.0/oem" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{ 
   "name": "OEM3"
 }'  
echo "\nend Create an OEM to Appstore\n"

#-------------
#Create User to Appstore
echo "\nstart Create User to Appstore\n"

curl "http://$APPSTORE_IP_PORT/api/1.0/user" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{
   "username": "user1",
   "password": "user1",
   "userType": "Normal",
   "oem": null,
   "members": []
 }'
 
curl "http://$APPSTORE_IP_PORT/api/1.0/user" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{
   "username": "user2",
   "password": "user2",
   "userType": "Normal",
   "oem": null,
   "members": []
 }'
 
curl "http://$APPSTORE_IP_PORT/api/1.0/user" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{
   "username": "user3",
   "password": "user3",
   "userType": "Normal",
   "oem": null,
   "members": []
 }' 
curl "http://$APPSTORE_IP_PORT/api/1.0/user" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{
   "username": "org1",
   "password": "org1",
   "userType": "GroupAdmin",
   "oem": null,
   "members": [
     {
       "id": 3
     }
   ]
 }'
curl "http://$APPSTORE_IP_PORT/api/1.0/user" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{
   "username": "org2",
   "password": "org2",
   "userType": "GroupAdmin",
   "oem": null,
   "members": [
     {
       "id": 4
     },{
       "id": 5
     }
   ]
 }' 
curl "http://$APPSTORE_IP_PORT/api/1.0/user" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{
   "username": "org3",
   "password": "org3",
   "userType": "GroupAdmin",
   "oem": null,
   "members": [
     {
       "id": 6
     }
   ]
 }'
curl "http://$APPSTORE_IP_PORT/api/1.0/user" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{
   "username": "org4",
   "password": "org4",
   "userType": "GroupAdmin",
   "oem": {
    "id": 1
	},
   "members": []
 }'
curl "http://$APPSTORE_IP_PORT/api/1.0/user" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{
   "username": "org5",
   "password": "org5",
   "userType": "GroupAdmin",
   "oem": {
    "id": 2
	},
   "members": []
 }'
curl "http://$APPSTORE_IP_PORT/api/1.0/user" -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4=' -d '{
   "username": "org6",
   "password": "org6",
   "userType": "GroupAdmin",
   "oem": {
    "id": 3
	},
   "members": []
 }'   
echo "\nend Create User to Appstore\n"
#-------------
#Purchase App for an user on Appstore
echo "\nstart Purchase App for an user on Appstore\n"

curl "http://$APPSTORE_IP_PORT/api/1.0/app/1/purchase/2" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/1/purchase/3" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/1/purchase/5" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/2/purchase/3" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/2/purchase/6" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/3/purchase/3" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/3/purchase/4" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/4/purchase/5" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/5/purchase/6" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/6/purchase/7" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/7/purchase/8" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/8/purchase/9" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='

curl "http://$APPSTORE_IP_PORT/api/1.0/app/9/purchase/10" -X GET --header 'Accept: application/json' --header 'Authorization: Basic YWRtaW46YWRtaW4='



echo "\nend Purchase App for an user on Appstore\n"
