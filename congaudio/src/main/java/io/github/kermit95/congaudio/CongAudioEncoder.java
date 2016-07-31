package io.github.kermit95.congaudio;


import io.github.kermit95.congaudio.encoder.EncoderCallback;

/**
 * Created by kermit on 16/7/13.
 */

public interface CongAudioEncoder {

    void addEncoderCallback(EncoderCallback callback);

    void encode(String inputPath, String outputPath);

}
