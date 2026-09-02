const { Client, GatewayIntentBits, EmbedBuilder } = require('discord.js');
const { joinVoiceChannel, createAudioPlayer, createAudioResource, AudioPlayerStatus, VoiceConnectionStatus } = require('@discordjs/voice');
const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

const PREFIX = process.env.PREFIX || '?';
const TOKEN = process.env.DISCORD_TOKEN;
const YTDLP = process.env.YTDLP_PATH || path.join(__dirname, 'bin', process.platform === 'win32' ? 'yt-dlp.exe' : 'yt-dlp');
const FFMPEG = process.env.FFMPEG_PATH || path.join(__dirname, 'bin', process.platform === 'win32' ? 'ffmpeg.exe' : 'ffmpeg');

if (!TOKEN) {
  console.error('Falta DISCORD_TOKEN. Configuralo como variable de entorno.');
  process.exit(1);
}

const client = new Client({ intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildVoiceStates, GatewayIntentBits.GuildMessages, GatewayIntentBits.MessageContent] });
const queues = new Map();

function binaryExists(file) { return fs.existsSync(file); }
function runYtDlp(args) {
  return new Promise((resolve, reject) => {
    const p = spawn(YTDLP, args, { windowsHide: true });
    let out = '', err = '';
    p.stdout.on('data', d => out += d);
    p.stderr.on('data', d => err += d);
    p.on('error', reject);
    p.on('close', code => code === 0 ? resolve(out.trim()) : reject(new Error(err || `yt-dlp termino con codigo ${code}`)));
  });
}

async function resolveTrack(query) {
  const target = /^https?:\/\//i.test(query) ? query : `ytsearch1:${query}`;
  const raw = await runYtDlp(['--dump-single-json', '--no-playlist', '--skip-download', target]);
  const info = JSON.parse(raw);
  return { title: info.title || 'Sin titulo', url: info.webpage_url || query, duration: info.duration || 0, thumbnail: info.thumbnail };
}

function streamTrack(track) {
  const ytdlp = spawn(YTDLP, ['-f', 'bestaudio/best', '-o', '-', '--no-playlist', '--quiet', '--no-warnings', track.url], { windowsHide: true });
  const ffmpeg = spawn(FFMPEG, ['-hide_banner', '-loglevel', 'error', '-i', 'pipe:0', '-f', 's16le', '-ar', '48000', '-ac', '2', 'pipe:1'], { windowsHide: true });
  ytdlp.stdout.pipe(ffmpeg.stdin);
  ytdlp.stderr.on('data', () => {});
  ffmpeg.stderr.on('data', () => {});
  ytdlp.on('error', err => ffmpeg.stdin.destroy(err));
  return { stream: ffmpeg.stdout, processes: [ytdlp, ffmpeg] };
}

async function playNext(guildId) {
  const state = queues.get(guildId);
  if (!state || !state.tracks.length) {
    if (state?.connection) state.connection.destroy();
    queues.delete(guildId);
    return;
  }
  const track = state.tracks.shift();
  try {
    const { stream, processes } = streamTrack(track);
    state.processes = processes;
    const resource = createAudioResource(stream, { inputType: 'raw' });
    state.player.play(resource);
    state.nowPlaying = track;
    state.textChannel.send(`🎵 **Reproduciendo:** ${track.title}`).catch(() => {});
  } catch (e) {
    state.textChannel.send(`❌ No pude reproducir **${track.title}**: ${e.message}`).catch(() => {});
    return playNext(guildId);
  }
}

client.once('ready', () => console.log(`🎵 Musica lista como ${client.user.tag}`));

