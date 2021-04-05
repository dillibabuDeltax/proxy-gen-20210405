# APIGEE Proxy generator tool

The proxy generator utility will help generating APIGEE API proxies based on pre-defined templates. The templates are created based on the recommendations from Google and best practices for the platform.

---

## QuickStart
```shell
mvn clean install
java -jar target/proxygen-0.0.1-SNAPSHOT.jar src/test/resources/partnerprofileapi-metadata.yaml src/test/resources/partnerprofileapi.yaml partnerprofileapiWorkDir

java -jar target/proxygen-0.0.1-SNAPSHOT.jar src/test/resources/apigee-mock-metadata.yaml src/test/resources/apigee-mock.yaml apigee-mockWorkDir

java -jar target/proxygen-0.0.1-SNAPSHOT.jar src/test/resources/callbackApi-metadata.yaml src/test/resources/callbackApi.yaml callbackApiWorkDir
java -jar target/proxygen-0.0.1-SNAPSHOT.jar src/test/resources/partner-profiles-api-0.5.2-metadata.yaml src/test/resources/partner-profiles-api-0.5.2.yaml partner-profiles-api-0.5.2PandushWorkDir
java -jar target/proxygen-0.0.1-SNAPSHOT.jar src/test/resources/nodetemplateapi-metadata.yaml src/test/resources/nodetemplateapi.yaml nodetemplateapi-WorkDir


```

## How does the tool work?

The tool works by taking in two inputs.

-   API Proxy Metadata in the form of YAML
-   Open API Spec for the proxy

Tool can be executed on local machine using the below command.

```shell
java -jar proxygen.jar <<path to the Proxy Metadata file> <<Path to Open API Spec>>
```

### Info expected from API Proxy metadata

At a high level, the below information is needed on the metadata file. Refer a sample under the `test/resources` directory for a real world example.

---

## Patterns supported by the tool

The goal is to support various API patterns as the API program matures. Currently, the below patterns are supported.

-   API Proxy with API key based protection.
-   API proxy with OAuth 2.0 (Client Credential) opaque token (generated by APIGEE)
-   _Future_ --> API proxy with OAuth 2.0 (Authorization Grant) jwt token (generated by External OAuth provider. Example: Keycloak)

---

## How to build

The project is a maven project and use the below command to build an executable jar.

```shell
mvn clean install
```

Building the project will result two different `jar` files placed under `target` directory.

## How to make changes to anything in /templates folder

Templates are found in _src/resources/templates_ directory. This directory is compreessed as _templates.zip_.

If you are on macOS, use the below command to compress.

```shell
cd <<workspace>>/proxygen/src/main/resources
zip -r -X templates.zip *
```

## Customizations

The proxy gen tool will checkout the dev branch from git repository if it exists and overwrite and checkin the new proxy. Also it will try to create a Jenkins multibranch pipeline.

This behavior however can be changed by modifying _src/resources/application.properties_

GIT  integrations can be turned off by changing the following values to false

```shell
config.gitlab.enabled=false
