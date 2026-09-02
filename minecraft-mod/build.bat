@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ==========================================
echo   MUSIC MOD - Minecraft 1.21.1 / Fabric
echo ==========================================

where java >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Java no esta instalado o no esta en PATH.
  echo Necesitas Java 21 para compilar Minecraft 1.21.1.
  pause
  exit /b 1
)

java -version

if exist "%~dp0gradlew.bat" (
  echo [INFO] Usando Gradle Wrapper...
  call "%~dp0gradlew.bat" clean build
  if errorlevel 1 goto :failed
  goto :success
)

where gradle >nul 2>nul
if not errorlevel 1 (
  echo [INFO] Usando Gradle del sistema...
  gradle clean build
  if errorlevel 1 goto :failed
  goto :success
)

echo [INFO] Gradle no esta instalado. Descargando Gradle 8.10.2...
set "GRADLE_HOME=%~dp0.gradle-dist\gradle-8.10.2"
set "GRADLE_ZIP=%~dp0.gradle-dist\gradle-8.10.2-bin.zip"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  if not exist "%~dp0.gradle-dist" mkdir "%~dp0.gradle-dist"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-8.10.2-bin.zip' -OutFile '%GRADLE_ZIP%'"
  if errorlevel 1 goto :gradle_download_failed
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%GRADLE_ZIP%' '%~dp0.gradle-dist'"
  if errorlevel 1 goto :gradle_extract_failed
  del /q "%GRADLE_ZIP%" >nul 2>nul
)

call "%GRADLE_HOME%\bin\gradle.bat" clean build
if errorlevel 1 goto :failed

goto :success

:gradle_download_failed
echo [ERROR] No se pudo descargar Gradle.
pause
exit /b 1

:gradle_extract_failed
echo [ERROR] No se pudo extraer Gradle.
pause
exit /b 1

:failed
echo.
echo [ERROR] La compilacion fallo.
pause
exit /b 1

:success
echo.
echo [OK] Compilacion terminada.
echo JAR generado en: %~dp0build\libs
if exist "%~dp0build\libs" start "" explorer "%~dp0build\libs"
pause