client.on('messageCreate', async msg => {
  if (msg.author.bot || !msg.guild || !msg.content.startsWith(PREFIX)) return;
  const args = msg.content.slice(PREFIX.length).trim().split(/\s+/);
  const command = (args.shift() || '').toLowerCase();
  const state = queues.get(msg.guild.id);

  try {
    if (command === 'play' || command === 'p') {
      const query = args.join(' ');
      const channel = msg.member?.voice?.channel;
      if (!channel) return msg.reply('❌ Entra primero a un canal de voz.');
      if (!query) return msg.reply(`Usa: \`${PREFIX}play <cancion o URL>\``);
      if (!binaryExists(YTDLP) || !binaryExists(FFMPEG)) return msg.reply('❌ Faltan yt-dlp o FFmpeg. Ejecuta `npm install`.');
      const track = await resolveTrack(query);
      let s = queues.get(msg.guild.id);
      if (!s) {
        const player = createAudioPlayer();
        const connection = joinVoiceChannel({ channelId: channel.id, guildId: msg.guild.id, adapterCreator: msg.guild.voiceAdapterCreator });
        connection.subscribe(player);
        s = { tracks: [], player, connection, textChannel: msg.channel, processes: [], nowPlaying: null };
        queues.set(msg.guild.id, s);
        player.on(AudioPlayerStatus.Idle, () => {
          s.processes.forEach(p => p.kill());
          s.processes = [];
          playNext(msg.guild.id);
        });
        connection.on(VoiceConnectionStatus.Disconnected, () => setTimeout(() => { if (queues.has(msg.guild.id)) connection.destroy(); }, 5000));
      }
      const wasEmpty = s.tracks.length === 0 && !s.nowPlaying;
      s.tracks.push(track);
      await msg.reply(`✅ **${track.title}** ${wasEmpty ? 'empieza ahora.' : 'fue añadida a la cola.'}`);
      if (wasEmpty) playNext(msg.guild.id);
    } else if (command === 'skip' || command === 's') {
      if (!state) return msg.reply('❌ No hay musica reproduciendose.');
      state.player.stop();
    } else if (command === 'stop') {
      if (!state) return msg.reply('❌ No hay musica reproduciendose.');
      state.processes.forEach(p => p.kill());
      state.connection.destroy();
      queues.delete(msg.guild.id);
      await msg.reply('⏹️ Musica detenida y cola vaciada.');
    } else if (command === 'pause') {
      if (!state) return msg.reply('❌ No hay musica.');
      state.player.pause(); await msg.reply('⏸️ Pausado.');
    } else if (command === 'resume') {
      if (!state) return msg.reply('❌ No hay musica.');
      state.player.unpause(); await msg.reply('▶️ Continuando.');
    } else if (command === 'queue' || command === 'q') {
      if (!state || (!state.nowPlaying && !state.tracks.length)) return msg.reply('📭 La cola esta vacia.');
      const list = state.tracks.slice(0, 10).map((t, i) => `${i + 1}. ${t.title}`).join('\n') || 'No hay canciones pendientes.';
      await msg.reply(`🎶 **Cola**\n${state.nowPlaying ? `▶️ ${state.nowPlaying.title}\n\n` : ''}${list}`);
    } else if (command === 'nowplaying' || command === 'np') {
      await msg.reply(state?.nowPlaying ? `🎵 **${state.nowPlaying.title}**` : '📭 No hay nada reproduciendose.');
    } else if (command === 'volume') {
      await msg.reply('ℹ️ El control de volumen se puede añadir usando un transformador PCM/FFmpeg sin cambiar la arquitectura del modulo.');
    } else if (command === 'musichelp') {
      const embed = new EmbedBuilder().setTitle('🎵 Comandos de musica').setDescription([
        `\`${PREFIX}play <cancion/URL>\` — reproduce o añade a la cola`,
        `\`${PREFIX}skip\` — siguiente cancion`,
        `\`${PREFIX}pause\` — pausa`,
        `\`${PREFIX}resume\` — continua`,
        `\`${PREFIX}queue\` — muestra la cola`,
        `\`${PREFIX}nowplaying\` — cancion actual`,
        `\`${PREFIX}stop\` — detiene y desconecta`
      ].join('\n'));
      await msg.reply({ embeds: [embed] });
    }
  } catch (err) {
    console.error(err);
    await msg.reply(`❌ Error: ${err.message}`).catch(() => {});
  }
});

client.login(TOKEN);
