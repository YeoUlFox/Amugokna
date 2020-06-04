package com.example.inha_capston;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.inha_capston.handling_audio.AnswerSheet;
import com.example.inha_capston.handling_audio.AnswerSheetMaker;
import com.example.inha_capston.utility_class.LocalFileHandler;
import com.example.inha_capston.utility_class.network.SocketClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener {

    // permission variable
    static final String TAG = "MainFragment";

    // context
    private Context mContext;       // getContext()
    private Activity mActivity;     // getActivity()

    // fragment transition
    private NavController navController;

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        Button loadBtn = view.findViewById(R.id.load_btn);
        Button recordBtn = view.findViewById(R.id.record_btn);
        Button OptionsBtn = view.findViewById(R.id.setting_btn);

        recordBtn.setOnClickListener(this);
        loadBtn.setOnClickListener(this);
        OptionsBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_btn:
                navController.navigate(R.id.action_recordFragment_to_waitFragment);
                break;
            case R.id.record_btn:
                // transition fragment with anim, show list of audio record files
                navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                break;
            case R.id.setting_btn:
                navController.navigate(R.id.action_recordFragment_to_optionFragment);
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
