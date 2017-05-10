package com.novoda.library;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

class NetworkFileSizeRequester implements FileSizeRequester {

    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final int UNKNOWN_CONTENT_LENGTH = -1;

    private final OkHttpClient httpClient;

    NetworkFileSizeRequester(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public FileSize requestFileSize(String url) {
        Request.Builder requestBuilder = new Request.Builder()
                .head()
                .url(url);

        try {
            Response response = httpClient.newCall(requestBuilder.build()).execute();
            if (response.isSuccessful()) {
                long totalFileSize = Long.parseLong(response.header(HEADER_CONTENT_LENGTH, String.valueOf(UNKNOWN_CONTENT_LENGTH)));
                return FileSize.Total(totalFileSize);
            }
        } catch (IOException e) {
            // log exception
        }

        return FileSize.Unknown();
    }
}
