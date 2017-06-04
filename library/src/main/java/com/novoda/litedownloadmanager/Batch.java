package com.novoda.litedownloadmanager;

import java.util.ArrayList;
import java.util.List;

public final class Batch {

    private final String title;
    private final List<String> fileUrls;

    private Batch(String title, List<String> fileUrls) {
        this.title = title;
        this.fileUrls = fileUrls;
    }

    String getTitle() {
        return title;
    }

    List<String> getFileUrls() {
        return new ArrayList<>(fileUrls);
    }

    public static class Builder {

        private final String title;
        private final List<String> fileUrls = new ArrayList<>();

        public Builder(String title) {
            this.title = title;
        }

        public Builder addFile(String fileUrl) {
            fileUrls.add(fileUrl);
            return this;
        }

        public Batch build() {
            return new Batch(title, fileUrls);
        }
    }
}
