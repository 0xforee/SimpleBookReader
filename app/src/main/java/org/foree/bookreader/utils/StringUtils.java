package org.foree.bookreader.utils;

import java.util.HashMap;
import java.util.Map;

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

    public static double getSimilarity(String text1, String text2){
        Map<Character, int[]> vectorMap = new HashMap<>();
        // 分析第一个字符串
        for(Character character: text1.toCharArray()){
            if(vectorMap.containsKey(character)){
                vectorMap.get(character)[0]++;
            }else{
                int[] tmp = new int[]{1, 0};
                vectorMap.put(character, tmp);
            }
        }

        // 分析第二个字符串
        for(Character character: text2.toCharArray()){
            if(vectorMap.containsKey(character)){
                vectorMap.get(character)[1]++;
            }else{
                int[] tmp = new int[]{0, 1};
                vectorMap.put(character, tmp);
            }
        }

        // 计算余弦
        double str1 = 0;
        double str2 = 0;
        double numerator = 0;
        for(Character character: vectorMap.keySet()){
            int[] tmp = vectorMap.get(character);
            str1 += tmp[0] * tmp[0];
            str2 += tmp[1] * tmp[1];
            numerator += tmp[0] * tmp[1];
        }
        double denominator = Math.sqrt(str1 * str2);

        return numerator / denominator;
    }
}
