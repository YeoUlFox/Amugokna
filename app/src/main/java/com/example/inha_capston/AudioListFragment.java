package com.example.inha_capston;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inha_capston.adapter.AudioListAdapter;
import com.example.inha_capston.handling_audio.AnswerSheet;
import com.example.inha_capston.utility_class.LocalFileHandler;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick {

    private String TAG = "AudioListFragment";

    // for transition
    private NavController navController;
    private TextView ListisNull_textView;

    // media variable
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private AnswerSheet CurrentAnswerSheet;

    // Context
    private Context mContext;
    private Activity mActivity;

    // AudioList Adapter
    private AudioListAdapter audioListAdapter;
    private ArrayList<File> audioFiles;
    private File directory;

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
        ListisNull_textView = view.findViewById(R.id.audioListFrag_listIsNull_textView);

        // UI
        RecyclerView audioList_recyclerView = view.findViewById(R.id.audio_list_view);

        // file path setting
        String path = mContext.getFilesDir().getAbsolutePath();
        directory = new File(path);
        audioFiles = new ArrayList<>();

        // load file list
        if(directory.listFiles() != null)
            audioFiles.addAll(Arrays.asList(directory.listFiles()));

        // remove tmp
        for(int i = 0; i < audioFiles.size(); i++) {
            if(audioFiles.get(i).getName().equals("out.wav") || audioFiles.get(i).getName().equals("record_out.wav")) {
                audioFiles.remove(i);
                break;
            }
        }

        // adapter and list View setting
        audioListAdapter = new AudioListAdapter(audioFiles, this);
        audioList_recyclerView.setHasFixedSize(true);
        audioList_recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        audioList_recyclerView.setAdapter(audioListAdapter);

        // show there is no answer Sheet in app
        if(audioFiles.isEmpty())
            ListisNull_textView.setVisibility(View.GONE);
        else
            ListisNull_textView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPlayClick(File file, int position) {
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
    public void onPrePlayClick(View v, File file, int position) {
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
     * interface implementation for rename and delete file
     * @param file
     * @param position
     */
    @Override
    public void onItemLongClick(final File file, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.pick_action)
                .setItems(R.array.action_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Rename
                                renameFile(position);
                                break;
                            case 1:
                                // Delete
                                removeFile(position);
                                break;
                            default:
                                dialog.dismiss();
                                break;
                        }
                    }
                })
                .show();
    }

    /**
     * rename file of view model list
     * @param position index of files
     */
    private void renameFile(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("파일 이름은요?");

        // Set up the input
        final EditText input = new EditText(mContext);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("완료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filename = input.getText().toString();

                if(isValidFileName(filename)) {
                    if(audioFiles.get(position).renameTo(new File(directory, filename)))
                        navController.navigate(R.id.action_audioListFragment_to_recordFragment);
                }
            }
        });

        builder.show();
    }

    /**
     * check length and invalid file char
     * @param str string inputted
     * @return true or false
     */
    private boolean isValidFileName(String str)
    {
        // exceed max length
        if(str.length() > 100)
            return false;

        // check null
        if(TextUtils.isEmpty(str))
            return false;

        // F
        File f = new File(str);
        try {
            // not meaning
            f.getCanonicalPath();
            return true;
        }
        catch (IOException e) {
            // invalid file name
            return false;
        }
    }

    private void removeFile(int position) {
        File removed = audioFiles.get(position);

        if(removed.delete()) {
            audioFiles.remove(position);
            audioListAdapter.notifyItemRemoved(position);
        }
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
