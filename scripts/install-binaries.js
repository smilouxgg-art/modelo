const fs = require('fs');
const path = require('path');
const https = require('https');
const { spawnSync } = require('child_process');

const binDir = path.join(__dirname, '..', 'bin');
fs.mkdirSync(binDir, { recursive: true });

function commandExists(command) {
  const r = spawnSync(command, ['-version'], { stdio: 'ignore', shell: process.platform === 'win32' });
  return r.status === 0;
}

function download(url, destination) {
  return new Promise((resolve, reject) => {
    const file = fs.createWriteStream(destination);
    https.get(url, response => {
      if (response.statusCode >= 300 && response.statusCode < 400 && response.headers.location) {
        file.close(); fs.unlinkSync(destination);
        return download(response.headers.location, destination).then(resolve, reject);
      }
      if (response.statusCode !== 200) {
        file.close(); fs.unlinkSync(destination);
        return reject(new Error(`HTTP ${response.statusCode}`));
      }
      response.pipe(file);
      file.on('finish', () => file.close(resolve));
    }).on('error', err => { file.close(); try { fs.unlinkSync(destination); } catch {} reject(err); });
  });
}

async function main() {
  const isWin = process.platform === 'win32';
  const ytdlpName = isWin ? 'yt-dlp.exe' : 'yt-dlp';
  const ytdlpPath = path.join(binDir, ytdlpName);
  const localYtdlp = fs.existsSync(ytdlpPath);

  if (!localYtdlp) {
    const url = isWin
      ? 'https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe'
      : 'https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp';
    console.log('Descargando yt-dlp...');
    await download(url, ytdlpPath);
    if (!isWin) fs.chmodSync(ytdlpPath, 0o755);
  }

  if (!commandExists('ffmpeg') && !fs.existsSync(path.join(binDir, isWin ? 'ffmpeg.exe' : 'ffmpeg'))) {
    console.warn('FFmpeg no esta instalado localmente. Para mantener el paquete ligero, instalalo con el gestor del sistema (winget/choco/apt) o define FFMPEG_PATH.');
    console.warn('Ejemplo Windows: winget install Gyan.FFmpeg');
    console.warn('Ejemplo Debian/Ubuntu: sudo apt install ffmpeg');
  } else {
    console.log('FFmpeg detectado.');
  }
  console.log('Instalacion de binarios terminada.');
}

main().catch(err => { console.error('No se pudo instalar yt-dlp:', err.message); process.exitCode = 1; });
