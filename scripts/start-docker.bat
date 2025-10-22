@echo off
echo Building the application...
mvn clean package -DskipTests

echo Starting Docker containers...
docker-compose down
docker-compose up --build -d

echo Waiting for services to be ready...
timeout 30

echo Checking application health...
curl -f http://localhost:8080/actuator/health

if %errorlevel% equ 0 (
    echo ✅ Application is running successfully!
    echo 🌐 Access URLs:
    echo    Application: http://localhost:8080
    echo    Swagger UI: http://localhost:8080/swagger-ui.html
    echo    Actuator: http://localhost:8080/actuator/health
) else (
    echo ❌ Application failed to start. Check logs with: docker-compose logs app
)