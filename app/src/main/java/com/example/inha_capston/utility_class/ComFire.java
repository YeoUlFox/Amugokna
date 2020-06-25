package com.example.inha_capston.utility_class;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.inha_capston.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class ComFire {
    //업로드 파일
    private Context mContext;
    private Activity mActivity;

    private boolean upload_flag;


    private Intent intent;

    public ComFire(Activity curActivity,Context curContext){
        Log.i("mj","comfire generation");
        mActivity = curActivity;
        mContext=curContext;
    }

    public void aquirePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 마시멜로우 버전과 같거나 이상이라면
            if(ContextCompat.checkSelfPermission(mActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(mActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(mActivity, "외부 저장소 사용을 위해 읽기/쓰기 필요", Toast.LENGTH_SHORT).show();
                }
                ActivityCompat.requestPermissions(mActivity, new String[]
                                {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                        2);  //마지막 인자는 체크해야될 권한 갯수

            } else {
                Toast.makeText(mActivity, "권한 승인되었음 근데 재실행하셈 안되면 문의 ㄱㄱ", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void mjtest(){

    }

    public boolean includesForUploadFiles(Intent data, int keyadj) throws IOException, InterruptedException {
        aquirePermission();
        String path= Environment.getExternalStorageDirectory().getPath();
        Log.i("mj",path);
        //Uri file = Uri.fromFile(new File(path));
        Uri file = data.getData();
        Log.i("mj",file.getPath());

        String[] filePathSplit = file.getPath().toString().split("/");
        int filePathLastIdx = filePathSplit.length-1;
        String fileName = filePathSplit[filePathLastIdx];
        String filePureName = fileName.split("\\.")[0];

        fileName = getAbsolutePath(mContext, data.getData());
        filePureName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference musicRef = storageRef.child(fileName);

        //try
        {
            String inputData = Integer.toString(keyadj);
            String pitchFile = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()) + "pitch.txt";

            FileOutputStream fos = mContext.openFileOutput(pitchFile, Context.MODE_PRIVATE);

            fos.write(inputData.getBytes());
            fos.close();;
            Log.i("mj","done writing example txt, "+mContext.getFilesDir());
            Thread.sleep(1000);

            File readfile = new File(mContext.getFilesDir()+"/"+pitchFile);
            Log.i("mj",readfile.getAbsolutePath());
            FileInputStream fr = new FileInputStream(readfile) ;


            StorageReference pitchRef = storageRef.child(pitchFile);
            //Log.i("mj",Uri.parse(mContext.getFilesDir()+"/"+pitchFile).getPath());
            //UploadTask uploadTask = pitchRef.putFile(Uri.parse(mContext.getFilesDir()+"/"+pitchFile));
            UploadTask uploadTask = pitchRef.putStream(fr);

            upload_flag = true;

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    upload_flag = false;
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    upload_flag = true;
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });

        }

        //StorageReference riversRef = storageRef.child(file.getLastPathSegment());
        UploadTask uploadTask = musicRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                upload_flag = false;
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                upload_flag = true;
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });

        return upload_flag;
    }
    ///////////업로드 끝


    ///////////////////////////////다운로드 파일

    public ArrayList<String> includesForDownloadFiles(Intent data) throws IOException{
        aquirePermission();
        String inputData = "mijin is kawai\n";

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Uri file = data.getData();

        String[] filePathSplit = file.getPath().toString().split("/");
        int filePathLastIdx = filePathSplit.length-1;
        String fileName = filePathSplit[filePathLastIdx];
        Log.i("mj",fileName+", "+fileName.split("\\.").length);
        String filePureName = fileName.split("\\.")[0];
        String fileFormat; //= fileName.split("\\.")[1];

        // added
        fileName = getAbsolutePath(mContext, data.getData());
        filePureName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
        filePureName = filePureName.substring(0, filePureName.lastIndexOf("."));
        fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());


        StorageReference  islandRef = storageRef.child(filePureName+"vocals.wav");
        StorageReference fileOriRef = storageRef.child(filePureName+"ori."+fileFormat);
        //String filepath_to_send = getAbsolutePath(mContext, data.getData());//can't use
        /*
        String fileParentPath = "";
        for(int i =0;i<filePathLastIdx;i++){
            fileParentPath+=filePathSplit[i];
        }
        String s = "mjtemp";
        */

        File rootPath =  new File(mContext.getFilesDir().getAbsolutePath());
        if(!rootPath.exists()) {
            rootPath.mkdirs();
            Log.i("mj","after rootppath genr : "+rootPath.getAbsolutePath());
        }
        final File localFileVoc = new File(rootPath,filePureName+"vocals.wav");
        //localFile.createNewFile();
        Log.i("mj",localFileVoc.getAbsolutePath());


        final ArrayList<String> ret_pathes = new ArrayList<>();

        islandRef.getFile(localFileVoc).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.i("firebase ",";local tem file created  created " + localFileVoc.getAbsolutePath().toString());

                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());

            }
        });



        final File localFileOri = new File(rootPath,filePureName + "ori.wav");
        //localFile.createNewFile();
        Log.i("mj",localFileOri.getAbsolutePath());

        islandRef.getFile(localFileOri).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.i("firebase ",";local tem file created  created " + localFileOri.getAbsolutePath().toString());

                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());

            }
        });

        ret_pathes.add(localFileVoc.getAbsolutePath());
        ret_pathes.add(localFileOri.getAbsolutePath());

        return ret_pathes;
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
                                    3);
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
                                3);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}