package com.smiloux.mod.client.model;

import com.smiloux.mod.client.audio.AudioProcessManager;

import java.util.ArrayList;
import java.util.List;

public final class Playlist {
    private String name;
    private List<AudioProcessManager.TrackInfo> tracks = new ArrayList<>();

    public Playlist() {}
    public Playlist(String name) { this.name = name; }
    public String getName() { return name == null ? "Mi playlist" : name; }
    public List<AudioProcessManager.TrackInfo> getTracks() { return tracks; }
    public void setName(String name) { this.name = name; }
}
