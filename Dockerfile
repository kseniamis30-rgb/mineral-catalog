# OOP/Dockerfile - простой вариант
FROM openjdk:11

WORKDIR /app

# Копируем ВСЁ из текущей папки (OOP) в контейнер
COPY . .

# Убедимся что есть JAR или создадим его
# Если у вас уже есть собранный JAR, уберите следующую строку
RUN javac -d target/classes -cp ".:lib/sqlite-jdbc-3.42.0.0.jar" src/*.java && \
    echo "Main-Class: WebService" > MANIFEST.MF && \
    echo "Class-Path: lib/sqlite-jdbc-3.42.0.0.jar" >> MANIFEST.MF && \
    jar cfm mineral-catalog.jar MANIFEST.MF -C target/classes .

# Запускаем приложение
CMD ["java", "-cp", ".:lib/*:mineral-catalog.jar", "WebService"]

EXPOSE 8080