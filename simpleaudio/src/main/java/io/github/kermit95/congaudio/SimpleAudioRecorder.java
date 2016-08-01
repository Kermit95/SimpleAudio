package io.github.kermit95.congaudio;


import io.github.kermit95.congaudio.recorder.RecordState;
import io.github.kermit95.congaudio.recorder.RecorderCallback;

/**
 * Created by kermit on 16/7/13.
 *
 */

public interface SimpleAudioRecorder {

    // act
    void record(String targetPath);

    void pause();

    void resume();

    void stop();

    void release();

    // state
    RecordState getState();

    // callback
    void addCallback(RecorderCallback callback);
}
