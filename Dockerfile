# Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests
# Extract stage (optional, for clarity)
FROM build AS extract
WORKDIR /app
CMD ["cp", "/app/target/kafka-producer-0.0.1-SNAPSHOT.jar", "/output/"]
# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser
COPY --from=build /app/target/kafka-producer-0.0.1-SNAPSHOT.jar /app.jar
#COPY src/main/resources/certs/truststore.jks /etc/kafka/certs/truststore.jks
#ENTRYPOINT ["java", "-jar", "/app.jar"]
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-XX:+UseG1GC", "-XX:+UseContainerSupport", "-jar", "/app.jar"]
