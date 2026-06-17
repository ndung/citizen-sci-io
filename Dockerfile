FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
RUN mkdir -p /var/app/uploads

ENV PORT=8080
EXPOSE 8080

COPY --from=build /workspace/target/citizen-0.0.1.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
