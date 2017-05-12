package com.novoda.library;

final class FileName {

    private final String name;

    public static FileName fromUrl(String url) {
        int hashCode = url.hashCode();
        String stringHashCode = String.valueOf(hashCode);
        return new FileName(stringHashCode);
    }

    FileName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
