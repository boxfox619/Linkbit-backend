package com.boxfox.cross.common.util;

import com.boxfox.cross.common.data.Config;

public class LogUtil {

    public static void debug(String message) {
        if (isDebug())
            System.out.println(message);
    }

    public static void debug(String format, Object ... args) {
        if (isDebug())
            System.out.println(String.format(format, args));
    }

    public static boolean isDebug() {
        return Config.getDefaultInstance().getBoolean("debug");
    }
}
