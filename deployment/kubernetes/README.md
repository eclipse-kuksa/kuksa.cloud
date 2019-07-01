# Kubernetes-specific functionality

## Deploy the Gateway service

* Script:
  * `deploy_ambassador.sh`
* Purpose:
  * Install and configure the [Ambassador gateway service](https://getambassador.io/) to perform TLS termination
* Options:
  * `CLUSTER_NAME`: Name of the AKS cluster - it is used to create domain names that 
    contain the name of the cluster.
  * `DNS_ZONE_NAME`: The name of the DNS zone to use for the dns01 challenge and for 
    hosting the services. The name of the DNS zone is a domain name that is appended 
    to the domain names used to configure Ambassador.
  * `GATEWAY_IP_ADDRESS`: The static IP address to be used by the Ambassador gateway.
* Stages:
  * Customize Ambassador service configuration
  * Install/update Ambassador (with a default service configuration)
  * Install custom Ambassador service configuration
* Troubleshooting:
  * You can delete the ambassador release using `helm del --purge ambassador`.
  * See also troubleshooting TLS termination below.

## Troubleshooting TLS termination

We use a set-up with the following components
* unencrypted services
* a gateway that offers TLS-encrypted ports for the unencrypted services (TLS termination)
* clients that accesses the TLS ports offered by the gateway 

If a client is not able to connect to the service via the gateway,
it makes sense to analyze the issues from service to gateway to
client to be able to be able to isolate the cause of the connection
error.

1. Make sure the unencrypted service is serving requests.
   1. If the service is available from the internet, try to connect directly, either 
      using `curl` or telnet` or with a real client which is typically easy to
      configure to non-TLS connections.
   1. If the service is not available, use 
      [port forwarding with kubectl](https://kubernetes.io/docs/tasks/access-application-cluster/port-forward-access-application-cluster/#forward-a-local-port-to-a-port-on-the-pod).
1. Make sure the gateway serves TLS requests.
   When the unencrypted service is actually offered, the gateway might be
   misconfigured.
   1. The ambassador pods (in the default namespace) output useful information when the 
      service configuration is updated.
   1. There is an [ambassador diagnostics service](https://www.getambassador.io/user-guide/getting-started#6-the-diagnostics-service-in-kubernetes)
      that provides further information when there is a misconfiguration.
   1. When you customized the ambassador service but your services are not accessible, 
      one (out of many) sources of error might be that the original ambassador service 
      configuration changed in an unexpected way so that the customized ambassador service 
      does not fit anymore. You can figure that out by not deploying the customized ambassador 
      service - a new installation will then deploy the default ambassador service. You 
      can the export it using `kubectl get service ambassador -o yaml` and compare 
      it to the customized ambassador service.
   1. [testssl.sh](https://github.com/drwetter/testssl.sh#installation)
      is a handy tool that outputs information on e.g. the TLS certificates offered
      and works for TCP connections, too. This may be helpful because sometimes
      gateway configuration is not effectively immediately. You can then testssl.sh
      to figure out whether the gateway configuration is effective. Testssl.sh runs 
      a number of tests and provides debug output. It does not enable you to enter 
      data to be transmitted to the service.
   1. You may use openssl to establish an interactive TLS session. E.g.
      `openssl s_client -connect <hostname>:<tlsPort> -servername <hostname> -CAfile <caCertInPemFormat>`.
      Note that `-servername` is required to make openssl send server name indication (SNI) 
      headers. The gateway requires SNI headers. The `-CAfile` argument needs to be 
      given e.g. when the Let'S encrypt staging environment is used which does not
      have a CA that is trusted by default.
   1. If you do not have a service that you can easily test interactively, you can
      also start a [tcp-echo](https://github.com/istio/istio/blob/release-1.1/samples/tcp-echo/tcp-echo.yaml)
      service. If you get that to echo your input via TLS, other services with a
      comparable gateway configuration might work as well.
1. Make sure the client is properly connecting to the gateway.
   When the unencrypted service and the gateway work as expected, the client must be 
   misconfigured.
   1. Have a look at the logs of the unencrypted service - should it log client requests 
      but does not do so? Then the client probably fails to connect.
   1. Some clients require a trust store with trusted certificates to be configured 
      whenever TLS is used and never ever use the system trust store.

## Decision log

Why did we choose the current solution? What alternatives did we consider?
 
* Design decisions - why was the current solution chosen?
  * *Hono certs solution* We could have used the [hono demo certificates](https://github.com/eclipse/hono/tree/master/demo-certs).
    * Pros
      * That's a minimal solution that works already.
      * The certificates would be used internally and externally and would thus have the 
        cluster-internal DNS names and the external domain name.
    * Cons
      * The certificates are self-signed i.e. clients need to configure a truststore. 
        *This did not work for the cli-proton-python-receive script from
        [cli-proton-python](https://cli-proton-python.readthedocs.io/en/latest/)
        that we use for AMQP testing. This is a show-stopper.*
      * The certificates/truststore are only valid for 1 year and are not 
        renewed/distributed to clients automatically.
      * The solution does not yet cover hawkBit and other services.
  * *Cert-manager solution* We could have used [cert-manager](https://docs.cert-manager.io/en/latest/) and 
    [Let's encrypt](https://letsencrypt.org/) with
    [dns-01 challenge](https://docs.cert-manager.io/en/latest/tasks/acme/configuring-dns01/index.html) and
    [AzureDNS](https://docs.cert-manager.io/en/latest/tasks/acme/configuring-dns01/azuredns.html) 
    (see also [Microsoft Docs](https://docs.microsoft.com/en-us/azure/dns/)). We did
    not use the alternative [http-01 challenge](https://docs.cert-manager.io/en/latest/tasks/acme/configuring-http01.html)
    because we have non-http endpoints that do not use Kubernetes ingresses that the
    http-01 challenge is based on.
    * Pros
      * The certificates are signed by a well-known CA - clients do not need to configure 
        a truststore.
      * cert-manager renews the certificates automatically.
    * Cons
      * There is currently no way to restart our services automatically so they use
        renewed certificates. (When services are restarted for other reasons, they
        will pick up the certificates, though. If renewal happens early enough before
        expiry and the service is restarted often enough, renewed certificates will
        be picked up implicitly before the old certificates expire.)
      * Requires more effort.
      * *Let's encrypt does not issue certificates for non-public DNS names like the
        ones inside Kubernetes by services contacting other services. We decided not
        to use public DNS names for inter-service communication because that would
        also route inter-service traffic via the external routes. While it might be
        possible to have the services present different certificates to internal and
        external clients that would be a very uncommon solution that would make it hard 
        to integrate further services. This is a show-stopper.*
  * *nginx* was not considered further because the docs on
    [TLS termination for TCP](https://docs.nginx.com/nginx/admin-guide/security-controls/terminating-ssl-tcp/)
    say that nginx Pro is required for this feature.
  * *Istio solution* We wanted to use [Istio](https://istio.io/)
    [Gateways](https://istio.io/docs/concepts/traffic-management/#gateways) with above
    cert-manager solution.
    * Pros
      * Allows for uniform TLS termination so TLS does not need to be configured 
        in each server implementation. TLS termination can be performed for external
        access only so internal and external DNS names are not mixed in the certificates.
      * Works for non-HTTP connections.
      * As of version 1.1, Istio applies updated certificates automatically
        using a built-in secret discovery service (SDS).
    * Cons
      * Requires more effort
      * Sebastian Lohmeier did not manage to configure it successfully but it might 
        be worth another try if time permits and further Istio features are desired.
    * See also
      * https://istio.io/docs/tasks/traffic-management/secure-ingress/#configure-a-tls-ingress-gateway-for-multiple-hosts
  * *Ambassador solution* We used [Ambassador](https://www.getambassador.io/) that provides a
    layer on top of [Envoy](https://www.envoyproxy.io/).
    * Pros
      * Allows for uniform TLS termination (like in Istio solution).
      * Works for non-HTTP connections (as of version 0.51.0).
      * Ambassador automatically applies updated certificates.
      * Could later also be [combined with Istio](https://www.getambassador.io/user-guide/with-istio).
    * Cons
      * Some enterprise features are only available in
        [Ambassador Pro](https://www.getambassador.io/pro)
      * Is only able to serve a single certificate per port. I.e. when multiple hostnames 
        are configured for a port number via SNI (server name indication) all requests 
        to these different host names are served a single certificate. We work around 
        this limitation by using a single certificate with subject alt names for all 
        desired host names.
