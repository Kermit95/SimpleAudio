package io.github.kermit95.congaudio.encoder;

/**
 * Created by kermit on 16/7/13.
 *  这两个回调都是在 UI 线程触发, 所以可以试着在开始时显示 Progressbar ,
 *  结束时隐藏它
 */
public interface EncoderCallback {

    void onStartEncode();

    void onFinishEncode();

}
