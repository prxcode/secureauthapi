## SecureAuthAPI
While building backend for Banking app, ecommerce, Admin dashboard or SaaS Platform. We have to figure out these problems:
- User Must login securely [we can use JWT authentication]
- Passwords must be safe [Password Hashing]
- Only admin access admin APIs [Role based access]
- Prevent Brute Force login [Rate limiting]
- Avoiding hitting database repeatedly [Redis caching]
- Secure APIs [Spring security]
- Deploy easily [Using Docker]

To solve this problem I have built **SecureAuthAPI**, here i demonstrated how using *Java* and *SpringBoot* Framework to built RESTAPIs quickly as it handles web server, dependency injection, config, security, database connection
- While *SpringSecurity* handles authentication, authorization, password encryption, security filters. Now instead of storing sessions in server memory 
- I have used JWT (*JSON Web Token*: a token used to authenticate user). 
- I have also used *Redis*(In memory database used for caching, as its faster than mysql and can store user sessions and cached user data). 
- Last I have used *Docker* to package the application into a container.

## Project Structure
```bash
secureauthapi
│
├── src/main/java/com/example/auth
│
│   ├── controller #handles HTTP requests
│   ├── service #contains business logic like hashing pass, validating login, calling repository
│   ├── repository #handles database operations
│   ├── model #represents database tables
│   ├── security #handles authentication logic like it generates JWT and validate, it also includes security config and Filters
│   ├── config
│   └── middleware #contains filters executed before requests reaches controller as (Requests -> IP Filter -> Rate Limit Filter -> Controller)
│
├── resources
│     application.yml
│
├── pom.xml
└── Dockerfile

```

## Authentication Flow
```bash
User registers
      ↓
Password stored hashed
      ↓
User logs in
      ↓
Server validates password
      ↓
JWT token generated
      ↓
Client stores token
      ↓
Client sends token in header
      ↓
Server verifies JWT
      ↓
Protected APIs allowed
```

## Architecture 
```bash
Client (Postman / Frontend)
          ↓
Controller
          ↓
Service
          ↓
Repository
          ↓
Database

Security Layer
   JWT Validation
   Rate Limiting
   IP Filtering

Cache Layer
   Redis
```


## To run on localhost
- Double click or run `SecureAuthAPI.bat` file, just type `./SecureAuthAPI.bat` in terminal
- Or type these two cmds, `mvn clean install` and `mvn spring-boot:run`


## To Containerize in Docker
- To build docker container `docker build -t secureauthapi .`
- To run docker container `docker run -p 8080:8080 secureauthapi`
