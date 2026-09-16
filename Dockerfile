# syntax=docker/dockerfile:1

# ---------- Etapa 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# -DskipTests: las pruebas ya corrieron en el pipeline de CI (Actividad 2);
# esta imagen solo empaqueta el artefacto ya validado.
RUN mvn -B -DskipTests clean package

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/app-inventario-tienda-1.0.0.jar app.jar

ENV SPRING_PROFILES_ACTIVE=staging
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
