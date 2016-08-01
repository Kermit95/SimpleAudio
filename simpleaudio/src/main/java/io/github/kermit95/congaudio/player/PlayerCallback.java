package io.github.kermit95.congaudio.player;

/**
 * Created by kermit on 16/7/16.
 * 所有回调都在 UI thread 触发
 */
public interface PlayerCallback {

    void onPlay();

    void onProgress(int progress);

    void onPause();

    void onStop();
}
