package io.github.kermit95.simpleaudio;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.github.kermit95.congaudio.CongAudioEncoder;
import io.github.kermit95.congaudio.SimpleAudioPlayer;
import io.github.kermit95.congaudio.SimpleAudioRecorder;
import io.github.kermit95.congaudio.encoder.EncoderCallback;
import io.github.kermit95.congaudio.encoder.MediaCodecEncoder;
import io.github.kermit95.congaudio.player.AudioPCMPlayer;
import io.github.kermit95.congaudio.player.PlayerCallback;
import io.github.kermit95.congaudio.player.PlayerState;
import io.github.kermit95.congaudio.recorder.AudioPCMRecorder;
import io.github.kermit95.congaudio.recorder.RecorderCallback;
import io.github.kermit95.congaudio.util.TimeUtils;


/**
 * Created by kermit on 16/7/9.
 *
 */
public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "AudioRecordActivity";

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 566;

    private String fileDirPath;
    private String filePath;
    private File savedFile;

    // view
    private ListView mListView;
    private RecordAdapter mRecordAdapter;
    private Button mBtnRecord;
    private Button mBtnPause;
    private Button mBtnSave;
    private Button mBtnResume;
    private Button mBtnDeleteAll;
    private TextView mTvRecordTime;
    private AlertDialog mDialog;
    private ProgressDialog mProgressDialog;

    // Audio Worker
    private SimpleAudioPlayer mPlayer;
    private CongAudioEncoder mEncoder;
    private SimpleAudioRecorder mRecorder;

    // data
    private String[] recordFilesName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        mListView = (ListView) findViewById(R.id.list_activity_record);

        mBtnRecord = (Button) findViewById(R.id.btn_activity_record_record);
        mBtnPause = (Button) findViewById(R.id.btn_activity_record_pause);
        mBtnSave = (Button) findViewById(R.id.btn_activity_record_save);
        mBtnResume = (Button) findViewById(R.id.btn_activity_record_resume);
        mBtnDeleteAll = (Button) findViewById(R.id.btn_activity_record_delete_all);
        mTvRecordTime = (TextView) findViewById(R.id.tv_activity_record_time);

        mBtnRecord.setOnClickListener(this);
        mBtnPause.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mBtnResume.setOnClickListener(this);
        mBtnDeleteAll.setOnClickListener(this);

        initButtonState();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(null);

        handlePermission();

        mRecorder = new AudioPCMRecorder();
        mPlayer = new AudioPCMPlayer();
        mEncoder = new MediaCodecEncoder();

        if(Environment.getExternalStorageState().
                equals( Environment.MEDIA_MOUNTED)){
            fileDirPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/TestRecord";
        }
        initLisetView();
        updateDir();
    }

    private void initButtonState(){
        toggleRecordButton(true);
        togglePauseButton(false);
        toggleResumeButton(false);
        toggleSaveButton(false);
        toggleDeleteAll(true);
    }

    /**
     * 更新文件目录, 更新listview的显示
     */
    private void updateDir(){
        // read updated file
        File files = new File(fileDirPath);
        if (!files.exists()){
            if (files.mkdir()){
                recordFilesName = files.list();
            }
        }else {
            recordFilesName = files.list();
        }
        // update listview
        mRecordAdapter.notifyDataSetChanged();
    }


    private void initLisetView() {
        mRecordAdapter = new RecordAdapter();
        mListView.setAdapter(mRecordAdapter);
    }


    private void handlePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("需要录制音频权限");
                builder.setMessage("确认授予音频权限吗?");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{ Manifest.permission.RECORD_AUDIO },
                                    PERMISSION_REQUEST_RECORD_AUDIO);
                        }
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORD_AUDIO: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "granted RECORD_AUDIO permission");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("功能受限");
                    builder.setMessage("由于未能得到权限, 将无法录制音频");
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    private void recordButtonState(){
        toggleRecordButton(false);
        togglePauseButton(true);
        toggleResumeButton(false);
        toggleSaveButton(true);
        toggleDeleteAll(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_activity_record_record:
                final EditText ed_filename = new EditText(this);

                new AlertDialog.Builder(this)
                        .setTitle("输入文件名(可不输入):")
                        .setView(ed_filename)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // get input
                                String text = ed_filename.getText().toString();

                                // set save file name
                                filePath = fileDirPath + File.separator + text +
                                        new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
                                                .format(System .currentTimeMillis()) + ".pcm";

                                savedFile = new File(filePath);

                                // start record
                                mRecorder.addCallback(new RecorderCallback() {
                                    @Override
                                    public void onRecord() {

                                    }

                                    @Override
                                    public void onPause(){

                                    }

                                    @Override
                                    public void onProgress(int sec) {
                                        mTvRecordTime.setText(TimeUtils.convertSecondToMinute(sec));
                                    }

                                    @Override
                                    public void onStop() {
                                        updateDir();
                                    }
                                });

                                mRecorder.record(filePath);

                                // set button
                                recordButtonState();

                            }
                        }).setCancelable(true).show();
                break;
            case R.id.btn_activity_record_pause:
                mRecorder.pause();

                togglePauseButton(false);
                toggleResumeButton(true);
                break;
            case R.id.btn_activity_record_resume:
                mRecorder.resume();
                togglePauseButton(true);
                toggleResumeButton(false);
                break;
            case R.id.btn_activity_record_save:
                if (savedFile != null && savedFile.exists()){
                    mRecorder.stop();
                    new AlertDialog.Builder(this)
                            .setTitle("Save?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (savedFile.delete()){
                                        updateDir();
                                    }
                                }
                            }).show();
                    initButtonState();
                }

                break;
            case R.id.btn_activity_record_delete_all:
                deleteFile(new File(fileDirPath));
                initButtonState();
                updateDir();
                break;
        }
    }

    private void toggleSaveButton(boolean active){
        if (active){
            mBtnSave.setText("停止/保存");
        }else{
            mBtnSave.setText("停止/保存");
        }
        mBtnSave.setEnabled(active);
    }

    private void togglePauseButton(boolean active){
        if (active){
            mBtnPause.setText("暂停");
        }else {
            mBtnPause.setText("暂停中");
        }
        mBtnPause.setEnabled(active);
    }

    private void toggleResumeButton(boolean active){
        if (active){
            mBtnResume.setText("恢复");
            mBtnResume.setEnabled(true);
        }else{
            mBtnResume.setText("恢复");
            mBtnResume.setEnabled(false);
        }
    }

    private void toggleRecordButton(boolean active){
        if (active){
            mBtnRecord.setText("录音");
            mBtnRecord.setEnabled(true);
        }else{
            mBtnRecord.setText("录音中");
            mBtnRecord.setEnabled(false);
        }
    }

    private void toggleDeleteAll(boolean active){
        mBtnDeleteAll.setEnabled(active);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecorder.stop();
        mPlayer.stop();
        mRecorder.release();
        mPlayer.release();
    }


    private class RecordAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        RecordAdapter(){
            mInflater = LayoutInflater.from(AudioRecordActivity.this);
        }

        @Override
        public int getCount() {
            return recordFilesName == null ? 0 : recordFilesName.length;
        }

        @Override
        public Object getItem(int position) {
            return recordFilesName[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;

            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_record, parent, false);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.filename = (TextView) convertView.findViewById(R.id.tv_record_filename);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_record_time);
            viewHolder.mSeekBar = (SeekBar) convertView.findViewById(R.id.seekbar_record_play);
            viewHolder.play = (Button) convertView.findViewById(R.id.btn_record_play);
            viewHolder.stop = (Button) convertView.findViewById(R.id.btn_record_stop);
            viewHolder.encode = (Button) convertView.findViewById(R.id.btn_record_encode);
            viewHolder.pause = (Button) convertView.findViewById(R.id.btn_record_pause);

            // filename
            viewHolder.filename.setText(recordFilesName[position]);

            viewHolder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String targetPath = fileDirPath + File.separator + recordFilesName[position];

                    // 设置 seekbar
                    int fileSize = (int) new File(targetPath).length();
                    viewHolder.mSeekBar.setMax(fileSize);
                    viewHolder.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser){
                                mPlayer.setOffSet(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            if (mPlayer.getState() == PlayerState.Playing){
                                mPlayer.pause();
                            }
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            mPlayer.resume();
                        }
                    });

                    // 添加回调
                    mPlayer.addCallback(new PlayerCallback() {
                        @Override
                        public void onPlay() {
                            Log.i(TAG, "onPlay");
                        }

                        @Override
                        public void onPause() {
                            Log.i(TAG, "onPause");
                        }

                        @Override
                        public void onProgress(int progress) {
                            Log.i(TAG, progress + "");
                            viewHolder.mSeekBar.setProgress(progress);
                        }

                        @Override
                        public void onStop() {
                            Log.i(TAG, "onStop");
                        }
                    });

                    // Play & Resume
                    switch (mPlayer.getState()){
                        case Stoped:
                            mPlayer.play(targetPath);
                        case Paused:
                            mPlayer.resume();
                    }

                }
            });

            viewHolder.pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPlayer.pause();
                }
            });
            viewHolder.stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPlayer.stop();
                }
            });
            viewHolder.encode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String targetPath = fileDirPath + File.separator + recordFilesName[position];

                    mEncoder.addEncoderCallback(new EncoderCallback() {
                        @Override
                        public void onStartEncode() {
                            mProgressDialog.show();
                        }

                        @Override
                        public void onFinishEncode() {
                            mProgressDialog.dismiss();
                            Toast.makeText(AudioRecordActivity.this, "Finish!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mEncoder.encode(targetPath, targetPath.replace(".pcm", ".m4a"));

                }
            });

            return convertView;
        }

        class ViewHolder{
            TextView filename;
            TextView tvTime;
            Button play;
            Button pause;
            Button stop;
            Button encode;
            SeekBar mSeekBar;
        }
    }

    private void deleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                deleteFile(f);
            }
            file.delete();
        }
    }
}
