# Étape de build
FROM openjdk:24-ea-21-slim-bookworm AS build
WORKDIR /app

# Copier les fichiers du projet
COPY . .

# Exécuter le build Gradle
RUN ./gradlew build --no-daemon

# Étape de l'application
FROM openjdk:24-ea-21-slim-bookworm
VOLUME /main-app
WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]