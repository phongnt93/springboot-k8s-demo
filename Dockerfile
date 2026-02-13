# Multi-stage build

# ==== Build stage ====
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml và tải dependency
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# ==== Runtime stage ====
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Tạo user không phải root
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy jar từ build stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check (tùy chọn, nếu app có endpoint /health)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
