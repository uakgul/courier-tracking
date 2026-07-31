# Build stage - the Gradle toolchain asks for Java 26, so build on a JDK 26 image.
FROM eclipse-temurin:26-jdk AS build
WORKDIR /workspace

# Wrapper and build scripts first: Docker can reuse this layer while only sources change.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon --version

COPY src src
# Tests run via ./gradlew build; keep the image build focused on producing the jar.
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:26-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
