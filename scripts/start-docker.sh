#!/bin/bash

# Build the application
echo "Building the application..."
mvn clean package -DskipTests

# Start Docker containers
echo "Starting Docker containers..."
docker-compose down
docker-compose up --build -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 30

# Check if application is running
echo "Checking application health..."
curl -f http://localhost:8080/actuator/health

if [ $? -eq 0 ]; then
    echo "✅ Application is running successfully!"
    echo "🌐 Access URLs:"
    echo "   Application: http://localhost:8080"
    echo "   Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "   Actuator: http://localhost:8080/actuator/health"
else
    echo "❌ Application failed to start. Check logs with: docker-compose logs app"
fi