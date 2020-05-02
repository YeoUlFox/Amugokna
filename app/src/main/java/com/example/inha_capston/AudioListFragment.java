package com.example.inha_capston;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.inha_capston.adapter.AudioListAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;

public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick {

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView audioList_recyclerView;
    private AudioListAdapter audioListAdapter;

    private File[] audioFiles;

    // media variable
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private File file_to_Play;

    // UI from Bottom Sheet
    private ImageButton player_playBtn;
    private TextView playerHeader_textView;
    private TextView playerFilename_textView;
    // seek bar handling
    private SeekBar player_seekbar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;

    public AudioListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // UI
        audioList_recyclerView = view.findViewById(R.id.audio_list_view);
        playerSheet = view.findViewById(R.id.player_sheet_Layout);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);

        // UI from bottom playerSheet
        player_playBtn = view.findViewById(R.id.player_play_imageView);
        playerHeader_textView = view.findViewById(R.id.player_header_status_textView);
        playerFilename_textView = view.findViewById(R.id.player_filename_textView);
        player_seekbar = view.findViewById(R.id.player_seekbar);

        player_playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying)
                    pauseAudio();
                else
                {
                    if(file_to_Play != null)
                    {
                        // TODO : check music is over and restart music
                        resumeAudio();
                    }

                }
            }
        });

        player_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // nothing to do
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(file_to_Play != null)
                    pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(file_to_Play != null) {
                    int progress = seekBar.getProgress();
                    mediaPlayer.seekTo(progress);
                    resumeAudio();
                }
            }
        });

        // file path setting
        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        audioFiles = directory.listFiles();

        // adapter and list View setting
        audioListAdapter = new AudioListAdapter(audioFiles, this);
        audioList_recyclerView.setHasFixedSize(true);
        audioList_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList_recyclerView.setAdapter(audioListAdapter);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
        {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN) {
                    // prevent BottomSheet hidden
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // nothing to do in this callback function
            }
        });
    }

    @Override
    public void onClickListener(File file, int position) {
        file_to_Play = file;

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
        player_playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
        // update status
        isPlaying = false;
        seekbarHandler.removeCallbacks(updateSeekbar);

        mediaPlayer.pause();
    }

    /**
     * resume audio
     */
    private void resumeAudio()
    {
        // update UIs
        player_playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_pause_black_24dp));
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 500);

        // update status
        isPlaying = true;

        mediaPlayer.start();
    }

    /**
     * stop audio
     */
    private void stopAudio()
    {
        // update UIs
        // TODO : null object error
        player_playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
        playerHeader_textView.setText("Stopping");

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
        // update UIs
        player_playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_pause_black_24dp));
        playerFilename_textView.setText(file_to_Play.getName());
        playerHeader_textView.setText("Playing");

        // update state
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        isPlaying = true;

        // media player callback (completed)
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
                playerHeader_textView.setText("finished");
            }
        });

        // Seek bar sync
        player_seekbar.setMax(mediaPlayer.getDuration());
        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 500);
    }

    /**
     * make thread to post runnable object
     */
    private void updateRunnable()
    {
        updateSeekbar = new Runnable() {
            @Override
            public void run()
            {
                player_seekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 500);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isPlaying)
            stopAudio();
    }
}
