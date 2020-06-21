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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inha_capston.handling_audio.AnswerSheet;
import com.example.inha_capston.handling_audio.AnswerSheetMaker;
import com.example.inha_capston.utility_class.ComFire;
import com.example.inha_capston.utility_class.LocalFileHandler;

import com.example.inha_capston.utility_class.SharedPreferencesManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import io.grpc.internal.SharedResourceHolder;


/**
 * before network connection
 * A simple {@link Fragment} subclass.
 */
public class WaitFragment extends Fragment
{
    static final String TAG = "WaitFragment";

    private NavController navController;

    private Context mContext;
    private Activity mActivity;

    // UI
    private RadioGroup radioGroup;
    private RadioButton[] radioBtn;
    private ProgressBar progressBar;
    private TextView radioGroup_label_textview;
    private TextView shiftAmount_label_textview;
    private FloatingActionButton floatingActionButton;
    private EditText shiftAmount_EditText;

    private int[] radioBtnId = new int[]{R.id.wait_frag_genre_pop, R.id.wait_frag_genre_rock, R.id.wait_frag_genre_edm,
            R.id.wait_frag_genre_hiphop, R.id.wait_frag_genre_ballad};

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static final int REQUEST_AUDIO_MP3 = 1;             // for mp3 request Intent

    // for
    private AnswerSheet answerSheet;
    private boolean isFileLoaded = false;
    private String filepath_to_send;  // vocal path
    private String filepath_to_play;  // play path

    // for network
    private Intent fromMusicData;
    private ComFire cf;
    private int shiftAmount;


    public WaitFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wait, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        progressBar = view.findViewById(R.id.wait_frag_progressBar);
        floatingActionButton = view.findViewById(R.id.wait_frag_floatingActionButton);

        radioGroup_label_textview = view.findViewById(R.id.wait_frag_genre_label_textView);
        radioGroup = view.findViewById(R.id.wait_genre_selection_radioGroup);
        radioBtn = new RadioButton[5];
        for(int i = 0; i < 5; i ++) radioBtn[i] = view.findViewById(radioBtnId[i]);

        shiftAmount_label_textview = view.findViewById(R.id.wait_frag_shiftAmount_label_textView);
        shiftAmount_EditText = view.findViewById(R.id.wait_frag_shiftAmount_editText);

        setViewVisible_onFile(false);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewVisible_onFile(false);

                // set Option bit
                switch(radioGroup.getCheckedRadioButtonId()) {
                    case R.id.wait_frag_genre_pop:
                        SharedPreferencesManager.setGenreOptionValue(mContext, 0);
                        break;
                    case R.id.wait_frag_genre_rock:
                        SharedPreferencesManager.setGenreOptionValue(mContext, 1);
                        break;
                    case R.id.wait_frag_genre_edm:
                        SharedPreferencesManager.setGenreOptionValue(mContext, 2);
                        break;
                    case R.id.wait_frag_genre_hiphop:
                        SharedPreferencesManager.setGenreOptionValue(mContext, 3);
                        break;
                    default: // wait_frag_genre_ballad
                        SharedPreferencesManager.setGenreOptionValue(mContext, 4);
                        break;
                }

                String editText_str = shiftAmount_EditText.getText().toString();
                // check shiftAmount null
                if(editText_str.matches("")) {
                    Toast.makeText(mContext, "음정값을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                shiftAmount = Integer.parseInt(editText_str);

                try {
                    if(!cf.includesForUploadFiles(fromMusicData, shiftAmount)) {
                        Log.e(TAG, "first server file load failed");
                        Toast.makeText(mContext, "서버와의 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_waitFragment_to_recordFragment);
                        return;
                    }
                    Thread.sleep(120000);        // colab을 다녀오기위한 20초

                    ArrayList<String> ret = cf.includesForDownloadFiles(fromMusicData);
                    Thread.sleep(3000);         // 파일 write를 위한 3초

                    if(ret.size() == 2) {
                        filepath_to_send = ret.get(0);
                        filepath_to_play = ret.get(1);
                    }
                    else {
                        Log.e(TAG, "second server file download failed");
                        Toast.makeText(mContext, "서버 파일 다운로드 실패", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_waitFragment_to_recordFragment);
                        return;
                    }

                    Log.i(TAG, "file path : " + filepath_to_send);
                    Log.i(TAG, "file path : " + filepath_to_play);
                }
                catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                AnswerSheetMaker answerSheetMaker = new AnswerSheetMaker(mContext, filepath_to_send);
                answerSheet = answerSheetMaker.makeAnswerSheet();
                answerSheet.setFileName(filepath_to_play);

                if(answerSheet == null) {
                    Toast.makeText(mContext, "파일을 불러오는 동안 문제가 발생하였습니다", Toast.LENGTH_LONG).show();
                    navController.navigate(R.id.action_waitFragment_to_recordFragment);
                    Log.e(TAG, "make answer sheet return null");
                    return;
                }

                inputFilename_saveLocal(answerSheet);
                navController.navigate(R.id.action_waitFragment_to_audioListFragment);
            }
        });

