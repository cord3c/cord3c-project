# cord3c-project

**Here you find W3C DID/VC, REST, HTTP, and monitoring support for R3 Corda!**

Corda is an open source blockchain/DLT project  with a strong focus on
privacy and scalability while addressing the needs of enterprises. More information
are available in https://github.com/corda/corda/. A focus area of Corda are business-to-business
transactions in finance and insurance markets. However, its model and strong privacy guarantees
also make it applicable to a wide range of other markets. This project aims at
unlocking some of that potential with a number of extensions. Primary focus
lies bringing various RFCs and W3C specifications to Corda with the goal to:

- avoid the lock-in to any single vendor.
- involve end-users and IoT devices in business processes-
- give ownership of data back to users with self-sovereign identity (SSI)
  to improve security and privacy.
- gain interoperability across different kinds of devices.
- simplify transactions and state data formats to ease their use, exchange, maintenance and upgrade.
- target the non-Java developer community.
- improve speed and reduce storage requirements to process transactions.
- simplify some development and operational aspects.

This is achieved due to the flexible nature of the Corda flow framework and some further internal APIs.
Our steps are:

- *Native support for W3C [Verifiable Credentials](https://www.w3.org/TR/vc-data-model/) (VCs)
  and [Decentralized Identifiers](https://www.w3.org/TR/did-core/)* (DIDs) to make identities and claims about those identities
  first-class citizen in Corda (aka native SSI for Corda). Possibilities are endless by modelling the world
  from people to objects and relationships among them with DIDs and VCs. VCs can be created,
  signed, exchanged, verified, and saved in a vault not unlike Corda transactions.
- Focus on W3C standards to (over time)  support a wide range of SSI implementations (soverin,  uport, etc.).
- Support for [did:web method](https://w3c-ccg.github.io/did-method-web/) to bootstrap DIDs with
  HTTPS to ease development and avoid relying on other more complex SSI implementation from start.
- Support for [hashlink](https://tools.ietf.org/html/draft-sporny-hashlink-04) to implement
  [content integrity protection](https://www.w3.org/TR/vc-data-model/#content-integrity-protection)
  for W3C VCs.
- Letting every Corda Party gain a DID and make it discoverable using
  [universal resolvers](https://medium.com/decentralized-identity/a-universal-resolver-for-self-sovereign-identifiers-48e6b4a5cc3c).
  An adapter service thereby bridges between network maps and universal resolvers.
- Allow *end-users and IoT devices carry a DID to participate in transactions*.
- *JSON as simpler data format* for states and transactions. Renders Corda transactions human-readable!
  Non-Java application can start creating, retrieving and verifying data with little effort.
  Existing tools allow to customize the mapping of data to Java objects while helping with aspects
  like compatibility, versioning and upgrades.
- Adoptions of [JSON Web Signatures](https://tools.ietf.org/html/rfc7515) to *ease signing and verifying of
  transactions by any device*, from a Corda server to browsers, mobiles and IoT devices.
- Blurring the boundaries between Corda transactions/states and W3C VCs by modelling the former with the later.
  The benefits are two-fold: let W3C VCs gain Corda functionality and let W3C VC implementations consume transactions.
- A *REST API* following the open [JSON:API](https://jsonapi.org/) specification complementing the proprietary
  Corda RPC protocol to ease working with Corda across a wider range of clients.
- A monitoring endpoint offering health checking and Prometheus-based metrics.

This project is under early but active development. Feedback very welcomed.

It is important to note that all extensions are complementary to the existing Corda features
and can be used together or individually with any Corda server. The extensions do not break
existing features. Further, we focus on implementing W3C standards rather than working with
any single













