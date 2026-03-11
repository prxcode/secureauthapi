# use openjdk base image
FROM openjdk:17

# copy jar into container
COPY target/secureauthapi.jar app.jar

# run application
ENTRYPOINT ["java","-jar","/app.jar"]