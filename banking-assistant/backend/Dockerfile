# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY /home/tekuser/Demo/banking-assistant/backend/src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --shell /bin/bash appuser
COPY --from=build /build/target/*.jar app.jar
RUN chown appuser:appuser app.jar
USER appuser

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
