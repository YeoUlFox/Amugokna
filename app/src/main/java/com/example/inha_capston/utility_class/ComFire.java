package com.example.inha_capston.utility_class;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
        setUpload_flag(false);

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

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference musicRef = storageRef.child(fileName);

        //try
        {
            String inputData = Integer.toString(keyadj);
            String pitchFile = filePureName+"pitch.txt";

            FileOutputStream fos = mContext.openFileOutput(pitchFile, Context.MODE_PRIVATE);

            fos.write(inputData.getBytes());
            fos.close();;
            Log.i("mj","done writing example txt, "+mContext.getFilesDir());
            Thread.sleep(1000);

            File readfile = new File(mContext.getFilesDir()+"/"+pitchFile);
            Log.i("mj",readfile.getAbsolutePath());
            FileInputStream fr = new FileInputStream(readfile) ;


            StorageReference pitchRef = storageRef.child(pitchFile);
            Log.i("mj",Uri.parse(mContext.getFilesDir()+"/"+pitchFile).getPath());
            //UploadTask uploadTask = pitchRef.putFile(Uri.parse(mContext.getFilesDir()+"/"+pitchFile));
            UploadTask uploadTask = pitchRef.putStream(fr);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    setUpload_flag(false);
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    setUpload_flag(true);
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
                setUpload_flag(false);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                setUpload_flag(true);
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });

        return upload_flag;
    }

    private void setUpload_flag(boolean flag) {
        upload_flag = flag;
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
        String fileFormat = fileName.split("\\.")[1];
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
                Log.i("firebase ",";local tem file created  created " +localFileVoc.getAbsolutePath().toString());
                ret_pathes.add(localFileVoc.getAbsolutePath());
                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());

            }
        });

        final File localFileOri = new File(rootPath,filePureName+"ori.wav");
        //localFile.createNewFile();
        Log.i("mj",localFileOri.getAbsolutePath());

        islandRef.getFile(localFileOri).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.i("firebase ",";local tem file created  created " +localFileOri.getAbsolutePath().toString());
                ret_pathes.add(localFileOri.getAbsolutePath());
                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());

            }
        });

        return ret_pathes;
    }
}