package com.example.inha_capston;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inha_capston.adapter.AudioListAdapter;
import com.example.inha_capston.handling_audio.AnswerSheet;
import com.example.inha_capston.utility_class.LocalFileHandler;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;

public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick {

    private String TAG = "AudioListFragment";

    // for transition
    private NavController navController;

    // media variable
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private AnswerSheet CurrentAnswerSheet;

    // Context
    private Context mContext;
    private Activity mActivity;

    public AudioListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // UI
        RecyclerView audioList_recyclerView = view.findViewById(R.id.audio_list_view);

        // file path setting
        String path = mContext.getFilesDir().getAbsolutePath();
        File directory = new File(path);
        File[] audioFiles = directory.listFiles();

        // adapter and list View setting
        AudioListAdapter audioListAdapter = new AudioListAdapter(audioFiles, this);
        audioList_recyclerView.setHasFixedSize(true);
        audioList_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList_recyclerView.setAdapter(audioListAdapter);
    }

    @Override
    public void onPlayClickListener(File file, int position) {
        // for file data extract
        LocalFileHandler localFileHandler = new LocalFileHandler(mContext, file.getName());
        CurrentAnswerSheet = localFileHandler.loadAnswerSheet();

        // file not found exception
        if(CurrentAnswerSheet == null)
        {
            Toast.makeText(mContext, "해당 파일에 문제가 생겼습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        // send object
        Bundle argument = new Bundle();
        argument.putSerializable("ANSWER_SHEET", CurrentAnswerSheet);

        navController.navigate(R.id.action_audioListFragment_to_playFragment, argument);
    }

    @Override
    public void onItemClickListener(File file, int position) {
        // for file data extract
        LocalFileHandler localFileHandler = new LocalFileHandler(mContext, file.getName());
        AnswerSheet loadedAnswerSheet = localFileHandler.loadAnswerSheet();

        // file not found exception
        if(loadedAnswerSheet == null)
        {
            Toast.makeText(mContext, "해당 파일에 문제가 생겼습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        if(CurrentAnswerSheet == null)
            CurrentAnswerSheet = loadedAnswerSheet;
        else {
            if(!loadedAnswerSheet.getFileName().equals(CurrentAnswerSheet.getFileName())) {
                CurrentAnswerSheet = loadedAnswerSheet;
            }
            else {
                // if same item clicked
                return;
            }
        }

        File file_to_Play = new File(CurrentAnswerSheet.getFilePath());

        // play music preview
        if(isPlaying)
        {
            stopAudio();
            playAudio(file_to_Play);
        }
        else
            playAudio(file_to_Play);
    }

    /**
     * pause audio
     */
    private void pauseAudio()
    {
        // update UIs
        // update status
        isPlaying = false;
        mediaPlayer.pause();
    }

    /**
     * resume audio
     */
    private void resumeAudio()
    {
        // update status
        isPlaying = true;
        mediaPlayer.start();
    }

    /**
     * stop audio
     */
    private void stopAudio()
    {
        // update state
        isPlaying = false;
        mediaPlayer.stop();
    }

    /**
     * play the audio file
     * @param file_to_Play
     */
    private void playAudio(File file_to_Play) {
        // media play
        mediaPlayer = new MediaPlayer();
        try
        {
            mediaPlayer.setDataSource(file_to_Play.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        isPlaying = true;

        // media player callback (completed)
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isPlaying)
            stopAudio();
    }
}
