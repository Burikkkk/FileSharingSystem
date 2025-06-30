# Используем официальный минимальный образ с JDK 17
FROM eclipse-temurin:17-jdk-jammy

# Автор
LABEL authors="Aleksandra"

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл (после сборки)
COPY target/*.jar app.jar

# Открываем порт 8080 (Spring Boot по умолчанию)
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
