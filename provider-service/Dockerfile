FROM eclipse-temurin:24-jre
WORKDIR /app
COPY target/provider-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
