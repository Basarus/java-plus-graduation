FROM maven:3.9.11-amazoncorretto-21 AS builder
WORKDIR /application

COPY . .

RUN mvn clean package -pl infra/config-server -am -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /application/infra/config-server/target/config-server-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]