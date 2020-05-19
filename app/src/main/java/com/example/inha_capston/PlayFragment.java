package com.example.inha_capston;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.UnicodeSetSpanner;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inha_capston.handling_audio.AnswerSheet;
import com.example.inha_capston.handling_audio.Scoring;
import com.example.inha_capston.handling_audio.noteConverter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayFragment extends Fragment
{
    static final String TAG = "playFragment";

    // context
    private Context mContext;       // getContext()
    private Activity mActivity;     // getActivity()

    // UIs
    private ImageView PlayButton;
    private TextView musicName_textView;
    private TextView detectionResult_textView;
    private SeekBar result_seekbar;
    private NavController navController;

    // audio resources
    private AnswerSheet answerSheet;
    private MediaPlayer mediaPlayer;
    private AudioDispatcher audioDispatcher;
    private Thread pitchThread;

    // scoring
    private noteConverter noteConverter;
    private Scoring scoring;

    // flags
    private boolean isPlaying = false;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 3;

    public PlayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        // for handling pitch detection result
        noteConverter = new noteConverter();

        PlayButton = view.findViewById(R.id.PlayFragment_playBtn);
        musicName_textView = view.findViewById(R.id.PlayFragment_music_name_textView);
        detectionResult_textView = view.findViewById(R.id.PlayFragment_test_TextView);
        result_seekbar = view.findViewById(R.id.PlayFragment_ScoreSeekBar);

        PlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying) {
                    // stop all
                    stopAudio();
                    stopRecord();
                    isPlaying = false;
                }
                else {
                    playAudio();
                    recordAudio();
                    isPlaying = true;
                }
            }
        });

        // AnswerSheet and scoring class init Load
        answerSheet = (AnswerSheet)getArguments().getSerializable("ANSWER_SHEET");
        scoring = new Scoring(answerSheet);

        if(answerSheet == null) {
            Toast.makeText(mContext, "파일을 불러오는 도중 문제가 발생하였습니다", Toast.LENGTH_LONG).show();
            navController.navigate(R.id.action_playFragment_to_audioListFragment);
            return;
        }

        AudioSetting();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isPlaying) {
            stopAudio();
            stopRecord();
            isPlaying = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AudioSetting();
    }

    private void AudioSetting() {
        // Record setting
        PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler()
        {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e)
            {
                final float pitchHz = res.getPitch();
                if(pitchHz != -1 && res.getProbability() > 0.95)
                {
                    Log.e(TAG, noteConverter.getNoteName(pitchHz) + " " + e.getTimeStamp());
                    scoring.calScore(noteConverter.getNoteNum(pitchHz), e.getTimeStamp());

                    // show realtime result
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            detectionResult_textView.setText(noteConverter.getNoteName(pitchHz));
                            // result_seekbar.setProgress(); TODO: seekbar handling
                        }
                    });
                }
            }
        };

        if (!checkPermissions()) {
            Toast.makeText(mContext, "마이크 사용을 허가 해주세요", Toast.LENGTH_LONG).show();
            navController.navigate(R.id.action_playFragment_to_audioListFragment);
            return;
        }

        audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024, 0);
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050,1024, pitchDetectionHandler);
        audioDispatcher.addAudioProcessor(pitchProcessor);

        // Play setting
        mediaPlayer = new MediaPlayer();
        try
        {
            mediaPlayer.setDataSource(answerSheet.getFilePath());
            mediaPlayer.prepare();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        // update UIs
        musicName_textView.setText(answerSheet.getMetaTitle());

        // media player callback (completed)
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO : show result
                stopRecord();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        detectionResult_textView.setText(scoring.getResult() + "");
                    }
                });
            }
        });
    }

    /**
     *  make record thread
     *  + make scoring
     */
    private void recordAudio() {
        if(audioDispatcher == null) {
            Toast.makeText(mContext, "오디오 프로세스간의 문제가 발생하였습니다.", Toast.LENGTH_LONG).show();
            return;
        }
        pitchThread = new Thread(audioDispatcher, "game start");
        scoring.setRecordStartTime(SystemClock.elapsedRealtime());
        Log.i(TAG, "recordMusicTime" + SystemClock.elapsedRealtime());
        pitchThread.start();
    }

    /**
     * interrupt thread and make null
     */
    private void stopRecord() {
        if(audioDispatcher != null)
            audioDispatcher.stop();
        audioDispatcher = null;
    }

    /**
     *  play music
     */
    private void playAudio() {
        scoring.setMusicStartTime(SystemClock.elapsedRealtime());
        Log.i(TAG, "startMusicTime" + SystemClock.elapsedRealtime());
        mediaPlayer.start();
    }


    /**
     *  stop record
     */
    private void stopAudio() {
        // erase filename
        detectionResult_textView.setText("");

        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    /**
     * check permissions for mic recording
     * @return true for yes
     */
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        return false;
    }
}
