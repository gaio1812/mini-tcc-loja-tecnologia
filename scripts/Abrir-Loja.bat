@echo off
cd /d "%~dp0"
java -jar loja-tecnologia.jar
if errorlevel 1 (
  echo.
  echo Instale Java 17 ou superior. Confira tambem a pasta logs.
  pause
)
