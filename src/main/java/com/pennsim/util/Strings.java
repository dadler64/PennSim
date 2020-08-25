package com.pennsim.util;

class Strings {

    static LocaleManager source
            = new LocaleManager("pennsim", "util");

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