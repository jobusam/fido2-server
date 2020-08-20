# Fido 2 Server Sample

## Goal
Implement authentication and authorization via WebAuthN-Standard and Fido2 using JWT for later authorization.

## Current state 
At the moment this server only provides a user login with a simple password. 
The Fido2 Device Registration is not fully implemented at the moment. 
Application tests will be done with Firefox 79.

## Technologies
- Java 14 with preview functions
    - with records
    - with text blocks
- Javalin as Microframework
- Jackson for Serialization
- Tinylog for Logging
- Vue.js as Client Framework
- Json Web Tokens (JWT) for authentication and authorization
    - The implementation is based on the github repository: https://github.com/kmehrunes/javalin-jwt
- Fido 2 Support
    - The implementation uses the [WebAuthN Server](https://github.com/Yubico/java-webauthn-server) provided by Yubico.

## Setup TLS Support
### Create server certificate (development only)
```shell script
// Create files in resource folder
$ cd resources

// Create server certificate based on elliptic curve keys with ECDSA
$ keytool -keystore serverkeystore.jks -alias jetty-custom -genkey -keyalg EC -sigalg SHA384withECDSA --dname CN=localhost
    -> Enter keystore password:  jetty-pwd
    -> set correct hostname with CN=[hostname]
```
This generated certificate can be used for HTTPS support on Jetty Server.
But due to the fact it's an self-signed cert you have to accept the security exception
in the browser. In production environment you should use Let's encrypt to provide a valid cert
singed by a CA.
