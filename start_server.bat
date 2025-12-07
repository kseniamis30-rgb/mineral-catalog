@echo off
chcp 65001 > nul
title Веб-каталог минералов
color 0A

echo ========================================
echo     ВЕБ-КАТАЛОГ МИНЕРАЛОВ
echo ========================================
echo.

echo 🔥 Освобождаем порт 8080...
netstat -ano | findstr :8080
taskkill /F /PID 12345 2>nul
echo.

echo 1. Проверяем базу данных...
if exist minerals.db (
    for %%F in (minerals.db) do (
        echo ✅ База найдена! Размер: %%~zF байт
    )
) else (
    echo ❌ Файл minerals.db не найден!
    pause
    exit
)

echo.
echo 2. Компилируем...
if not exist bin mkdir bin
javac -cp "lib/*" -encoding UTF-8 -d bin src/*.java

if errorlevel 1 (
    echo ❌ Ошибка компиляции!
    pause
    exit
)
echo ✅ Компиляция успешна

echo.
echo 3. Запускаем сервер...
echo ========================================
echo     ОТКРОЙ В БРАУЗЕРЕ:
echo     http://localhost:8080
echo ========================================
echo.

java -cp "bin;lib/*" -Dfile.encoding=UTF-8 WebService

pause