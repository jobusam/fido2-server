# Fido 2 Server Sample

## Goal
Implement authentication and authorization via WebAuthN-Standard and Fido2 using JWT for later authorization.

## Current state 
At the moment this server only provides a user login with simple password

## Technologies

- Java 14 with preview functions
    - with records
    - with text blocks
- Javalin as Microframework
- Jackson for Serialization
- Vue.js as Client Framework
- Json Web Tokens (JWT) for Authentication and Autorization
    - The implementation is based on the github repository: https://github.com/kmehrunes/javalin-jwt