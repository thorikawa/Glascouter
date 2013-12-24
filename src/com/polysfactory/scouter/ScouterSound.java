package com.polysfactory.scouter;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class ScouterSound {
    private SoundPool sp;
    private int processingSound;
    private int beapSound;

    public ScouterSound(Context context) {
        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        processingSound = sp.load(context, R.raw.processing2, 1);
        beapSound = sp.load(context, R.raw.beap, 1);
    }

    public void processing(boolean loop) {
        sp.play(processingSound, 1, 1, 0, loop ? -1 : 0, 1);
    }

    public void beap() {
        sp.play(beapSound, 1, 1, 0, 0, 1);
    }
}
