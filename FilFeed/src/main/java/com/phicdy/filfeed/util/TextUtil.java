package com.phicdy.filfeed.util;

import java.util.regex.Pattern;

public class TextUtil {

    public static String removeLineFeed(String string) {
        return Pattern.compile("\t|\r|\n|\r\n").matcher(string).replaceAll("");
    }
}
