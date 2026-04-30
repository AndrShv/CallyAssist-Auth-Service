FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/Cally-Mobile-Auth-Service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
