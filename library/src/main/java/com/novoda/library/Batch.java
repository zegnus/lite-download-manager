package com.novoda.library;

import java.util.ArrayList;
import java.util.List;

public final class Batch {

    private final String id;
    private final List<String> fileUrls;

    private Batch(String id, List<String> fileUrls) {
        this.id = id;
        this.fileUrls = fileUrls;
    }

    String getId() {
        return id;
    }

    List<String> getFileUrls() {
        return new ArrayList<>(fileUrls);
    }

    public static class Builder {

        private final String id;
        private final List<String> fileUrls = new ArrayList<>();

        public Builder(String id) {
            this.id = id;
        }

        public Builder addFile(String fileUrl) {
            fileUrls.add(fileUrl);
            return this;
        }

        public Batch build() {
            return new Batch(id, fileUrls);
        }
    }
}
