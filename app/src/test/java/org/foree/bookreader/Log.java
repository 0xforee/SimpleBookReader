package org.foree.bookreader;

/**
 * Created by foree on 2018/7/9.
 */

public class Log {
    public static void d(String tag, String s){
        System.out.println(tag + ", DEBUG: " + s);
    }

    public static void e(String tag, String s){
        System.out.println(tag + ", Error: " + s);
    }
}
