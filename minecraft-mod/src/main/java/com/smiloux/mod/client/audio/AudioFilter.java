package com.smiloux.mod.client.audio;

public enum AudioFilter {
    NONE("Normal", "anull"),
    BASS_BOOST("Bass Boost", "equalizer=f=100:width_type=h:width=200:g=10"),
    NIGHTCORE("Nightcore", "asetrate=44100*1.25,aresample=44100"),
    EIGHT_D("8D Audio", "apulsator=mode=sine:hz=0.2"),
    VAPORWAVE("Vaporwave", "asetrate=44100*0.85,aresample=44100,aecho=0.8:0.88:60:0.4");

    private final String displayName;
    private final String ffmpegFilter;

    AudioFilter(String displayName, String ffmpegFilter) {
        this.displayName = displayName;
        this.ffmpegFilter = ffmpegFilter;
    }

    public String displayName() { return displayName; }
    public String ffmpegFilter() { return ffmpegFilter; }
}
