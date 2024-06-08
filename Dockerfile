FROM eclipse-temurin:22-jdk-alpine

VOLUME /tmp

COPY target/osrm_demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app.jar"]