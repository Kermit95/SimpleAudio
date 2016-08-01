package io.github.kermit95.congaudio;

import io.github.kermit95.congaudio.player.PlayerCallback;
import io.github.kermit95.congaudio.player.PlayerState;

/**
 * Created by kermit on 16/7/13.
 */

public interface SimpleAudioPlayer {

    // play
    void play(String tagetPath);

    void pause();

    void resume();

    void stop();

    void release();

    // state
    PlayerState getState();

    // interactive
    void setOffSet(int offSet);

    // callback
    void addCallback(PlayerCallback callback);

}
