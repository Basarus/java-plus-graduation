FROM maven:3.9.11-amazoncorretto-21 AS builder
WORKDIR /application

COPY pom.xml ./
COPY core/pom.xml core/pom.xml

COPY core/event-service/pom.xml core/event-service/pom.xml
COPY core/request-service/pom.xml core/request-service/pom.xml
COPY core/extra-service/pom.xml core/extra-service/pom.xml
COPY core/user-service/pom.xml core/user-service/pom.xml
COPY stat/pom.xml stat/pom.xml
COPY stat/stat-dto/pom.xml stat/stat-dto/pom.xml
COPY stat/stat-client/pom.xml stat/stat-client/pom.xml
COPY stat/stats-server/pom.xml stat/stats-server/pom.xml
COPY recommendations/pom.xml recommendations/pom.xml
COPY recommendations/stats-proto/pom.xml recommendations/stats-proto/pom.xml
COPY recommendations/stats-avro/pom.xml recommendations/stats-avro/pom.xml
COPY recommendations/collector/pom.xml recommendations/collector/pom.xml
COPY recommendations/aggregator/pom.xml recommendations/aggregator/pom.xml
COPY recommendations/analyzer/pom.xml recommendations/analyzer/pom.xml
COPY infra/pom.xml infra/pom.xml
COPY infra/discovery-server/pom.xml infra/discovery-server/pom.xml
COPY infra/config-server/pom.xml infra/config-server/pom.xml
COPY infra/gateway-server/pom.xml infra/gateway-server/pom.xml

ENV MAVEN_OPTS="-Dmaven.repo.local=/app/.m2/repository"
RUN mvn dependency:go-offline -pl stat/stats-server -am -B

COPY stat stat

RUN mvn clean package -pl stat/stats-server -am -DskipTests


FROM amazoncorretto:21.0.8-alpine AS layers
WORKDIR /application

COPY --from=builder /application/stat/stats-server/target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted


FROM amazoncorretto:21.0.8-alpine
VOLUME /tmp

RUN adduser -S spring-user
USER spring-user

WORKDIR /application

COPY --from=layers /application/extracted/dependencies/ ./
COPY --from=layers /application/extracted/spring-boot-loader/ ./
COPY --from=layers /application/extracted/snapshot-dependencies/ ./
COPY --from=layers /application/extracted/application/ ./

RUN java -XX:ArchiveClassesAtExit=app.jsa \
    -Dspring.context.exit=onRefresh \
    -jar app.jar & exit 0

ENV JAVA_CDS_OPTS="-XX:SharedArchiveFile=app.jsa -Xlog:class+load:file=/tmp/classload.log"
ENV JAVA_ERROR_FILE_OPTS="-XX:ErrorFile=/tmp/java_error.log"

ENTRYPOINT exec java \
    $JAVA_ERROR_FILE_OPTS \
    $JAVA_CDS_OPTS \
    -jar app.jar
