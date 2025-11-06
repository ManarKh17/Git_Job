FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.war app.war
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.war"]
