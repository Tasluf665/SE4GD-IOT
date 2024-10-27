FROM openjdk:17

# Set the working directory to /app
WORKDIR /app

# local application port
EXPOSE 5683

# Copy the current directory contents into the container at /app
COPY mycoapserver.jar /app

# Specify the command to run on container start
CMD ["java", "-jar", "mycoapserver.jar"]
