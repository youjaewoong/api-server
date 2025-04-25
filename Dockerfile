# Step 1: Build the application with Gradle 7.4 and Corretto-11
FROM amazoncorretto:11-alpine AS build

# Install Gradle 7.4
RUN apk add --no-cache bash && \
    wget https://services.gradle.org/distributions/gradle-7.4-bin.zip && \
    unzip gradle-7.4-bin.zip -d /opt && \
    ln -s /opt/gradle-7.4/bin/gradle /usr/local/bin/gradle

# Set the working directory in the container
WORKDIR /app

# Copy the build.gradle and settings.gradle files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle


# Copy the source code into the container
COPY . .

# Build the Spring Boot application (creates the JAR file)
RUN gradle :api-application:bootJar --no-daemon

# Step 2: Create a smaller image for running the application
FROM amazoncorretto:11-alpine

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/api-application/build/libs/*.jar app.jar

# Expose port 9001 (change if necessary)
EXPOSE 9001

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
