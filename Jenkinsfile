/*********************************************************************
 * Copyright (c) 2019 Bosch Software Innovations GmbH [and others]
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

pipeline {
    agent {
    kubernetes {
      label 'kuksa-release-pod'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: gradle
    image: gradle:jdk8
    command:
    - cat
    tty: true
"""
    }
  }
  stages {
    stage('Run Gradle') {
      steps {
        container('gradle') {
          sh 'mkdir release && mkdir release/utils && mkdir release/examples && mkdir release/utils/hono-influxdb-connector && mkdir release/examples/malfunction-indicator-light'
          dir('utils/hono-influxdb-connector') {
              sh 'gradle build'
              sh 'curl -o ../../release/utils/hono-influxdb-connector/hono-influxdb-connector.jar -F file=@build/libs/hono-influxdb-connector-0.2.1.jar http://build.eclipse.org:31338/sign'
              sh 'cp pushImage.sh ../../release/utils/hono-influxdb-connector/pushImage.sh'
          }
          dir('examples/malfunction-indicator-light') {
            dir('influxdb-rest') {
              sh 'gradle build'
              sh 'curl -o build/libs/influxdb-rest.jar -F file=@build/libs/influxdb-rest-0.1.0.jar http://build.eclipse.org:31338/sign'
              sh 'cp build/libs/influxdb-rest.jar ../../../release/examples/malfunction-indicator-light/influxdb-rest.jar'
            }
            dir('mail-notification') {
              sh 'gradle build'
              sh 'curl -o build/libs/mail-notification.jar -F file=@build/libs/mail-notification-0.2.0.jar http://build.eclipse.org:31338/sign'
              sh 'cp build/libs/mail-notification.jar ../../../release/examples/malfunction-indicator-light/mail-notification.jar'
            }
            dir('mil-service') {
              sh 'gradle build'
              sh 'cd build/libs && ls'
              sh 'curl -o build/libs/indicator-light-service.jar -F file=@build/libs/indicator-light-service-0.1.0.jar http://build.eclipse.org:31338/sign'
              sh 'cp build/libs/indicator-light-service.jar ../../../release/examples/malfunction-indicator-light/indicator-light-service.jar'
            }
          }
          sh 'cp -r deployment release/deployment'
          sh 'cp -r utils/interact-with-hono release/utils/deployment'
          sh 'tar -cvf kuksa-cloud-release.tar release'
          archiveArtifacts 'kuksa-cloud-release.tar'
        }
      }
    }
  }
    post {
        failure {
            sh 'echo failure'
        }
    }
}
