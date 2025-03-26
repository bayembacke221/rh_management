# Étape de build
FROM openjdk:21-slim AS build
WORKDIR /app

# Copier les fichiers du projet
COPY . .

# Donner les permissions d'exécution au script gradlew
RUN chmod +x ./gradlew

# Exécuter le build Gradle
RUN ./gradlew build --no-daemon

# Étape de l'application
FROM openjdk:21-slim
VOLUME /main-app
WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]