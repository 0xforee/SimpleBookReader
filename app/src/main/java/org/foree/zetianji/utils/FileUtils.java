package org.foree.zetianji.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by foree on 16-7-25.
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    //读取文件
    //TODO 添加文件为空的处理
    public static String readFile(File file) throws IOException {
        Log.d(TAG, "readFile: " + file.toString());
        FileReader in = null;
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        String temp;
        try {
            in = new FileReader(file);
            bufferedReader = new BufferedReader(in);
            while ((temp = bufferedReader.readLine()) != null) {
                stringBuffer.append(temp).append("\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
            if (in != null)
                in.close();
        }
        return stringBuffer.toString();
    }

    //写入文件
    public static void writeFile(File file, String string) throws IOException {
        FileWriter out = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(out);
        bufferedWriter.write(string);
        bufferedWriter.close();
        out.close();
    }

    // url encode
    public static String encodeUrl(String url){
        return url.replaceAll("/", "_");
    }
}
