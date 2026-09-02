#FROM ubuntu:latest
#LABEL authors="komal"
#
#ENTRYPOINT ["top", "-b"]

FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . .

# Build jar inside docker
RUN mvn clean package -DskipTests

# ---------- STAGE 2 : Run the jar ----------
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy jar from builder s
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java","-jar","app.jar"]