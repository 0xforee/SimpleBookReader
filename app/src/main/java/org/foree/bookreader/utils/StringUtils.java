package org.foree.bookreader.utils;

/**
 * Created by foree on 17-2-12.
 */

public class StringUtils {

    // 去掉文章开头结尾的non-breaking space和换行符，以及空格
    public static String trim(String string) {
        int start = 0, last = string.length() - 1;
        int end = last;
        while ((start <= end) && ((string.charAt(start) == '\n'))) {
            start++;
        }
        while ((end >= start) && ((string.charAt(end) == '\u00A0' || string.charAt(end) == ' ' || string.charAt(end) == '\n'))) {
            end--;
        }
        if (start == 0 && end == last) {
            return string;
        }
        return string.substring(start, end + 1);
    }
}
