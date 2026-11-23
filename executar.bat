@echo off
cd /d "%~dp0"
if not exist "target\controle-financeiro-1.0.0.jar" (
  echo Compile primeiro: .\mvnw.cmd clean package
  exit /b 1
)
if defined JAVA_HOME (
  "%JAVA_HOME%\bin\java.exe" -jar "target\controle-financeiro-1.0.0.jar"
) else (
  java -jar "target\controle-financeiro-1.0.0.jar"
)
exit /b %errorlevel%
