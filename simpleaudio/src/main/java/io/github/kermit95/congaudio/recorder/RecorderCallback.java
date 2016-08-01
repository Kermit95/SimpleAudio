package io.github.kermit95.congaudio.recorder;

/**
 * Created by kermit on 16/7/14.
 * 在 UI thread 触发
 */
public interface RecorderCallback {

    void onRecord();

    void onProgress(int sec);

    void onPause();

    void onStop();
}
