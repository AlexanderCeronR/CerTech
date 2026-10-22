FROM maven:3.9.16-eclipse-temurin-21@sha256:99e61abcff91a9b1333463bd8451fb18495d6eba9250ac66a338b518f8278320 AS build
WORKDIR /workspace

# Copy the dependency manifest first so Docker can reuse the dependency layer.
COPY backend/pom.xml ./pom.xml
RUN mvn -B -ntp dependency:go-offline
COPY backend/src ./src
RUN mvn -B -ntp verify

FROM eclipse-temurin:21-jre-jammy@sha256:affcab5356e1270ac35d2361ee83135af2f03e8d580eca78a6d5d6fdbf048db0 AS runtime
WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/certech-api-1.0.0.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
