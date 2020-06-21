package com.example.inha_capston.utility_class;

import android.content.Context;
import android.util.Log;

import com.example.inha_capston.handling_audio.AnswerSheet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *  provide interface of access AnswerSheet File
 */
public class LocalFileHandler
{
    private static final String TAG = "localFileHandler";
    private File file;
    private File listFile;

    public LocalFileHandler(Context mContext, String fileName) {
        file = new File(mContext.getFilesDir(), fileName);
    }

    public LocalFileHandler(Context mContext) {
        file = new File(mContext.getFilesDir(), "listFile");
    }

    public boolean writeListOfFiles(String str) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

            if(str != null)
                bufferedWriter.write(str);
            bufferedWriter.newLine();

            bufferedWriter.close();
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found");
            e.printStackTrace();
            return false;
        }
        catch (IOException e) {
            Log.e(TAG, "IO exception");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 파일 목록을 반환
     * @return
     */
    public List<String> ReadListOfFiles() {
        List<String> allLines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNextLine()) {
                allLines.add(scanner.nextLine());
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "file not found");
            e.printStackTrace();

            writeListOfFiles(null);
            return ReadListOfFiles();
        }

        return allLines;
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
            objectOutputStream.close();
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

            AnswerSheet answerSheet = (AnswerSheet) objectInputStream.readObject();

            fileInputStream.close();
            objectInputStream.close();
            return answerSheet;
        }
        catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "LoadAnswerSheet : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
