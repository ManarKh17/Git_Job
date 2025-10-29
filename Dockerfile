FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.war app.war
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.war"]
