# Build
FROM maven:3.8.8-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests

# Fase 2: Run
FROM amazoncorretto:21.0.5-alpine3.20

# Imposta la working directory
WORKDIR /app

# Copy the lib directory if there are additional dependencies
COPY --from=build /app/target/lib/* /app/lib/

# Copia il JAR generato dalla fase di build nella working directory del nuovo container
COPY --from=build /app/target/*.jar simple-coap-server.jar



# Esponi la porta su cui il servizio è in ascolto
EXPOSE 5683

# Comando per eseguire l'applicazione
ENTRYPOINT ["java", "-cp", "simple-coap-server.jar:/app/lib/*", "server.SimpleCoapServer"]