FROM openjdk:11

WORKDIR /app

# Копируем всё из папки OOP
COPY . .

# Компилируем
RUN javac -d target/classes -cp ".:lib/sqlite-jdbc-3.42.0.0.jar" src/*.java

# Создаем JAR
RUN echo "Main-Class: WebService" > MANIFEST.MF && \
    echo "Class-Path: lib/sqlite-jdbc-3.42.0.0.jar" >> MANIFEST.MF && \
    jar cfm app.jar MANIFEST.MF -C target/classes .

# Используем порт из переменной окружения или 8080
ENV PORT=8080

# Запускаем с указанием порта
CMD ["sh", "-c", "java -cp \".:lib/*:app.jar\" WebService ${PORT}"]

EXPOSE 8080
