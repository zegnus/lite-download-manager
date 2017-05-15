package com.novoda.library;

final class FileName {

    private final String name;

    public static FileName from(Batch batch, String fileUrl) {
        String name = batch + fileUrl + String.valueOf(System.nanoTime());
        return new FileName(String.valueOf(name.hashCode()));
    }

    FileName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
