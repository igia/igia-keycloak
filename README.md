# igia-keycloak

igia-keycloak is igia's OAuth2/OIDC server. It is based on Redhat Keycloak and includes additional support for SMART-on-FHIR.

This library contains Keycloak providers that can be installed on an existing instance, as well as a Docker file which can be used to build a Keycloak image that includes the additional providers.

## Usage

See usage documentation on Keycloak configuration for SMART-on-FHIR.

## Development

To fully dockerize Keycloak including the additional providers and pre-installed realm configuration, first build a docker image of your app by running:

    ./mvnw package dockerfile:build

Then run:

    docker-compose -f src/main/docker/app.yml up -d

After the container is running and the Keycloak service is available, you can run the following command to update the Keycloak database to finish all required setup for the SMART-on-FHIR test environment. Change 'docker_keycloak_1' to reflect your container name.

    docker exec docker_keycloak_1 bash -c "java -cp /opt/jboss/keycloak/modules/system/layers/base/com/h2database/h2/main/h2*.jar org.h2.tools.RunScript -url 'jdbc:h2:/opt/jboss/keycloak/standalone/data/keycloak;AUTO_SERVER=TRUE' -user sa -password sa -script /opt/jboss/keycloak/realm-config/update.sql && ./keycloak/bin/kcadm.sh config credentials --server http://keycloak:9080/auth --realm master --user admin --password admin && ./keycloak/bin/kcadm.sh update clients/ed424acd-36ce-433e-bf59-f1f3143faf6f -r igia -s enabled=true --merge"

You should be able to navigate to the Keycloak admin console at http://localhost:9080/auth/admin and login using default credentials (username: admin, password: admin). The igia realm should be imported.

The container is configured to use an embedded H2 database that does not persist data on restart. See Keycloak documentation for details on how to configure and external RDBMS. If you use a database other than the embedded H2, you will need to manually configure the Keycloak patient_data_manager client instead of using the script above. You will need to modify the client entry to set Authentication Flow Overrides -> Browser Flow to SMART browser and save.

### Prerequisites

If you are running docker in the development environment, you should add a mapping from keycloak to localhost in your /etc/hosts file.

    127.0.0.1       keycloak

## Building for production

To package the igia-keycloak application as a jar for installation on an existing Keycloak instance, run:

    ./mvnw clean package

The resulting jar file can be copied to your existing Keycloak installation into the /providers directory.

## Testing

To launch your application's tests, run:

    ./mvnw clean test

## Contributing

Please read [CONTRIBUTING](https://igia.github.io/docs/contributing/) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/igia/igia-keycloak/tags).

## Acknowledgments
* [Redhat Keycloak](http://www.keycloak.org/).

## License and Copyright

MPL 2.0 w/ HD  
See [LICENSE](LICENSE) file.  
See [HEALTHCARE DISCLAIMER](HD.md) file.  
© [Persistent Systems, Inc.](https://www.persistent.com)