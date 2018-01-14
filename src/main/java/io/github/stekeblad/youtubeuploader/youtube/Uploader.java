package io.github.stekeblad.youtubeuploader.youtube;

import javafx.concurrent.Task;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Uploader {
    private ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true); // "allows app to exit if tasks are running" // has it any effect?
        return t ;
    });
    private HashMap<String, Future> tasks = new HashMap<>();

    public void add(VideoUpload video, String cancelName) {
        Future upload = exec.submit(new Task<Void>() {
            @Override
            public Void call() throws Exception {
                video.uploadToTheTube();
                return null ;
            }
        });
        tasks.put(cancelName, upload); // save the future to be able to abort upload
    }

    public boolean abortUpload(String cancelName) {
        return tasks.get(cancelName).cancel(true);
    }
}
