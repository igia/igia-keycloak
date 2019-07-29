Deploying using the igia-orchestrator will automatically deploy a docker image of Keycloak with the custom providers installed and the igia realm imported. This document describes how to configure manually or customize the Keycloak settings.

# Install

To install on an existing Keycloak server, copy the igia-keycloak jar file to your Keycloak installation into the /providers directory. The provider should automatically be deployed and registered. See [Keycloak documentation](https://github.com/keycloak/keycloak-documentation/blob/master/server_development/topics/providers.adoc) for details.

You can check the admin console Server Info → Providers tab to ensure the following providers are registered.
1. authenticator: smart-launch-context-authenticator
2. login-protocol: smart-openid-connect
3. protocol-mapper: smart-oidc-client-session-note-mapper

In addition to the providers, there is a smart-launch.ftl freemarker template that acts as a placeholder for patient context, for testing purposes when a patient context launch app is not available. This can be copied into the keycloak /themes/base/login.

# Configuration
In order to configure Keycloak for standalone launch using the platform smart-launch-app or another launch context provider, you will need to setup scopes, authentication flow, and clients to your realm as described below. The following applies to Keycloak version 4.5.0.Final.

## Client scopes

Client scopes for protocol openid-connect need to be added for SMART apps. Display on consent screen is optional.
1. openid (if ID provider required)
2. offline_access
3. launch/patient
4. any desired SMART user or patient scopes (ie patient/\*.read)

For the launch/patient scope, setup a scope Mapper with the properties below:
* Name = SMART launch/patient client session note
* Protocol = openid-connect
* Mapper Type = Client Session Note
* Client Session Note = launch/patient
* Token Claim Name = patient
* Claim JSON Type = String
* Add to access token = on

## Authentication flows
1. Create a new Authentication flow named "SMART browser" as a copy of existing browser flow.
* Execution: add an execution to the flow of provider type Smart Launch to the flow and make it Optional. This must be the final provider in the flow, otherwise you risk having the launch selector appear to the user before they have successfully authenticated. This can cause leak of PHI if a selector widget displays PHI.
* Execution configuration: configure the new execution with the properties below.

|Configuration property|description|Example igia-smart-launch-app setting|
|--- |--- |--- |
|External SMART Launch URL|External URL to redirect for user launch context selection. The launch URL must include a query parameter with value placeholder "{TOKEN}" for Keycloak to insert the generated token into the external request.|http://localhost:9000/#/patient?token={token}|
|External SMART Launch Secret Key|HmacSHA256 secret key for smart launch external application.|Must match application.secret-key configuration property from igia-smart-launch-app backend service.|
|External SMART Launch Client Id|Client Id for smart launch external application. You will need to create a new client application entry for the external launch app and enter the client id into this config property. The client application should be assigned default scopes for any required access to patient data, such as user/Patient.read for a patient search application. During launch, an access token will be created for this client application using default scope only.|Client id setup in Keycloak for external launch app|
|External SMART Launch Supported Params|Space separated list of Smart launch context parameters supported by external application. The external app will only be executed if there is a match between the supported launch context params and requested or default scopes of the SMART app.|patient|

## Clients

For each SMART client application, setup as follows.
* Client : create new Client entry for your individual SMART app with protocol openid-connect. The openid configuration of this client will be client specific.
* Authentication Flow Overrides: change the Browser Flow authentication binding from browser to the new "SMART browser" flow.
* Client Scopes: add desired Client Scopes to optional available scopes. This will only be included if the app requests them.
* Scope: Set full scope allowed to false.
