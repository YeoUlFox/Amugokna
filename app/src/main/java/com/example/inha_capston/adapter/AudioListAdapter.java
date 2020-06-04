package com.example.inha_capston.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inha_capston.R;
import com.example.inha_capston.handling_audio.AnswerSheet;
import com.example.inha_capston.utility_class.LocalFileHandler;
import com.example.inha_capston.utility_class.TimeAgo;

import java.io.File;
import java.util.ArrayList;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder> {

    private ArrayList<File> audioFiles;
    private TimeAgo timeAgo;

    private onItemListClick onItemListClick;

    public AudioListAdapter(ArrayList<File> audioFiles, onItemListClick onItemListClick) {
        this.audioFiles = audioFiles;
        this.onItemListClick = onItemListClick;
    }

    @NonNull
    @Override
    public AudioListAdapter.AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_audio_list, parent, false);
        timeAgo = new TimeAgo();
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioListAdapter.AudioViewHolder holder, int position) {
        // list item initialization
        String filename = audioFiles.get(position).getName().trim();
        if(filename.length() >= 13) {
            filename = filename.substring(0, 13) + "...";
            holder.list_title.setText(filename);
        }
        else
            holder.list_title.setText(filename);

        holder.list_date.setText(timeAgo.getTimeAgo(audioFiles.get(position).lastModified()));
    }

    @Override
    public int getItemCount() { return audioFiles.size(); }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        // single list view component
        private Button list_playBtn;
        private Button list_prePlayBtn;
        private TextView list_title;
        private TextView list_date;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            list_playBtn = itemView.findViewById(R.id.single_list_play_Btn);
            list_prePlayBtn = itemView.findViewById(R.id.single_list_pre_play);

            list_title = itemView.findViewById(R.id.single_list_filename_textView);
            list_date = itemView.findViewById(R.id.single_list_date_textView);

            list_playBtn.setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.single_list_play_Btn:
                    // fragment transition
                    onItemListClick.onPlayClick(audioFiles.get(getAdapterPosition()), getAdapterPosition());
                    break;
                case R.id.single_list_pre_play:
                    // play music
                    onItemListClick.onPrePlayClick(v, audioFiles.get(getAdapterPosition()), getAdapterPosition());
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            onItemListClick.onItemLongClick(audioFiles.get(getAdapterPosition()), getAdapterPosition());
            return true;
        }
    }

    /**
     * interface for making to use parameter file with click
     */
    public interface  onItemListClick {
        void  onPrePlayClick(View v, File file, int position);
        void  onPlayClick(File file, int position);
        void  onItemLongClick(File file, int position);
    }
}
