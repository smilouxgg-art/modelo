package com.smiloux.mod.client.discord;

import com.smiloux.mod.client.audio.AudioProcessManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public final class SmilouxDiscordRPC {
    private static final String CLIENT_ID = System.getProperty("smiloux.discord.clientId", "");
    private static RandomAccessFile ipc;
    private static long startTime = Instant.now().getEpochSecond();
    private static String lastTrack = "";

    private SmilouxDiscordRPC() {}

    public static void initialize() { if (!CLIENT_ID.isBlank()) connect(); }

    public static void tick(AudioProcessManager.TrackInfo track) {
        if (CLIENT_ID.isBlank()) return;
        if (track == null) { lastTrack = ""; return; }
        if (!track.title().equals(lastTrack)) {
            lastTrack = track.title();
            startTime = Instant.now().getEpochSecond();
            update(track);
        }
    }

    private static void connect() {
        for (int i = 0; i < 10; i++) {
            try {
                String path = switch (System.getProperty("os.name").toLowerCase()) {
                    case String s when s.contains("win") -> "\\\\.\\pipe\\discord-ipc-" + i;
                    default -> "/tmp/discord-ipc-" + i;
                };
                ipc = new RandomAccessFile(path, "rw");
                write(0, "{\"v\":1,\"client_id\":\"" + escape(CLIENT_ID) + "\"}");
                return;
            } catch (Exception ignored) { ipc = null; }
        }
    }

    private static void update(AudioProcessManager.TrackInfo track) {
        try {
            if (ipc == null) connect();
            if (ipc == null) return;
            String payload = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + ProcessHandle.current().pid() + ",\"activity\":{\"details\":\"" + escape(track.title()) + "\",\"state\":\"" + escape(track.artist()) + "\",\"timestamps\":{\"start\":" + startTime + "}}},\"nonce\":\"smiloux\"}";
            write(1, payload);
        } catch (Exception ignored) {}
    }

    private static void write(int opcode, String json) throws Exception {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        DataOutputStream out = new DataOutputStream(new java.io.OutputStream() {
            @Override public void write(int b) throws java.io.IOException { ipc.write(b); }
            @Override public void write(byte[] b, int off, int len) throws java.io.IOException { ipc.write(b, off, len); }
        });
        writeIntLE(out, opcode);
        writeIntLE(out, bytes.length);
        out.write(bytes);
        out.flush();
    }

    private static void writeIntLE(DataOutputStream out, int v) throws Exception {
        out.write(v & 255); out.write((v >>> 8) & 255); out.write((v >>> 16) & 255); out.write((v >>> 24) & 255);
    }

    private static String escape(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " "); }
}
