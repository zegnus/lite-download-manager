package com.zegnus.litedownloadmanager;

class LiteFilePath implements FilePath {

    private final String path;

    LiteFilePath(String path) {
        this.path = path;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public boolean isUnknown() {
        return path.equalsIgnoreCase(FilePathCreator.UNKNOWN_FILEPATH.path());
    }
}
