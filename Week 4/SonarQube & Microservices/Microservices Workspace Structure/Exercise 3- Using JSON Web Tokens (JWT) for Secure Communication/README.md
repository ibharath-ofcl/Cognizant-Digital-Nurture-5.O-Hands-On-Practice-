# JWT Authentication in Spring Boot 3 & Spring Security 6

This repository contains a complete, production-ready REST API implementing stateless security using JSON Web Tokens (JWT) with **Spring Boot 3.3.x**, **Spring Security 6.3.x**, **Java 17**, and the latest **JJWT 0.12.x** library.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Folder Structure & Package Purpose](#folder-structure--package-purpose)
3. [Dependency Breakdown](#dependency-breakdown)
4. [Configuration Details](#configuration-details)
5. [Core Implementation Explanations](#core-implementation-explanations)
6. [JWT Core Concepts & Structure](#jwt-core-concepts--structure)
7. [Authentication Flow & Architecture](#authentication-flow--architecture)
8. [Testing & Verification (cURL & Postman)](#testing--verification-curl--postman)
9. [Common Errors & Troubleshooting](#common-errors--troubleshooting)
10. [Security & Architectural Best Practices](#security--architectural-best-practices)
11. [Spring Security 6 & JWT Interview Q&A (20 Questions)](#spring-security-6--jwt-interview-qa-20-questions)

---

## Project Overview

This project provides a robust template for securing microservices and web APIs. It is built to show best practices for:
* **Stateless Session Management**: No server-side HTTP session state is stored.
* **Modern Cryptography**: Signatures using HMAC SHA-256 with strong Base64 keys.
* **Granular Exception Handling**: Filters route security exceptions straight to the Global Exception Controller.
* **Declarative Security Filters**: Customized filter-chain wiring using `SecurityFilterChain` bean definitions.

---

## Folder Structure & Package Purpose

The project is structured under `src/main/java/com/cognizant/jwtauthentication`:

```
com.cognizant.jwtauthentication
│
├── config                      # Application configurations (e.g., JwtConfig mapping yml properties)
├── controller                  # REST endpoints (AuthenticationController, SecureController)
├── dto                         # Data Transfer Objects (LoginRequest, LoginResponse, ErrorResponse)
├── exception                   # Custom exceptions and GlobalExceptionHandler
├── security                    # Spring Security configuration and JwtAuthenticationFilter
├── util                        # Helper utilities (JwtTokenProvider)
└── JwtAuthenticationApplication.java # Bootstrap entry point
```

### Package Explanations:
* **`config`**: Holds configuration classes. In modern Spring Boot 3, using configuration classes to bind properties using `@ConfigurationProperties` is favored over scattering `@Value` annotations across classes.
* **`controller`**: Contains HTTP endpoints. This isolates the MVC entry points from security logic and business operations.
* **`dto`**: Data Transfer Objects define the JSON payloads accepted and returned by our API, enforcing data validation rules.
* **`exception`**: Holds global interceptors that format exceptions (like validation errors, bad credentials, or expired tokens) into standard JSON error objects.
* **`security`**: Contains security filters and security policies (like CORS, CSRF, and route protections) wired via Spring Security 6.
* **`util`**: Utility classes housing standalone operational logic (like token generation, verification, and claims extraction).

---

## Dependency Breakdown

The dependencies configured in [pom.xml](file:///Users/bharathbodduvenkata/SonarQube%20%26%20Microservices/Exercise%203-%20Using%20JSON%20Web%20Tokens%20%28JWT%29%20for%20Secure%20Communication/pom.xml) serve the following purposes:

1. **Spring Boot Starter Web**: Includes Apache Tomcat and Spring MVC to construct RESTful web APIs.
2. **Spring Boot Starter Security**: Configures the Spring Security framework and applies default filters to protect endpoints.
3. **Spring Boot Starter Validation**: Brings in Hibernate Validator to evaluate annotations like `@NotBlank`, `@Min`, `@NotNull` on controller payloads.
4. **Spring Boot DevTools**: Enables fast, automatic application restarts and LiveReload for developers.
5. **Lombok (Dependency Declared)**: Declared to satisfy standard project parameters for compiling boilerplate methods.
6. **JJWT API (`jjwt-api`)**: Declared with version `0.12.5`. This provides the interfaces and builders to manage JSON Web Tokens in Java.
7. **JJWT Implementation (`jjwt-impl`)**: Runtime implementation dependency for JJWT.
8. **JJWT Jackson (`jjwt-jackson`)**: Jackson adapter used by JJWT at runtime to map and serialize/deserialize JSON claims.
9. **Spring Boot Starter Test**: Bundles testing tools like JUnit 5, Mockito, and Spring Test runner.
10. **Spring Security Test**: Extends testing capabilities to mock secure requests using `MockMvc` configurations.

---

## Configuration Details

### properties mapping in `application.yml`
```yaml
server:
  port: 8080                    # Port on which the Spring Boot application listens

spring:
  application:
    name: jwt-authentication    # Name of the microservice

jwt:
  secret: Y29nbml6YW50LWV4ZXJjaXNlLTMtand0LWF1dGhlbnRpY2F0aW9uLXNlY3VyZS1zZWNyZXQta2V5LWRldmVsb3BtZW50
                                # A Base64-encoded signing key (>= 256 bits) used to sign HMAC-SHA tokens.
  expiration: 3600000           # Expiration duration in milliseconds (3600000ms = 1 hour)

logging:
  level:
    root: INFO
    com.cognizant.jwtauthentication: DEBUG  # Configures debug-level output for our custom code
    org.springframework.security: DEBUG     # Exposes security filter logs for debugging authentication
```

### Part 5 – JWT Config Properties Loading
In **Spring Boot 3**, loading properties using `@ConfigurationProperties(prefix = "jwt")` in [JwtConfig.java](file:///Users/bharathbodduvenkata/SonarQube%20%26%20Microservices/Exercise%203-%20Using%20JSON%20Web%20Tokens%20(JWT)%20for%20Secure%20Communication/src/main/java/com/cognizant/jwtauthentication/config/JwtConfig.java) is preferred over `@Value` due to:
* **Type Safety**: Maps properties directly to strong Java data types.
* **Validation**: Supports JSR-303 annotations (`@NotBlank`, `@Min`) to validate parameters on start-up.
* **Relaxed Binding**: Resolves properties configured as camelCase, kebab-case, or snake_case automatically.
* **Modularity**: Groups related parameters into clean, testable configuration beans.

---

## Core Implementation Explanations

### 1. `JwtTokenProvider.java` Methods
* `generateToken(Authentication authentication)`: Resolves the authenticated user's name and roles, binds them as custom claims, stamps issued-at and expiration dates, signs the payload using the HMAC-SHA signing key, and compacts it.
* `validateToken(String token)`: Parses the signature and assertions of the JWT. Throws relevant exceptions (like `ExpiredJwtException`, `SignatureException`, etc.) if the token is invalid or expired.
* `extractUsername(String token)`: Extracts the `subject` claim representing the authenticated username.
* `extractExpiration(String token)`: Parses the `expiration` claim.
* `isTokenExpired(String token)`: Checks if the token's expiration timestamp is before the current system time.
* `getAuthentication(String token)`: Converts token claims back into a Spring Security `Authentication` object containing a principal (`User`) and their authorities.

### 2. `JwtAuthenticationFilter.java` Interception Flow
This filter intercepts all incoming HTTP calls.
* **Extracts Bearer Token**: Evaluates the `Authorization` header. If it starts with `Bearer `, it cuts out the token payload.
* **Validates JWT**: Calls `tokenProvider.validateToken()`.
* **Stores Principal**: If valid, extracts username and roles, wraps them in a `UsernamePasswordAuthenticationToken`, and loads it into Spring Security's `SecurityContextHolder`.
* **Elegant Exception Routing**: If a JWT error occurs (e.g. expired or tampered), the filter catches the exception and routes it directly to Spring's `HandlerExceptionResolver`. This allows our `@ControllerAdvice` (`GlobalExceptionHandler`) to format the response as JSON.

### 3. `SecurityConfig.java` Beans & Security Policy
* **`SecurityFilterChain`**: Configures our security policies.
  * Disables CSRF because stateless REST services don't use cookies and are not vulnerable to CSRF.
  * Sets Session creation policy to `STATELESS` to prevent session tracking.
  * Binds endpoints: Permits access to `/authenticate` and `/login` globally, but enforces `.authenticated()` on all other requests.
  * Injects `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.
* **`AuthenticationManager`**: Traditional Spring Security component that processes authentication requests.
* **`PasswordEncoder`**: Declares `BCryptPasswordEncoder` to safely verify password hashes.
* **`UserDetailsService`**: Provisions in-memory test credentials (`admin` / `password123` and `user` / `user123`).

### 4. Controller Access & User Retrieval
* In [SecureController.java](file:///Users/bharathbodduvenkata/SonarQube%20%26%20Microservices/Exercise%203-%20Using%20JSON%20Web%20Tokens%20(JWT)%20for%20Secure%20Communication/src/main/java/com/cognizant/jwtauthentication/controller/SecureController.java), the authenticated user details are resolved inside `/profile` using `@AuthenticationPrincipal UserDetails userDetails`.
* The annotation tells Spring MVC to search the current thread's `SecurityContext` (set by the `JwtAuthenticationFilter`) and inject the authenticated user's principal directly into the controller method parameter.

### 5. Global Exception Handling mapping
* **`BadCredentialsException` / `AuthenticationException`**: Handled when incorrect passwords/usernames are supplied to `/authenticate`. Returns `401 Unauthorized`.
* **`AccessDeniedException`**: Fired when a user attempts to access a resource they do not have roles/permissions for. Returns `403 Forbidden`.
* **`ExpiredJwtException`**: Handled when an expired JWT is detected. Returns `401 Unauthorized`.
* **`SignatureException`**: Fired when the signature has been tampered with. Returns `401 Unauthorized`.
* **`JwtException`**: Handles other general errors in parsing JWTs. Returns `401 Unauthorized`.
* **`MethodArgumentNotValidException`**: Handles parameter validation failures. Returns `400 Bad Request`.

---

## JWT Core Concepts & Structure

A **JSON Web Token (JWT)** is an open standard (RFC 7519) defining a compact, self-contained method for securely transmitting information between parties as a JSON object.

### Stateful vs. Stateless Authentication
```
Stateful Authentication (Session-based)
Client              Server
  │ ── Credentials ─> │ (Generates Session ID, stores it in Database/Memory)
  │ <── Session ID ── │ 
  │ ── Session ID ──> │ (Looks up session in DB on every request)

Stateless Authentication (Token-based)
Client              Server
  │ ── Credentials ─> │ (Validates credentials, signs JWT, returns it)
  │ <── Signed JWT ── │ (No state stored on server!)
  │ ── Bearer JWT ──> │ (Server decodes and verifies token cryptographically)
```

### Structure of a JWT (Dot-Separated)
A JWT consists of three parts separated by dots (`.`): `Header.Payload.Signature`

```
┌────────────────────────────────────────────────────────┐
│ Header (Algorithm & Token Type)                        │
│ e.g. {"alg": "HS256", "typ": "JWT"}                    │
├────────────────────────────────────────────────────────┤
│ Payload (Claims: Subject, Expiration, Roles)          │
│ e.g. {"sub": "admin", "roles": "ROLE_ADMIN", ...}      │
├────────────────────────────────────────────────────────┤
│ Signature (Verified via cryptographically signed key) │
│ HMACSHA256(base64Url(Header) + "." + base64Url(Payload), secret) │
└────────────────────────────────────────────────────────┘
```

* **Header**: Contains the algorithm used to sign the token (e.g. HS256) and the token type (JWT).
* **Payload**: Contains the claims. Claims are statements about an entity (typically, the user) and additional metadata:
  * **Subject (`sub`)**: Identifies the principal (user).
  * **Expiration (`exp`)**: Identifies the expiration time after which the JWT is invalid.
  * **Issuer (`iss`)**: Identifies the service that issued the JWT.
* **Signature**: Built by taking the encoded header, the encoded payload, a secret key, and signing them with the algorithm defined in the header. The signature is used to verify that the sender is who they claim to be and that the message wasn't tampered with.
* **Bearer Token**: The HTTP headers use the schema: `Authorization: Bearer <JWT_TOKEN>`. The word "Bearer" indicates that the bearer of the token is granted access.
* **Secret Key**: Cryptographic key used to verify and sign tokens. Must be kept confidential and be at least 256 bits for HMAC-SHA256.

---

## Authentication Flow & Architecture

Below is the execution flow from the moment a client requests authentication to accessing secure resources:

```
Client                  Security Config             Auth Controller           JwtTokenProvider
  │                           │                            │                          │
  │ ── 1. POST /authenticate ─>                            │                          │
  │    (Username/Password)    │                            │                          │
  │                           │ ── 2. Validate & Auth ────>│                          │
  │                           │    Credentials             │                          │
  │                           │                            │ ─ 3. Generate Token ────>│
  │                           │                            │ <─ 4. Return Token ──────│
  │ <─ 5. JSON Response ──────│                            │                          │
  │    (Bearer <token>)       │                            │                          │
  │                           │                            │                          │
  │ ── 6. GET /secure ────────> [JwtAuthenticationFilter]  │                          │
  │    (Header: Bearer JWT)   │       │                    │                          │
  │                           │       │ ─ 7. Validate ───────────────────────────────>│
  │                           │       │ <─ 8. Token Valid ────────────────────────────│
  │                           │       │                    │                          │
  │                           │       │ ─ 9. Set Context ─> [SecurityContextHolder]   │
  │                           │                            │                          │
  │                           │ ── 10. Forward Request ───> [SecureController]        │
  │ <─ 11. "This is secure" ──│                            │                          │
```

---

## Testing & Verification (cURL & Postman)

### Start the Application
Run the following command in the terminal to start the Spring Boot server:
```bash
mvn spring-boot:run
```

---

### cURL Commands & Expected Outputs

#### 1. Authenticate (Successful Login)
Request:
```bash
curl -X POST http://localhost:8080/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password123"}'
```
Expected Output:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImNvbS5jb2duaXphbnQucm9sZXMiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImlhdCI6MTcxOTcwNjAwMCwiZXhwIjoxNzE5NzA5NjAwfQ...",
  "type": "Bearer"
}
```

#### 2. Access Secure Endpoint (With Valid Token)
*(Replace `YOUR_JWT_TOKEN` with the token generated in the previous step)*
Request:
```bash
curl -X GET http://localhost:8080/secure \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```
Expected Output:
```
This is a secure endpoint.
```

#### 3. Access User Profile (With Valid Token)
Request:
```bash
curl -X GET http://localhost:8080/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```
Expected Output:
```json
{
  "username": "admin",
  "roles": ["ROLE_ADMIN", "ROLE_USER"],
  "enabled": true,
  "accountNonLocked": true,
  "credentialsNonExpired": true,
  "accountNonExpired": true
}
```

#### 4. Access Root Status Endpoint (With Valid Token)
Request:
```bash
curl -X GET http://localhost:8080/ \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```
Expected Output:
```
JWT Authentication is working successfully.
```

#### 5. Unauthorized Access (No Token)
Request:
```bash
curl -X GET http://localhost:8080/secure
```
Expected Output:
```json
{
  "timestamp": "2026-06-29T22:45:00.123456",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/secure"
}
```

#### 6. Access with Invalid Token
Request:
```bash
curl -X GET http://localhost:8080/secure \
  -H "Authorization: Bearer invalid_token_value"
```
Expected Output:
```json
{
  "timestamp": "2026-06-29T22:45:10.123456",
  "status": 401,
  "error": "Unauthorized (Invalid Token)",
  "message": "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0",
  "path": "/secure"
}
```

---

### Postman Testing Guide
1. **Authenticate**:
   * Method: `POST`
   * URL: `http://localhost:8080/authenticate`
   * Body: Select `raw` and `JSON`, then enter:
     ```json
     {
       "username": "admin",
       "password": "password123"
     }
     ```
   * Send the request and copy the `token` value from the response.
2. **Access Secured Routes**:
   * Method: `GET`
   * URL: `http://localhost:8080/secure` or `http://localhost:8080/profile`
   * Under the **Authorization** tab, select type **Bearer Token**.
   * Paste the copied JWT token into the **Token** text box.
   * Send the request and verify the response.

---

## Common Errors & Troubleshooting

* **`401 Unauthorized`**:
  * **Cause**: Authorization header is missing, incorrect schema (e.g. missing `Bearer ` prefix), or the credentials supplied to `/authenticate` are incorrect.
  * **Solution**: Check credentials and verify that the header is set as `Authorization: Bearer <token>`.
* **`403 Forbidden`**:
  * **Cause**: The authenticated user lacks the required security roles to access the resource.
  * **Solution**: Review security rule matcher patterns and configure the user details with appropriate roles in `SecurityConfig`.
* **`SignatureException` (Invalid Signature)**:
  * **Cause**: The token signature does not match the computed signature using the server's secret key (indicating tampering or that the secret key was rotated).
  * **Solution**: Re-authenticate to get a fresh token signed with the current key.
* **`ExpiredJwtException` (Expired JWT)**:
  * **Cause**: The token has exceeded its expiration window (`jwt.expiration`).
  * **Solution**: Request a new token from `/authenticate`.
* **`MalformedJwtException`**:
  * **Cause**: The token does not have 3 base64-encoded segments separated by dots.
  * **Solution**: Verify the token copy-paste integrity.
* **`BeanCreationException` / Dependency Conflicts**:
  * **Cause**: Mixed dependencies or old JJWT libraries.
  * **Solution**: Verify `pom.xml` excludes deprecated signatures and relies strictly on standard dependencies with correct scopes.

---

## Security & Architectural Best Practices

1. **Keep Secrets Secure**: Never hardcode JWT signing keys. Store them in environment variables or configuration servers (e.g. HashiCorp Vault, AWS Secrets Manager) and inject them at runtime.
2. **Use HTTPS**: Secure connections are critical. Without HTTPS, tokens can be intercepted in transit via packet sniffing.
3. **Short Expiration Windows**: Keep token lifespan short (e.g. 15-30 minutes) to minimize damage if a token is compromised.
4. **Implement Refresh Tokens**: Use refresh tokens saved securely (e.g. HttpOnly, SameSite cookies) to request new access tokens.
5. **Rotate Keys**: Frequently update secret keys to render old compromises invalid.
6. **Stateless Operations**: Never hold session state on the server. Keeping the server stateless makes it easy to scale horizontally.
7. **Use Constructor Injection**: Inject dependencies through constructors rather than field injection (`@Autowired` on variables) to improve testability and ensure immutability.

---

## Spring Security 6 & JWT Interview Q&A (20 Questions)

#### Q1: What is a JSON Web Token (JWT)?
**A**: A self-contained, digitally signed token format used to safely transmit claims as JSON objects. It consists of a Header, Payload, and Signature, separated by dots (`.`).

#### Q2: How does stateless authentication differ from stateful authentication?
**A**: In stateful authentication, the server generates a session ID, stores it in memory or a database, and validates it against client cookies. In stateless authentication, the server stores no session records. The client provides a cryptographically signed token (like a JWT) on every request, which the server decodes and validates on the fly.

#### Q3: Why do we disable CSRF protection in stateless REST APIs?
**A**: CSRF (Cross-Site Request Forgery) protection is used to prevent malicious sites from hijacking cookie-based browser sessions. Since stateless REST APIs do not use browser session cookies for authentication (relying instead on request headers like `Authorization: Bearer <token>`), they are immune to traditional CSRF attacks, allowing CSRF protection to be safely disabled.

#### Q4: What is the purpose of the `SecurityFilterChain` bean in Spring Security 6?
**A**: It defines a series of filters (cors, csrf, authorizeHttpRequests, exceptionHandling, custom filters) that process incoming HTTP requests to apply authentication and authorization rules before forwarding requests to the MVC controllers.

#### Q5: Why does `JwtAuthenticationFilter` extend `OncePerRequestFilter`?
**A**: `OncePerRequestFilter` guarantees that the filter is executed exactly once per request. This prevents redundant processing when requests are forwarded or dispatched internally during the same HTTP transaction.

#### Q6: How does Spring Security 6 handle authorization rules compared to older versions?
**A**: Spring Security 6 uses lambda expressions inside `SecurityFilterChain` declarations (e.g. `http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())`) and has deprecated method-chaining configuration blocks.

#### Q7: How does JJWT 0.12.x generate a signing key safely?
**A**: It uses `Keys.hmacShaKeyFor(byte[])` to create a strong cryptographic key from a Base64-decoded byte array, ensuring that the key is of sufficient length (at least 256 bits for HMAC-SHA).

#### Q8: What does the `subject` claim represent in a JWT?
**A**: The `subject` (labeled as `sub`) represents the primary identifier of the authenticated user (typically their username or user ID).

#### Q9: What happens when an expired JWT is sent to the server?
**A**: The parser throws an `ExpiredJwtException` during verification. In our application, this is caught by `JwtAuthenticationFilter` and sent to the exception resolver, which returns a `401 Unauthorized` JSON payload to the user.

#### Q10: How are exceptions thrown inside a security filter caught by a `@ControllerAdvice` handler?
**A**: Since security filters execute before the DispatcherServlet, standard controller exception handlers cannot catch filter exceptions directly. To bridge this, the filter catches security errors and routes them to Spring's `HandlerExceptionResolver` bean using `resolver.resolveException(...)`, which forwards them directly to the controller advice.

#### Q11: What is the difference between Authentication and Authorization?
**A**: **Authentication** verifies *who* you are (identifying credentials). **Authorization** determines *what* you are allowed to do (checking roles or permissions).

#### Q12: Why is BCryptPasswordEncoder preferred for hashing passwords?
**A**: BCrypt uses a configurable salt and a slow hashing algorithm to resist brute-force and rainbow-table attacks.

#### Q13: What is the purpose of the `Bearer` token scheme?
**A**: It is an OAuth2 standard defining that access is granted to the bearer of the token, without requiring additional verification credentials.

#### Q14: How does `@AuthenticationPrincipal` retrieve the user principal?
**A**: It resolves the parameter by reading the principal property of the `Authentication` object currently stored in the thread's `SecurityContextHolder`.

#### Q15: How can a JWT be invalidated before its expiration time?
**A**: Since JWTs are stateless, they cannot be invalidated on the server directly. Invalidation requires either blacklisting tokens in a fast database (like Redis) or rotating the signing secret key.

#### Q16: What is a Refresh Token and why is it used?
**A**: A refresh token is a long-lived token used to get a new short-lived access token, allowing users to remain logged in safely without exposing access credentials for long periods.

#### Q17: What is the difference between OAuth2 and JWT?
**A**: **OAuth2** is an authorization framework defining how actors share resources. **JWT** is a token format. OAuth2 can use JWTs as access tokens.

#### Q18: What is the benefit of constructor injection over field injection?
**A**: Constructor injection guarantees that required dependencies are initialized at object creation time, supports immutability (using `final` fields), and simplifies unit testing.

#### Q19: Why is standard Java code sometimes preferred over Lombok annotations?
**A**: While Lombok reduces boilerplate, it modifies bytecode during compilation and can cause compatibility issues when updating compiler versions (like JDK 25/26). Manual Java code compiles reliably on all Java environments.

#### Q20: What are the risks of using a small JWT secret key?
**A**: A small key (under 256 bits) is vulnerable to brute-force attacks, which could allow attackers to decipher the key and forge valid JWT tokens.
