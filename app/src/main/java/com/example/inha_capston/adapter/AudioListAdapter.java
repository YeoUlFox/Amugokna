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
        holder.list_title.setText(audioFiles.get(position).getName());
        holder.list_date.setText(timeAgo.getTimeAgo(audioFiles.get(position).lastModified()));
    }

    @Override
    public int getItemCount() { return audioFiles.size(); }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        // single list view component
        private ImageView list_image;
        private TextView list_title;
        private TextView list_date;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            list_image = itemView.findViewById(R.id.single_list_play_Btn);
            list_title = itemView.findViewById(R.id.single_list_filename_textView);
            list_date = itemView.findViewById(R.id.single_list_date_textView);

            list_image.setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView)
                // fragment transition
                onItemListClick.onPlayClick(audioFiles.get(getAdapterPosition()), getAdapterPosition());
            else
                // play music
                onItemListClick.onItemClick(v, audioFiles.get(getAdapterPosition()), getAdapterPosition());
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
        void  onItemClick(View v, File file, int position);
        void  onPlayClick(File file, int position);
        void  onItemLongClick(File file, int position);
    }
}
