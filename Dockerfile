# Build stage
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jdk-jammy
ARG PROFILE=dev
ARG APP_VERSION=1.0.0

WORKDIR /app
COPY --from=build /app/target/*.jar /app/

EXPOSE 8080

ENV DB_URL=jdbc:postgresql://postgres-sql-spring-app:5432/spring_app_db

ENV ACTIVE_PROFILE=${PROFILE}
ENV JAR_VERSION=${APP_VERSION}

CMD java -jar -Dspring.profiles.active=${ACTIVE_PROFILE} spring-security-asymmetric-encryption-${JAR_VERSION}.jar