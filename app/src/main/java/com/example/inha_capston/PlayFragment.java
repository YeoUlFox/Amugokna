package com.example.inha_capston;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.inha_capston.handling_audio.AnswerSheet;
import com.example.inha_capston.handling_audio.noteConverter;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;

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
    // context
    private Context mContext;       // getContext()
    private Activity mActivity;     // getActivity()
    private AnswerSheet answerSheet;
    private AudioDispatcher audioDispatcher;
    private noteConverter noteConverter;

    ImageView imageButton;
    TextView textView;
    StringBuilder test_tmp;

    private int i;

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

        answerSheet = new AnswerSheet();

        imageButton = view.findViewById(R.id.imageView);
        textView = view.findViewById(R.id.playFlag_test_TextView);
        test_tmp = new StringBuilder();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
    }


    private void test()
    {
        i = 0;

        TarsosDSPAudioFormat tarsosDSPAudioFormat  = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

        PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler()
        {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e)
            {
                final float pitchHz = res.getPitch();

                if(pitchHz != -1 && res.getProbability() > 0.99)
                {

                    answerSheet.addPitch(pitchHz);
                    answerSheet.addTimes(e.getTimeStamp());

                    test_tmp.append("< ").append(" : ").append(answerSheet.getPitches().get(i)).append(", ").append(answerSheet.getTimeStamps().get(i)).append(" >").append("\n");

                    mActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run() {
                            textView.setText(test_tmp.toString());
                        }
                    });

                    i++;
                }
            }
        };

        audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024, 0);

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050,1024, pitchDetectionHandler);
        audioDispatcher.addAudioProcessor(pitchProcessor);

        Thread pitchThread = new Thread(audioDispatcher, "Pitch Detection Thread");
        pitchThread.start();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }
}
