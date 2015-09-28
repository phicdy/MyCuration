package com.phicdy.mycuration.util;

import java.util.regex.Pattern;

public class TextUtil {

    public static String removeLineFeed(String string) {
        return Pattern.compile("\t|\r|\n|\r\n").matcher(string).replaceAll("");
    }

    public static boolean isEmpty(String text) {
        if (text == null || text.equals("")) {
            return true;
        }
        return false;
    }
}
