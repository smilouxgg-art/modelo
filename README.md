# 🎵 Music Module

Modulo de musica para un bot de Discord con `discord.js`, `@discordjs/voice`, **yt-dlp** y **FFmpeg**.

## Caracteristicas

- `?play <cancion o URL>` — busca en YouTube o reproduce una URL.
- `?skip` — salta la cancion actual.
- `?pause` / `?resume` — pausa y continua.
- `?queue` — muestra la cola.
- `?nowplaying` — muestra la cancion actual.
- `?stop` — detiene la reproduccion y desconecta.
- `?musichelp` — ayuda de musica.
- Descarga automatica de `yt-dlp` durante `npm install`.
- Detecta FFmpeg instalado en el sistema.

## Instalacion

1. Instala Node.js 18.17 o superior.
2. Instala FFmpeg en el sistema y asegurate de que `ffmpeg` este en el PATH.
3. Ejecuta:

```bash
npm install
```

4. Define el token del bot como variable de entorno `DISCORD_TOKEN`.
5. Ejecuta:

```bash
npm start
```

### FFmpeg

El instalador descarga `yt-dlp` automaticamente. FFmpeg se deja como dependencia del sistema para evitar incluir binarios pesados en el repositorio. Puedes indicar una ruta personalizada con `FFMPEG_PATH`.

## Permisos del bot

El bot necesita permisos para ver canales, enviar mensajes, conectarse a voz y hablar. Tambien necesita el intent **Message Content** activado en el Developer Portal de Discord.

## Uso responsable

Usa el modulo respetando las condiciones de servicio de Discord, YouTube y los derechos de autor del contenido que reproduzcas.
