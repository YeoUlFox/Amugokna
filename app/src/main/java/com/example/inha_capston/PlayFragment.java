package com.example.inha_capston;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.UnicodeSetSpanner;
import android.media.MediaMetadataRetriever;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.writer.WriterProcessor;


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
    private TextView detectionResult_textView;
    private NavController navController;
    private LineChart chart;

    // audio resources
    private AnswerSheet answerSheet;
    private MediaPlayer mediaPlayer;
    private AudioDispatcher audioDispatcher;
    private Thread pitchThread;

    private int SongTime;

    // scoring
    private noteConverter noteConverter;
    private Scoring scoring;
    private ArrayList<Integer> scoring_pitchList;
    private ArrayList<Double> scoring_timeStampList;


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

        chart =  view.findViewById(R.id.paly_frag_chart);
        PlayButton = view.findViewById(R.id.PlayFragment_playBtn);
        detectionResult_textView = view.findViewById(R.id.PlayFragment_test_TextView);

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
                    AudioSetting();
                    makeGraph();
                    playAudio();
                    recordAudio();
                    isPlaying = true;
                }
            }
        });
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


    private void AudioSetting() {
        // AnswerSheet and scoring class init Load
        if(getArguments() != null)
            answerSheet = (AnswerSheet)getArguments().getSerializable("ANSWER_SHEET");

        if(answerSheet == null || !checkPermissions()) {
            navController.popBackStack();
            navController.popBackStack();
            navController.navigate(R.id.action_playFragment_to_audioListFragment);
            return;
        }

        scoring = new Scoring(mContext, answerSheet);

        PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler()
        {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e)
            {
                final float pitchHz = res.getPitch();
                final double timeStamp = e.getTimeStamp();

                if(pitchHz != -1 && res.getProbability() > 0.95)
                {
                    // show realtime result
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(scoring.getGap(noteConverter.getNoteNum(pitchHz), timeStamp) >= 0)
                            {
                                detectionResult_textView.setText("+");
                            }
                            else
                            {
                                detectionResult_textView.setText("-");
                            }
                        }
                    });
                }
            }
        };

        TarsosDSPAudioFormat tarsosDSPAudioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

        // pitch detection
        audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024, 0);
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050,1024, pitchDetectionHandler);
        audioDispatcher.addAudioProcessor(pitchProcessor);

        // record voice
        try {
            String filename = "record_out.wav";
            File file = new File(mContext.getDataDir(), filename);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");

            AudioProcessor writeProcessor = new WriterProcessor(tarsosDSPAudioFormat, randomAccessFile);
            audioDispatcher.addAudioProcessor(writeProcessor);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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

        // media player callback (completed)
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO : show result
                stopRecord();

                final double result = scoring.getResult();

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Log.e(TAG, result + " ");
                        detectionResult_textView.setText(String.valueOf(result));
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

    /**
     * make result array for graph generation
     *
     * @param noteArr answer sheet
     * @param timeArr answer sheet
     * @return result of array for graph
     */
    private ArrayList<Integer> generateResultArr(ArrayList<Integer> noteArr,ArrayList<Double> timeArr){
        ArrayList<Integer> ret_arr = new ArrayList<>();
        double timeNow = 0.00;
        double timeInterval = 0.03;

        for(int i = 0; i < noteArr.size() - 2; i = i + 2)
        {
            while(timeNow < timeArr.get(i + 1)){
                ret_arr.add(noteArr.get(i));
                timeNow += timeInterval;
            }
        }

        return ret_arr;
    }

    private void makeGraph(){
        List<Entry> entries = new ArrayList<>();
        ArrayList<Integer> resultArr=generateResultArr(answerSheet.getPitches(),answerSheet.getTimeStamps());

        // get Song duration
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(answerSheet.getFilePath());
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int SongTime = Integer.parseInt(durationStr);

        for(int i = 0; i < resultArr.size(); i++){
            entries.add(new Entry(i, resultArr.get(i)));
        }

        LineDataSet set = new LineDataSet(entries,"Data Set 1");
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        LineData data = new LineData(set);
        chart.setData(data);
        chart.setVisibleXRangeMaximum(10); //화면에 보이는 원소 개수 설정
        chart.moveViewToAnimated(resultArr.size(), 0, YAxis.AxisDependency.LEFT, SongTime*1000);
    }
}
