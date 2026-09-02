# 🎵 Music Mod — Minecraft 1.21.1

Mod **client-side Fabric** para Minecraft 1.21.1 con reproductor de música integrado.

## Funciones

- 🎹 GUI propia con tecla **M**.
- 🔎 Buscador de canciones/artistas con `yt-dlp`.
- ▶️ Reproducir resultado.
- ➕ Añadir resultados a la cola.
- ⏮️ Anterior / ⏭️ siguiente.
- ⏸️ Pausar / ▶️ reanudar.
- ⏹️ Detener y vaciar cola.
- 🔊 Volumen desde la GUI o comando.
- 📦 Instalación automática de `yt-dlp` y FFmpeg al iniciar Minecraft en Windows x64.
- 💬 Comandos `/music`.

## Dependencias automáticas

En Windows x64, al abrir Minecraft el mod prepara:

```text
.minecraft/musicmod/bin/
```

y descarga `yt-dlp.exe` y FFmpeg. El instalador se ejecuta en segundo plano para no congelar el menú de Minecraft y muestra su estado en la GUI.

El build estático de FFmpeg para Windows x64 se obtiene desde la release `latest` de BtbN; actualmente esa distribución publica un paquete estático GPL de alrededor de 160 MB. citeturn904173search1

En Linux/macOS, la versión actual espera `yt-dlp` y `ffmpeg` en `PATH`.

## Comandos

```text
/music gui
/music play <URL>
/music pause
/music resume
/music skip
/music stop
/music status
/music volume <0-100>
/music clear
```

## Compilar en Windows

Ejecuta:

```text
build.bat
```

El script comprueba Java 21, usa Gradle si ya está instalado y, si no, descarga automáticamente Gradle 8.10.2 antes de ejecutar `clean build`.

El JAR aparece en:

```text
build/libs/
```

## Requisitos

- Minecraft Java Edition 1.21.1
- Fabric Loader 0.16.5+
- Fabric API compatible con 1.21.1
- Java 21

Fabric documenta las pantallas personalizadas mediante `Screen` y los comandos client-side mediante `ClientCommandRegistrationCallback` para esta línea de Minecraft. citeturn672252search4turn672252search5

## Importante

La reproducción es **local al cliente**: no necesitas instalar este mod en el servidor para escuchar música en tu PC.

Usa las fuentes de audio de acuerdo con sus condiciones de uso y los derechos correspondientes.
