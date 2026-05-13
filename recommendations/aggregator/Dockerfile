FROM maven:3.9.11-amazoncorretto-21 AS builder
WORKDIR /application

COPY . .

RUN mvn clean package -pl recommendations/aggregator -am -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /application/recommendations/aggregator/target/aggregator-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]