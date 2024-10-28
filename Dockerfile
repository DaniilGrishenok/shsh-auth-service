# Используем официальный образ OpenJDK как базовый
FROM openjdk:17-jdk-alpine

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем jar-файл сервиса в контейнер
COPY target/auth-service-social-network-0.0.1-SNAPSHOT.jar app.jar

# Указываем команду для запуска сервиса
ENTRYPOINT ["java","-jar","/app/app.jar"]
