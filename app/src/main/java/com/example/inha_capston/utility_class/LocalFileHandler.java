package com.example.inha_capston.utility_class;

import android.content.Context;
import android.util.Log;

import com.example.inha_capston.handling_audio.AnswerSheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *  provide interface of access AnswerSheet File
 */
public class LocalFileHandler
{
    private static final String TAG = "localFileHandler";
    private File file;

    public LocalFileHandler(Context mContext, String fileName)
    {
        file = new File(mContext.getFilesDir(), fileName);
    }

    /**
     * save object to file
     * @param answerSheet answer sheet
     * @return true or false
     */
    public boolean saveAnswerSheet(AnswerSheet answerSheet)
    {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(answerSheet);
            Log.i(TAG, "AnswerSheet write in " + file.getAbsolutePath());
            fileOutputStream.close();
            return true;
        }
        catch (IOException e) {
            Log.e(TAG, "saveAnswerSheet" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Rear Answer Sheet from file
     * @return Answer Sheet or null when failed
     */
    public AnswerSheet loadAnswerSheet()
    {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return (AnswerSheet) objectInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "LoadAnswerSheet : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
