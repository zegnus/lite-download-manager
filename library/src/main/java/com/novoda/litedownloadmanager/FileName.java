package com.novoda.litedownloadmanager;

public final class FileName {

    private final String name;

    public static FileName from(Batch batch, String fileUrl) {
        String name = batch + fileUrl + String.valueOf(System.nanoTime());
        return new FileName(String.valueOf(name.hashCode()));
    }

    public static FileName from(String name) {
        return new FileName(name);
    }

    FileName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "FileName{" +
                "name='" + name + '\'' +
                '}';
    }
}
