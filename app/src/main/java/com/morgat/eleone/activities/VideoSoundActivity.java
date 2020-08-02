package com.morgat.eleone.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.morgat.eleone.models.HomeModel;
import com.morgat.eleone.R;
import com.morgat.eleone.utils.Functions;
import com.morgat.eleone.utils.Variables;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;


public class VideoSoundActivity extends AppCompatActivity implements View.OnClickListener {

    HomeModel item;
    TextView sound_name, description_txt;
    ImageView sound_image;

    File videoFile, audioFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_sound);

        Intent intent = getIntent();
        if (intent.hasExtra("data")) {
            item = (HomeModel) intent.getSerializableExtra("data");
        }

        // video_file=new File(Variables.app_folder+item.video_id+".mp4");
        videoFile = new File(Variables.getRootPath(this) + item.video_id + ".mp4");

        sound_name = findViewById(R.id.sound_name);
        description_txt = findViewById(R.id.description_txt);
        sound_image = findViewById(R.id.sound_image);

        if ((item.sound_name == null || item.sound_name.equals("") || item.sound_name.equals("null"))) {
            sound_name.setText("original sound - " + item.first_name + " " + item.last_name);
        } else {
            sound_name.setText(item.sound_name);
        }
        description_txt.setText(item.video_description);


        findViewById(R.id.backButton).setOnClickListener(this);

        findViewById(R.id.save_btn).setOnClickListener(this);
        findViewById(R.id.create_btn).setOnClickListener(this);

        findViewById(R.id.play_btn).setOnClickListener(this);
        findViewById(R.id.pause_btn).setOnClickListener(this);

        if (videoFile.exists()) {

            Glide.with(this)
                    .load(Uri.fromFile(videoFile))
                    .into(sound_image);


            loadFFmpeg();

        } else {
            saveVideo();
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.backButton:
                finish();
                break;
            case R.id.save_btn:
                try {
                    copyFile(new File(Variables.getRootPath(this) + Variables.SelectedAudio_MP3),
                            new File(Variables.getRootPath(this) + item.video_id + ".mp3"));
                    Toast.makeText(this, "Audio Saved", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            case R.id.create_btn:
                convertMp3ToAcc();
                break;

            case R.id.play_btn:
                if (audioFile.exists())
                    playAudio();
                else if (videoFile.exists())
                    loadFFmpeg();
                else
                    saveVideo();

                break;

            case R.id.pause_btn:
                StopPlaying();
                break;
        }
    }


    SimpleExoPlayer player;

    public void playAudio() {

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "TikTok"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(audioFile));


        player.prepare(videoSource);
        player.setPlayWhenReady(true);

        Show_playing_state();
    }


    public void StopPlaying() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        Show_pause_state();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StopPlaying();
    }

    public void Show_playing_state() {
        findViewById(R.id.play_btn).setVisibility(View.GONE);
        findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
    }

    public void Show_pause_state() {
        findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.pause_btn).setVisibility(View.GONE);
    }


    public void saveVideo() {
        Functions.showDeterminentLoader(this, false, false);
        PRDownloader.initialize(this);
        DownloadRequest prDownloader = PRDownloader.download(item.video_url, Variables.getRootPath(this), item.video_id + ".mp4")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                        int prog = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                        Functions.showLoadingProgress(prog);

                    }
                });


        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                Functions.cancel_determinent_loader();
                audioFile = new File(Variables.getRootPath(VideoSoundActivity.this) + item.video_id + ".mp4");
                Glide.with(VideoSoundActivity.this)
                        .load(Uri.fromFile(videoFile))
                        .into(sound_image);
                loadFFmpeg();
            }

            @Override
            public void onError(Error error) {

                Functions.cancel_determinent_loader();
            }


        });


    }

    FFmpeg ffmpeg;

    public void loadFFmpeg() {


        showAudioLoading();

        ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                    hideAudioLoading();
                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFinish() {
                    extractSound();

                }
            });
        } catch (FFmpegNotSupportedException e) {
            showAudioLoading();
            Toast.makeText(this, "" + e.toString(), Toast.LENGTH_SHORT).show();
        }


    }

    public void extractSound() {

        String[] complexCommand = {"-y", "-i", Variables.getRootPath(this) + item.video_id + ".mp4", "-vn", "-ar", "44100", "-ac", "2", "-b:a", "256k", "-f", "mp3",
                Variables.getRootPath(this) + Variables.SelectedAudio_MP3};
        try {
            ffmpeg.execute(complexCommand, new FFmpegExecuteResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(String message) {

                    Log.d(Variables.tag, message);

                }

                @Override
                public void onFailure(String message) {
                    showAudioLoading();
                    Log.d(Variables.tag, "onFailure " + message);
                }

                @Override
                public void onSuccess(String message) {
                }

                @Override
                public void onFinish() {
                    hideAudioLoading();
                    audioFile = new File(Variables.getRootPath(VideoSoundActivity.this) + Variables.SelectedAudio_MP3);
                    if (audioFile.exists())
                        playAudio();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            hideAudioLoading();
        }
    }


    public void showAudioLoading() {
        findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
        findViewById(R.id.play_btn).setVisibility(View.GONE);
        findViewById(R.id.pause_btn).setVisibility(View.GONE);
    }

    public void hideAudioLoading() {
        findViewById(R.id.loading_progress).setVisibility(View.GONE);
        findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.pause_btn).setVisibility(View.GONE);
    }


    public void convertMp3ToAcc() {
        Functions.showLoader(this, false, false);
        AndroidAudioConverter.load(VideoSoundActivity.this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                File flacFile = new File(Variables.getRootPath(VideoSoundActivity.this), Variables.SelectedAudio_MP3);
                if(!flacFile.exists()) {
                    try {

                       flacFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                IConvertCallback callback = new IConvertCallback() {
                    @Override
                    public void onSuccess(File convertedFile) {
                        Functions.cancelLoader();
                        openVideoRecording();
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Functions.cancelLoader();
                        Toast.makeText(VideoSoundActivity.this, "" + error, Toast.LENGTH_SHORT).show();
                    }
                };
                AndroidAudioConverter.with(VideoSoundActivity.this)
                        .setFile(flacFile)
                        .setFormat(AudioFormat.AAC)
                        .setCallback(callback)
                        .convert();
            }

            @Override
            public void onFailure(Exception error) {
                Functions.cancelLoader();
            }
        });


    }


    public void openVideoRecording() {
        Intent intent = new Intent(VideoSoundActivity.this, VideoRecordingActivity.class);
        intent.putExtra("sound_name", sound_name.getText().toString());
        intent.putExtra("sound_id", item.sound_id);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);

    }


    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

}