        getMusicFileFromDevice();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }

    /**
     * call local Intent and get audio/* file
     */
    private void getMusicFileFromDevice()
    {
        if(checkPermissionREAD_EXTERNAL_STORAGE(mContext))
        {
            // load .mp3 file from device
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            startActivityForResult(Intent.createChooser(intent, "불러올 음악 파일을 선택해주세요"), REQUEST_AUDIO_MP3);
        }
        else
            Toast.makeText(mContext, "파일을 불러오는 동안 문제가 발생하였습니다 : 권한 문제", Toast.LENGTH_LONG).show();
    }

    /**
     * handling loaded mp3 file (Callback function)
     * @param requestCode REQUEST_AUDIO_MP3
     * @param resultCode RESULT_OK
     * @param data uri
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || resultCode != Activity.RESULT_OK || requestCode != REQUEST_AUDIO_MP3) {
            navController.navigate(R.id.action_waitFragment_to_recordFragment);
            Toast.makeText(mContext, "로컬 파일을 불러오는 동안 문제가 발생하였습니다", Toast.LENGTH_LONG).show();
            return;
        }

        filepath_to_play = getAbsolutePath(mContext, data.getData());
        filepath_to_send = null;

        Log.i(TAG, "Music file open Success");

        // firebase 통신
        cf = new ComFire(mActivity, mContext);
        fromMusicData = data;

        setViewVisible_onFile(true);
    }

    /**
     *  view visible setting
     * @param isFileLoaded true : progressbar on, false : progressbar off
     */
    private void setViewVisible_onFile(boolean isFileLoaded)
    {
        // view visiable setting
        if(!isFileLoaded) {
            for(int i = 0; i < 5; i++) radioBtn[i].setVisibility(View.INVISIBLE);
            radioGroup_label_textview.setVisibility(View.INVISIBLE);

            shiftAmount_label_textview.setVisibility(View.INVISIBLE);
            shiftAmount_EditText.setVisibility(View.INVISIBLE);

            floatingActionButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            for(int i = 0; i < 5; i++) radioBtn[i].setVisibility(View.VISIBLE);
            radioGroup_label_textview.setVisibility(View.VISIBLE);

            shiftAmount_label_textview.setVisibility(View.VISIBLE);
            shiftAmount_EditText.setVisibility(View.VISIBLE);

            floatingActionButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * save answerSheet file to local
     * and update list file
     * @param answerSheet
     * @return
     */
    private boolean inputFilename_saveLocal(final AnswerSheet answerSheet)
    {
        // "untitled_" + android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a", new Date())

        String textToWrite = filepath_to_play.substring(filepath_to_play.lastIndexOf("/"), filepath_to_play.length());
        answerSheet.setFileName(textToWrite);

        if(!(new LocalFileHandler(mContext).writeListOfFiles(textToWrite)))
            return false;
        else
            return new LocalFileHandler(mContext, answerSheet.getFileName()).saveAnswerSheet(answerSheet);
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    private String getAbsolutePath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                return Environment.getExternalStorageDirectory() + "/" + split[1];
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    /**
     * Self Check Device External Access
     * @param context mContext
     * @return true or false for accessing
     */
    private boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * for permission check alter dialog
     * @param msg msg
     * @param context mContext
     * @param permission READ_EXTERNAL_STORAGE
     */
    private void showDialog(final String msg, final Context context,
                            final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}
