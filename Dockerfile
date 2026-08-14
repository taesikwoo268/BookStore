# ============================================================
# STAGE 1: BUILD
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Cài đặt curl để healthcheck (optional)
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy pom.xml và download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# STAGE 2: RUNTIME
# ============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Tạo user non-root để chạy ứng dụng
RUN addgroup -S bookstore && adduser -S bookstore -G bookstore

# Set working directory
WORKDIR /app

# Copy JAR từ build stage
COPY --from=build /app/target/*.jar app.jar

# Copy CA certificates nếu cần (cho HTTPS)
# COPY --from=build /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/

# Tạo thư mục logs
RUN mkdir -p /app/logs && chown -R bookstore:bookstore /app

# Switch to non-root user
USER bookstore

# Expose port
EXPOSE 8080

# JVM Options - Tối ưu memory cho container
ENV JAVA_OPTS="\
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+UseCompressedOops \
    -XX:MetaspaceSize=64m \
    -XX:MaxMetaspaceSize=128m \
    -Djava.security.egd=file:/dev/./urandom"

# Spring Boot profile
ENV SPRING_PROFILES_ACTIVE=docker

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ============================================================
# STAGE 3: TEST (Optional)
# ============================================================
FROM runtime AS test
# Chỉ dùng cho test, có thể bỏ qua