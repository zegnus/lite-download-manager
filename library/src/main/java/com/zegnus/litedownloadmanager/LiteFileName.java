package com.zegnus.litedownloadmanager;

final class LiteFileName implements FileName {

    private final String name;

    public static LiteFileName from(Batch batch, String fileUrl) {
        String name = batch + fileUrl + String.valueOf(System.nanoTime());
        return new LiteFileName(String.valueOf(name.hashCode()));
    }

    public static LiteFileName from(String name) {
        return new LiteFileName(name);
    }

    private LiteFileName(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }
}
