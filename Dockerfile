# Base image
FROM openjdk:24-ea-21-slim-bookworm

# Maintainer information
LABEL maintainer="mbackembaye74@gmail.com"

# Create a volume for application data
VOLUME /main-app

# Copy the JAR file from Gradle's build directory
# Gradle places build artifacts in the 'build/libs' directory
COPY build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]