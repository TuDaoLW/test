# Build stage
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/kafka-producer-0.0.1-SNAPSHOT.jar /app.jar
COPY src/main/resources/certs/truststore.jks /etc/kafka/certs/truststore.jks
ENTRYPOINT ["java", "-jar", "/app.jar"]