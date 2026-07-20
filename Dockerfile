FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
RUN mkdir -p /app/data && chown -R app:app /app

COPY --from=build /app/target/quarkus-app /app/quarkus-app

USER app
ENV QUARKUS_DATASOURCE_JDBC_URL=jdbc:sqlite:/app/data/bookrating.db
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/quarkus-app/quarkus-run.jar"]
