package com.pennsim.exception;

import com.pennsim.util.LocaleManager;
import com.pennsim.util.StringGetter;

class Strings {

    static LocaleManager source
            = new LocaleManager("pennsim", "exception");

    public static LocaleManager getLocaleManager() {
        return source;
    }

    public static String get(String key) {
        return source.get(key);
    }

    public static StringGetter getter(String key) {
        return source.getter(key);
    }
}