# Music Mod — Minecraft 1.21.1

Mod de música para **Minecraft 1.21.1 + Fabric**.

## Comandos

```text
/music play <URL>
/music pause
/music resume
/music stop
/music status
```

La reproducción es local al cliente. El mod usa **yt-dlp** para obtener el audio y **FFmpeg** para convertirlo a PCM para el motor de audio.

## Instalación automática

Al ejecutar `/music play`, el mod crea:

```text
~/.musicmod/bin/
```

y descarga `yt-dlp` automáticamente. En Windows x64 también descarga automáticamente un paquete de FFmpeg y extrae `ffmpeg.exe`.

## Compilar

Requiere **Java 21**. Fabric recomienda Java 21 o superior para las versiones modernas de Minecraft. citehttps://docs.fabricmc.net/develop/getting-started/setting-up

```bash
gradle build
```

El JAR queda en `build/libs/`.

## Nota

Usa únicamente URLs y contenido que tengas derecho a reproducir o descargar.
