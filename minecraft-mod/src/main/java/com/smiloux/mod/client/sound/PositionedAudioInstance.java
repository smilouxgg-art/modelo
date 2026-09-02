package com.smiloux.mod.client.sound;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class PositionedAudioInstance extends AbstractSoundInstance {
    public PositionedAudioInstance(double x, double y, double z) {
        super(Identifier.of("smiloux", "external_audio"), SoundCategory.RECORDS, Random.create());
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.attenuationType = SoundInstance.AttenuationType.LINEAR;
        this.repeat = false;
    }
}
