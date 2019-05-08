package com.manhattan.blueprint.Utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.provider.MediaStore;

import com.manhattan.blueprint.Controller.ARActivity;
import com.manhattan.blueprint.Model.DAO.Consumer;

import java.util.Timer;
import java.util.TimerTask;

public class MediaUtils {
    private int fadeInDuration = 3000;
    private int fadeOutDuration = 1500;
    private int interval = 250;
    private float currentVolume = 0;
    private MediaPlayer player;
    private Timer timer;

    public MediaUtils(MediaPlayer player){
        this.player = player;
        this.timer = new Timer(true);
    }

    public void fadeOut(Consumer<Void> onCompletion) {
        // Cancel any previous timers
        timer.cancel();
        timer = new Timer();
        float numberOfSteps = fadeOutDuration / interval;
        float deltaVolume = 1 / numberOfSteps;

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                player.setVolume(currentVolume, currentVolume);
                currentVolume -= deltaVolume;
                if(currentVolume <= 0f){
                    timer.cancel();
                    timer.purge();
                    onCompletion.consume(null);
                }
            }
        };

        timer.schedule(timerTask, interval, interval);
    }

    public void fadeIn() {
        // Cancel any previous timers
        timer.cancel();
        timer = new Timer();
        float numberOfSteps = fadeInDuration / interval;
        float deltaVolume = 1 / numberOfSteps;

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                player.setVolume(currentVolume, currentVolume);
                currentVolume += deltaVolume;
                if(currentVolume >= 1f){
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(timerTask, interval, interval);
    }

    public static void playSoundEffect(int soundEffect, MediaPlayer mp, Context ctx) {
        if (mp != null) {
            mp.stop();
            mp.release();
        }
        mp = MediaPlayer.create(ctx, soundEffect);
        if (mp != null) {
            mp.start();
        }
    }
}
