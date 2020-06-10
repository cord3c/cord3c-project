# cord3c-project

**Here you find W3C DID/VC, REST, HTTP and monitoring support for R3 Corda!**

[Corda](https://github.com/corda/corda/) is an open source blockchain/DLT project
with a strong focus on privacy and scalability to address the needs of enterprises.
Target market of Corda are business-to-business transactions in finance and
insurance markets. However, its architecture also make it applicable
to a wide range of other markets. This project aims at
unlocking some of that potential with a number of extensions. Primary focus
lies bringing various RFCs and W3C specifications to Corda with the goal to:

- allow involvement of end-users and IoT devices in business processes.
- follow the principles of self-sovereign identity (SSI) to
  let users have ownership of their data and gain further
  security and privacy along the way.
- allow interoperability across different kinds of devices beyond the Java ecosystem.
- improve speed and reduce storage requirements to process transactions to
  cover the most demanding use cases.
- avoid the lock-in to any single vendor by following further open standards.

This is achieved due to the flexible nature of the Corda and its flow framework.
Our steps are:

- *Native support for W3C [Verifiable Credentials](https://www.w3.org/TR/vc-data-model/) (VCs)
  and [Decentralized Identifiers](https://www.w3.org/TR/did-core/)* (DIDs) to make identities and claims about those identities
  first-class citizen in Corda (aka native SSI for Corda). Possibilities are endless by modelling the world
  from people to objects and relationships among them with DIDs and VCs. VCs can be created,
  signed, exchanged, verified, and saved in a vault not unlike Corda transactions.
- Focus on W3C standards to (over time)  support a wide range of SSI implementations (soverin,  uport, etc.).
- Letting every Corda node gain a DID, make it discoverable using
  [universal resolvers](https://medium.com/decentralized-identity/a-universal-resolver-for-self-sovereign-identifiers-48e6b4a5cc3c),
  and let it issue VCs. An adapter HTTP service thereby bridges between network maps
  and universal resolvers.
- Support for [did:web method](https://w3c-ccg.github.io/did-method-web/) to bootstrap DIDs with
  HTTPS to ease development and avoid relying on other more complex SSI implementation from start.
- Allow *end-users and IoT devices carry a DID to participate in transactions*.
- Support for [hashlink](https://tools.ietf.org/html/draft-sporny-hashlink-04) to implement
  [content integrity protection](https://www.w3.org/TR/vc-data-model/#content-integrity-protection)
  for W3C VCs.
- Adoptions of [JSON Web Signatures](https://tools.ietf.org/html/rfc7515) to *ease signing and verifying of
  transactions by any device*, from a Corda server to browsers, mobiles and IoT devices.
- A *REST API* following the open [JSON:API](https://jsonapi.org/) specification complementing the proprietary
  Corda RPC protocol to ease working with Corda across a wider range of clients.
- A monitoring endpoint offering health checking and [Prometheus-based metrics](https://prometheus.io/).

In an experimental state we also start using:

- *JSON as data format* for states and transactions. Renders Corda transactions human-readable!
  Non-Java application can start creating, retrieving and verifying data with little effort.
  Existing tools allow to customize the mapping of data to Java objects while helping with aspects
  like compatibility, versioning and upgrades.
- Blurring the boundaries between Corda transactions/states and W3C VCs by modelling the former with the later.
  The benefits are two-fold: let W3C VCs gain Corda functionality like notarization and
  and UXTO and let W3C VC implementations consume transactions vice versa.

This project is under early but active development. Feedback very welcomed.

It is important to note that all extensions are complementary to the existing Corda features
and can be used together or individually with any Corda server.

## Examples

TODO

## Requirements

cord3c still requires Java 8 due to limitations of Corda. JDK 8 is automatically downloaded and used by
Gradle using [jdk-bootstrap](https://github.com/rmee/gradle-plugins/tree/master/jdk-bootstrap).



## IDE

Dewvelopment takes place with IntelliJ IDEA, but should also work with any other IDE.
In IDEA, make sure to have the Lombok plugin installed (is the default) and
annotation processing enabled (also default).


## Building from Source

cord3c make use of Gradle for its build. To build the project run:

```
./gradlew build
```







## Running the example node

To locally start a Corda node with the cord3c components and example application installed use:

	gradlew :cord3c-example-node:run

or

    docker run --name example --rm -i -t -p 8090:8090 cord3c/example-node

The API endpoint will be available at:

 	http://localhost:8090/api/

Some further URLs to play around:

    http://127.0.0.1:8080/api/movie
    FIXME


## Running the network map adapter

Start a Cordite network map (or configure any kind of existing network map):

```
docker run -p 8080:8080 --rm  -e NMS_TLS=false --name networkmap -t -i  -e NMS_PARAM_UPDATE_DELAY=0S -e NMS_STORAGE_TYPE=file  cordite/network-map:v0.4.5
```

And then run the adapter with:

```
  ./gradlew :cord3c-ssi-networkmap-adapter:run
```

or

TODO

## Integration with existing Corda setup

TODO
properties
cordapps



## Licensing

cord3c is licensed under the Apache License, Version 2.0.
You can grab a copy of the license at http://www.apache.org/licenses/LICENSE-2.0.
