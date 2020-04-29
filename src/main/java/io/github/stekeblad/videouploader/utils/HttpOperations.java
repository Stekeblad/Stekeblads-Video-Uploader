package io.github.stekeblad.videouploader.utils;

import okhttp3.*;

import java.io.IOException;

/**
 * Simple methods hiding the logic in making HTTP GETs and POSTs
 */
public class HttpOperations {
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Performs a HTTP GET request on the given URL
     *
     * @param url: A string with the url to connect to
     * @return The response as a string or null if any exception is caught
     */
    public static String getString(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                return null;

            ResponseBody body = response.body();
            if (body == null)
                return null;

            return body.string();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Performs a HTTP GET request on the given URL
     *
     * @param url: A string with the url to connect to
     * @return The response as a byte[] or null if any exception is caught
     */
    public static byte[] getBytes(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                return null;

            ResponseBody body = response.body();
            if (body == null)
                return null;

            return body.bytes();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Performs a HTTP POST request to the given URL containing the given formBody
     *
     * @param url      the url to post to
     * @param formBody the form content to post
     * @return true on successful response, false otherwise
     * @throws IOException in case of network errors or other IO stuff.
     */
    public static boolean postForm(String url, RequestBody formBody) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Response response = client.newCall(request).execute();
        boolean success = response.isSuccessful();

        ResponseBody body = response.body();
        if (body != null) {
            String tempResponseBody = body.string();
        }
        response.close();
        return success;

    }
}
