# Kubernetes Templates

This directory contains exemplary templates to deploy this use-case on Kubernetes.
All possible settings are defined by environment variables using Kubernetes config maps and secrets.
Therefore, it is not necessary to edit the `application.properties` or `application.yaml` before building a Docker image.
The `*.yaml` templates in this directory contain a `<PLACEHOLDER>` everywhere that needs to be adopted to the respective setup.
 