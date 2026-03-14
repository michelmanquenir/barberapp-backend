# ─── STAGE 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copiar archivos de Gradle primero (cache de dependencias)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x gradlew
# Descargar dependencias (cacheado si build.gradle no cambia)
RUN ./gradlew dependencies --no-daemon || true

# Copiar el código fuente y compilar
COPY src src
RUN ./gradlew build -x test --no-daemon

# ─── STAGE 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/build/libs/*.jar app.jar

# Puerto que expone Spring Boot (se mapea a $PORT en Render)
EXPOSE 8080

# Spring Boot leerá la variable PORT de Render automáticamente
ENTRYPOINT ["java", "-jar", "app.jar"]
