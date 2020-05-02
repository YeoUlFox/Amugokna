package com.example.inha_capston;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.example.inha_capston.handling_audio.AnswerSheet;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static android.text.TextUtils.indexOf;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener {
    private NavController navController;
    private ImageButton listBtn;
    private ImageButton loadBtn;
    private ImageButton recordBtn;
    private Chronometer timer;
    private TextView filename_textView;

    // context
    private Context mContext;       // getContext()
    private Activity mActivity;     // getActivity()

    // flags
    private int REQUEST_AUDIO_MP3 = 1;                  // for mp3 request Intent
    private boolean isRecording = false;            // recording state
    private boolean isDetecting = false;

    // permission variable
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 0;

    // record handling variable
    private MediaRecorder mediaRecorder;
    private String recordFile;

    // make anwer sheet with tarsos DSP lib
    private AudioDispatcher dispatcher;
    private AnswerSheet answerSheet;
    private InputStream inputStream;
    private FileInputStream fileInputStream;
    private File audioFile;

    // meta data
    private String metaArtist;
    private String metaTitle;

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
        timer = view.findViewById(R.id.record_timer);
        listBtn = view.findViewById(R.id.record_list_btn);
        loadBtn = view.findViewById(R.id.load_btn);
        recordBtn = view.findViewById(R.id.record_btn);
        filename_textView = view.findViewById(R.id.record_filename);

        recordBtn.setOnClickListener(this);
        listBtn.setOnClickListener(this);
        loadBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_list_btn:
                // transition fragment with anim, show list of audio record files
                if (isRecording) {
                    // alter to user for confirm
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isRecording = false;
                            navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                        }
                    });
                    alertDialog.setPositiveButton("아니", null);
                    alertDialog.setTitle("녹음중인데?");
                    alertDialog.setMessage("그만 할꺼?");
                    alertDialog.create().show();
                } else
                    navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                break;
            case R.id.load_btn:
                // load .mp3 file from device
                if (isRecording) {
                    // alter to user for confirm
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isRecording = false;
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("audio/*");
                            startActivityForResult(Intent.createChooser(intent, "불러올 음악 파일을 선택해주세요"), REQUEST_AUDIO_MP3);
                        }
                    });
                    alertDialog.setPositiveButton("아니", null);
                    alertDialog.setTitle("녹음중인데?");
                    alertDialog.setMessage("그만 할꺼?");
                    alertDialog.create().show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("audio/*");
                    //Intent.createChooser(intent, "불러올 음악 파일을 선택해주세요")
                    startActivityForResult(intent, REQUEST_AUDIO_MP3);
                }
                break;
            case R.id.record_btn:
                Toast.makeText(mContext, "ㅇㅅㅇ", Toast.LENGTH_SHORT).show();
                if (isRecording) {
                    // Stop Recording
                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_mic_96dp));
                    stop_Recording();
                    isRecording = false;
                } else {
                    // Start Recording
                    if (checkPermissions()) {
                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_none));
                        start_Recording();
                        isRecording = true;
                    }
                }
                break;
        }
    }

    /**
     * stop record
     */
    private void stop_Recording() {
        timer.stop();
        // erase filename
        filename_textView.setText("");

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    /**
     * record start
     */
    private void start_Recording() {
        // file name formatting
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.KOREA);
        Date now = new Date();

        // timer setting
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        String recordPath = mActivity.getExternalFilesDir("/").getAbsolutePath();
        recordFile = "Recording..." + formatter.format(now) + ".3gp";

        filename_textView.setText(recordFile);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    /**
     * check permissions for mic recording
     *
     * @return true for yes
     */
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(mContext, recordPermission) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            ActivityCompat.requestPermissions(mActivity, new String[]{recordPermission}, PERMISSION_CODE);
        return false;
    }

    /**
     * for stop recording
     */
    @Override
    public void onStop() {
        super.onStop();
        if (isRecording)
            stop_Recording();
        if (isDetecting) {
            releaseDispatcher();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }

    /**
     * release the audio dispatcher
     */
    private void releaseDispatcher() {
        if (dispatcher != null) {
            if (!dispatcher.isStopped())
                dispatcher.stop();
            dispatcher = null;
        }
        isDetecting = false;
    }

    /**
     * handling loaded mp3 file
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            Toast.makeText(mContext, "파일을 불러오는 도중 문제가 발생했습니다", Toast.LENGTH_LONG).show();
            return;
        }

        Uri uri = data.getData();

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_AUDIO_MP3) // TODO change
        {
            // audio formatter
            TarsosDSPAudioFormat tarsosDSPAudioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                    22050,
                    2 * 8,
                    1,
                    2 * 1,
                    22050,
                    ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

            PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                    final float pitchHz = res.getPitch();
                    if (pitchHz != -1 && res.getProbability() > 0.1) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                filename_textView.setText(pitchHz + "");
                            }
                        });

                        answerSheet.addPitch(pitchHz);
                        answerSheet.addTimes(e.getTimeStamp());
                    }
                    if (e.getProgress() <= 1) answerSheet.setEndtimeStamp(e.getEndTimeStamp());
                }
            };


            try {
                if (checkPermissionREAD_EXTERNAL_STORAGE(mContext)) {
                    audioFile = new File(getAbsolutePath(mContext, uri));
                    String path = audioFile.getAbsolutePath();

                    File f1 = new File(mContext.getFilesDir(), "out.wav");
//                    decoded_path = decoded_path.substring(0, decoded_path.lastIndexOf('/'));
//                    decoded_path = decoded_path + "/out.wav";

                    int rc = FFmpeg.execute(getCommand(f1.getAbsolutePath(), path, 22050, 0, -1));

                    if (rc == RETURN_CODE_SUCCESS) {
                        Log.i(Config.TAG, "Command execution completed successfully.");
                    } else if (rc == RETURN_CODE_CANCEL) {
                        Log.i(Config.TAG, "Command execution cancelled by user.");
                    } else {
                        Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
                        Config.printLastCommandOutput(Log.INFO);
                    }


                    fileInputStream = new FileInputStream(f1);
                    //dispatcher = myAudioDispatcherFactory.fromPipe(mContext, audioFile.getAbsolutePath(), 44100, 4096, 2048);
                    //dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 4096, 0);
                    dispatcher = new AudioDispatcher(new UniversalAudioInputStream(fileInputStream, tarsosDSPAudioFormat), 1024, 0);

                    answerSheet = new AnswerSheet();

                    AudioProcessor playerProcessor = new AndroidAudioPlayer(dispatcher.getFormat(), 4096, AudioManager.STREAM_MUSIC);
                    dispatcher.addAudioProcessor(playerProcessor);

                    AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchDetectionHandler);
                    dispatcher.addAudioProcessor(pitchProcessor);

                    Thread pitchThread = new Thread(dispatcher, "Pitch Detection Thread");
                    pitchThread.start();

                    /*
                    try {
                        isDetecting = true;
                        Thread pitchThread = new Thread(dispatcher, "Pitch Detection Thread");
                        pitchThread.start();
                        //pitchThread.join();
                        //TODO : progress bar setting

                        // SaveAnswerSheet();
                    }
                    catch (InterruptedException e)
                    {
                        releaseDispatcher();
                        SaveAnswerSheet();
                        e.printStackTrace();
                    }
                     */
                }
            } catch (IOException e) {
                Toast.makeText(mContext, "파일을 찾을 수 없습니다", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return;
            }
        }
    }

    String safUriToFFmpegPath(final Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = mContext.getContentResolver().openFileDescriptor(uri, "r");
            return String.format(Locale.getDefault(), "pipe:%d", parcelFileDescriptor.getFd());
        } catch (FileNotFoundException e) {
            return "";
        }
    }

    private String getCommand(final String output_path, final String resource, final int targetSampleRate, final double timeOffset, double numberOfSeconds) {
        String command = " -ss %input_seeking%  %number_of_seconds% -i \"%resource%\"  -vn -ar %sample_rate% -ac %channels% -sample_fmt s16 -f s16le ";
        //
        command = command.replace("%input_seeking%", String.valueOf(timeOffset));
        //defines the number of seconds to process
        // -t 10.000 e.g. specifies to process ten seconds
        // from the specified time offset (which is often zero).
        if (numberOfSeconds > 0) {
            command = command.replace("%number_of_seconds%", "-t " + String.valueOf(numberOfSeconds));
        } else {
            command = command.replace("%number_of_seconds%", "");
        }
        command = command.replace("%resource%", resource);
        command = command.replace("%sample_rate%", String.valueOf(targetSampleRate));
        command = command.replace("%channels%", "1");

        return command + " " + output_path;
    }


    /**
     * not used
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }


    /**
     * after, check permission, use uri and set audio dispatcher
     * @param context
     * @param uri
     *          file uri from intent
     */
    private void setAudioFile_dispatcher(Context context, Uri uri)
    {
        audioFile = new File(getAbsolutePath(mContext, uri));
        dispatcher = AudioDispatcherFactory.fromPipe(audioFile.getAbsolutePath(), 44100, 4096, 2048);

        //catching metadata from file
        MediaMetadataRetriever fileMeta = new MediaMetadataRetriever();
        fileMeta.setDataSource(audioFile.getAbsolutePath());
        fileMeta.setDataSource(audioFile.getAbsolutePath());
        metaArtist = fileMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        metaTitle = fileMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    }


    /**
     * save the detection result and handling opened resources
     * set Flag and handle stream closed
     * later, use ViewModel but now, it send to play Fragment with bundle object
     */
    private void SaveAnswerSheet()
    {
        try {
            // handle open input stream
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        releaseDispatcher();
        Bundle bundleArgs = new Bundle();
        bundleArgs.putSerializable("answer", answerSheet);
        navController.navigate(R.id.action_recordFragment_to_playFragment, bundleArgs);

        isDetecting = false;
    }

    /**
     * check if intent data is virtual file
     * @param uri
     * @return true - virtual
     */
    private boolean isVirtualFile(Uri uri) {
        if (!DocumentsContract.isDocumentUri(mContext, uri)) {
            return false;
        }

        Cursor cursor = mActivity.getContentResolver().query(
                uri,
                new String[] { DocumentsContract.Document.COLUMN_FLAGS },
                null, null, null);

        int flags = 0;
        if (cursor.moveToFirst()) {
            flags = cursor.getInt(0);
        }
        cursor.close();

        return (flags & DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT) != 0;
    }

    /**
     * get Input Stream
     * @param uri
     * @return
     * @throws IOException
     */
    private InputStream getInputStream(Uri uri) throws IOException {
        ContentResolver resolver = mActivity.getContentResolver();
        String mimeTypeFilter = "audio/*";
        String[] openableMimeTypes = resolver.getStreamTypes(uri, mimeTypeFilter);

        if (openableMimeTypes == null || openableMimeTypes.length < 1) {
            throw new FileNotFoundException();
        }

        if(isVirtualFile(uri))
            return resolver.openInputStream(uri);
        else
            return resolver
                .openTypedAssetFileDescriptor(uri, openableMimeTypes[0], null)
                .createInputStream();
    }

    /**
     *  get String file name from uri
     * @param uri
     * @return
     */
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = mActivity.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    result += ".mp3";
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * get Absolute path
     * @param contentUri
     * @return
     */
    private String getRealPathFromURI(Uri contentUri)
    {
        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath();
        }
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = { MediaStore.Files.FileColumns.DATA };
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        Cursor cursor = mActivity.getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null);

        try {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        } finally {
            cursor.close();
        }
        return null;
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

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

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

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    /**
     *
     * @param context
     * @return
     */
    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
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
     * @param msg
     * @param context
     * @param permission
     */
    public void showDialog(final String msg, final Context context,
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
