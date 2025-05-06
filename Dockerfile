FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy Maven wrapper files
COPY mvnw .
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x mvnw

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies (will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create a non-root user for running the application
RUN groupadd -r testcraftai && useradd -r -g testcraftai testcraftai

# Copy application from build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Set ownership and permissions
RUN chown -R testcraftai:testcraftai /app
USER testcraftai

# Set environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Expose the application port
EXPOSE 8080

# Set healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]