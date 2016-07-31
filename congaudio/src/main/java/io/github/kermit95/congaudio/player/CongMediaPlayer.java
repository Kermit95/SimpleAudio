package io.github.kermit95.congaudio.player;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by kermit on 16/7/17.
 *
 * TODO: 用来播放 M4A 文件
 */

public class CongMediaPlayer extends AbsPlayer {

    private MediaPlayer mMediaPlayer;

    public CongMediaPlayer() {
        mState = PlayerState.Stoped;
        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public void play(String tagetPath) {
        if (mState == PlayerState.Stoped){
            mMediaPlayer.reset();

            try {
                mMediaPlayer.setDataSource(tagetPath);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    void onAudioPlay(String targetPath) {

    }

    public int getDuration(){
        return mMediaPlayer.getDuration();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }

    @Override
    public void addCallback(PlayerCallback callback) {

    }
}
