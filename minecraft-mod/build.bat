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
if errorlevel 1 (
  echo [ERROR] No se encontro Gradle ni gradlew.bat.
  echo.
  echo Descarga/crea el Gradle Wrapper y vuelve a ejecutar este archivo,
  echo o instala Gradle y dejalo disponible en PATH.
  pause
  exit /b 1
)

echo [INFO] Usando Gradle del sistema...
gradle clean build
if errorlevel 1 goto :failed

goto :success

:failed
echo.
echo [ERROR] La compilacion fallo.
pause
exit /b 1

:success
echo.
echo [OK] Compilacion terminada.
echo JAR: %~dp0build\libs\music-mod-1.0.0.jar
echo.
if exist "%~dp0build\libs\music-mod-1.0.0.jar" start "" explorer "%~dp0build\libs"
pause
