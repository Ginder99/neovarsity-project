FROM eclipse-temurin:21-jre

RUN groupadd --system spring && useradd --system --gid spring spring
WORKDIR /app
COPY build/libs/app.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
