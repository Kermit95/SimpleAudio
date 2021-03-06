package io.github.kermit95.congaudio.recorder;

import io.github.kermit95.congaudio.SimpleAudioRecorder;

/**
 * Created by kermit on 16/7/17.
 *
 */

abstract class AbsRecorder implements SimpleAudioRecorder {

    RecordState mState;
    RecorderCallback mCallback;


    AbsRecorder() {
        this.mState = RecordState.Stoped;
    }

    @Override
    public void record(String targetPath) {
        if (mState == RecordState.Stoped){
            this.mState = RecordState.Recording;
            onAudioRecord(targetPath);
        }
    }

    @Override
    public void pause() {
        if (mState == RecordState.Recording){
            this.mState = RecordState.Paused;
            onAudioPause();
        }
    }

    @Override
    public void resume() {
        if (mState == RecordState.Paused){
            this.mState = RecordState.Recording;
            onAudioResume();
        }
    }

    @Override
    public void stop() {
        if (mState == RecordState.Paused || mState == RecordState.Recording){
            this.mState = RecordState.Stoped;
            onAudioStop();
        }
    }

    abstract void onAudioRecord(String targetPath);
    void onAudioPause(){}
    void onAudioResume(){}
    void onAudioStop(){}

    @Override
    public RecordState getState() {
        return mState;
    }

    @Override
    public void addCallback(RecorderCallback callback) {
        this.mCallback = callback;
    }

}
