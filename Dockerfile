FROM gradle:9.1.0-jdk25 AS build

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY src src

RUN chmod +x gradlew && ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --create-home spring
USER spring

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
