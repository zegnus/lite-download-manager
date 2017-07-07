package com.novoda.litedownloadmanager;

public class FilePath {

    private static final String UNKNOWN = "unknown";

    static final FilePath UNKNOWN_FILEPATH = newInstance(UNKNOWN);

    private final String path;

    public static FilePath newInstance(String path) {
        return new FilePath(path);
    }

    private FilePath(String path) {
        this.path = path;
    }

    String path() {
        return path;
    }

    boolean isUnknown() {
        return path.equals(UNKNOWN);
    }
}
