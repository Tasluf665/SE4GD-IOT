# Fase 1: Build
FROM maven:3.8.8-eclipse-temurin-21-alpine AS build

RUN apk add --no-cache git

# Set the working directory in the container
WORKDIR /app

# Step 4: Clone the Git repository
ARG GITHUB_TOKEN
RUN echo "Cloning repository..."
RUN git clone --sparse https://$GITHUB_TOKEN:x-oauth-basic@github.com/MDEGroup/se4iot.git .

WORKDIR /app

# Set sparse-checkout
RUN git sparse-checkout init --cone
RUN git sparse-checkout set simple-coap-server

WORKDIR /app/simple-coap-server
RUN mvn dependency:go-offline -B
RUN mvn clean install
RUN mvn package -DskipTests

# Fase 2: Run
FROM amazoncorretto:21.0.5-alpine3.20

# Set the working directory
WORKDIR /app

# Copy the lib directory if there are additional dependencies
COPY --from=build /app/simple-coap-server/target/lib/* /app/lib/

# Copia il JAR generato dalla fase di build nella working directory del nuovo container
COPY --from=build /app/simple-coap-server/target/*.jar simple-coap-server.jar

# Esponi la porta su cui il servizio è in ascolto
EXPOSE 5684

# Comando per eseguire l'applicazione
ENTRYPOINT ["java",  "-Dconfig.file=Californium3.properties", "-cp", "simple-coap-server.jar:/app/lib/*", "server.SimpleCoapServer"]