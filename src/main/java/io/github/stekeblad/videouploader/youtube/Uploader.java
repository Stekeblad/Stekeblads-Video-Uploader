package io.github.stekeblad.videouploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import io.github.stekeblad.videouploader.youtube.utils.PlaylistUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Uploader handles the actual uploading to Youtube and contains a queue for all uploads. New uploads can be added,
 * existing once can be aborted all at the same time or just a specific. It is possible to get if their is a upload
 * in progress and set a method to be called for all finished uploads with the Id of the upload as the only parameter.
 * The uploading is performed in a separate thread and the class is partly threadsafe, check the methods documentation 
 * to see if the particular method is threadsafe
 */
public class Uploader {
    private final String VIDEO_FILE_FORMAT = "video/";

    private final Map<String, Future> tasks;
    private final CategoryUtils categoryUtils;
    private final PlaylistUtils playlistUtils;
    private Consumer<String> uploadFinishedCallback = null;
    private BiConsumer<VideoUpload, Throwable> uploadErredCallback = null;
    private final ExecutorService exec;
    private final Translations translationsUpload;
    private final Translations translationsBasic;


    public Uploader() {
        translationsUpload = TranslationsManager.getTranslation(TranslationBundles.UPLOADER);
        translationsBasic = TranslationsManager.getTranslation(TranslationBundles.BASE);
        tasks = Collections.synchronizedMap(new HashMap<>());
        categoryUtils = CategoryUtils.INSTANCE;
        playlistUtils = PlaylistUtils.INSTANCE;
        exec = Executors.newSingleThreadExecutor(Thread::new);

    }

    /**
     * Sets a method to be called every time a upload finishes. The parameter given to the callback will be the cancelName
     * that was given in the add() method. Setting this callback is not required. This method is not threadsafe. It is 
     * recommended to call this method shortly after an instance of the class is created and before any uploads are added.
     * @param callback the callback to be called after every finished upload.
     */
    public void setUploadFinishedCallback(Consumer<String> callback) {
        this.uploadFinishedCallback = callback;
    }

    /**
     * Sets a method to be called when a upload fails and it can not be handled by this class. The parameters given to the
     * callback will be the VideoUpload that erred and the exception that could not be handled.
     * Setting this callback is not required. This method is not threadsafe. It is 
     * recommended to call this method shortly after an instance of the class is created and before any uploads are added.
     * @param callback the callback to be called when an upload errors
     */
    public void setUploadErredCallback(BiConsumer<VideoUpload, Throwable> callback) {
        this.uploadErredCallback = callback;
    }

    /**
     * Aborts a single upload, scheduled or active. This method is threadsafe.
     * @param cancelName the cancelName that was given when the add() method was called
     * @return true if the upload was aborted or no upload with the given name exists,
     * false if it for some reason is not possible to abort it.
     */
    public boolean abortUpload(String cancelName) {
        if (tasks.containsKey(cancelName)) {
            boolean success;
            success = tasks.get(cancelName).cancel(true);
            if (success) {
                tasks.remove(cancelName);
            }
            return success;
        }
        return true;
    }

    /**
     * This method is threadsafe.
     * @return true if a upload is in progress, false if not.
     */
    public boolean getIsActive() {
        return !tasks.keySet().isEmpty();
    }

    /**
     * @return a set with the given cancelName of all uploads currently in the queue
     */
    public Set<String> getUploadQueue() {
        return tasks.keySet();
    }

    /**
     * Aborts all uploads and shuts down the executor service that performs the background work.
     * This method is intended to be used when the program is about to shut down and no new uploads should be added
     * to this instance after this method has been called.
     * @return a Set with the cancelName of all unfinished uploads that was aborted.
     */
    public Set<String> kill() {
        exec.shutdownNow();
        return tasks.keySet();
    }

    /**
     * Adds video to the upload list. This method is threadsafe.
     * @param video video to upload
     * @param cancelName String to use for aborting the upload (and used to report that its finished if a callback is set)
     */
    public void add(VideoUpload video, String cancelName) {
        // Create the task
        Task newTask = new Task<Void>() {
            @Override
            // Define what it does
            protected Void call() {
                try {
                    // Do the uploading, but first a short wait, if something goes wrong we want a chance to abort all
                    // waiting uploads instead of all of them creating exception dialogs before we have a chance to react
                    try {
                        Thread.sleep(1000 * 2);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("INTERRUPTED");
                    }
                    // now, upload!
                    upload(video);
                } catch (Exception e) {
                    // if not interrupted by the user, print the error and call error handler if it is set
                    if (e.getMessage() != null && !e.getMessage().equals("INTERRUPTED")) {
                        e.printStackTrace();
                        if (uploadErredCallback != null) {
                            Platform.runLater(() -> uploadErredCallback.accept(video, e));
                        }
                    }
                    tasks.remove(cancelName);
                    return null;
                }
                // If upload finished without errors and callback is set, give the cancel name to the callback
                if(uploadFinishedCallback != null) {
                    uploadFinishedCallback.accept(cancelName);
                }
                // Remove the task from the list
                tasks.remove(cancelName);
                return null;
            }
        };
        newTask.setOnFailed(event -> {
            if (uploadErredCallback != null) {
                Platform.runLater(() -> uploadErredCallback.accept(video, newTask.getException()));
            }
        });
        Future upload = exec.submit(newTask);
        tasks.put(cancelName, upload); // save the future to be able to abort upload
    }

    /**
     * Does the uploading.
     * @param video a VideoUpload with all the details needed for uploading
     * @throws IOException if the user aborts the upload while it is uploading, there is a exception while reading the video
     * or thumbnail file or there is a network error that could not be handled.
     */
    private void upload(VideoUpload video) throws IOException {

        // debug thing to force error
        if (video.getVideoName().equals("forceUploadFailure")) {
            throw new RuntimeException("forced by filename.\nThe filename used is reserved for testing, uploads with this " +
                    "name will ALWAYS fail");
        }
        // debug thing to force error after a 10 seconds delay
        if (video.getVideoName().equals("forceUploadFailure_delayed")) {
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
                throw new RuntimeException("forced by filename.\nThe filename used is reserved for testing, uploads with this " +
                        "name will ALWAYS fail");
            }
            throw new RuntimeException("forced by filename.\nThe filename used is reserved for testing, uploads with this " +
                    "name will ALWAYS fail");
        }

        // Extra check if this upload has been aborted
        if (!tasks.containsKey(video.getPaneId()))
            throw new IOException("INTERRUPTED");

        // debug for testing daily upload limit exceeded
        if (video.getVideoName().equals("forceDailyLimit")) {
            throw new RuntimeException("The daily upload limit has been reached. (dailyLimitExceeded)");
        }

        // Auth the user and create the Youtube object
        Credential creds = Auth.authUser();
        YouTube myTube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
            creds.initialize(request);
            // Tell Youtube to attempt resume upload if a network error occur.
            request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(new ExponentialBackOff()));
        }).setApplicationName("Stekeblads Video Uploader").build();

        // Start building the Youtube Video object
        Video videoObject = new Video();

        videoObject.setStatus(new VideoStatus().setPrivacyStatus(video.getVisibility().getStatusName()));

        VideoSnippet videoMetaData = new VideoSnippet();
        videoMetaData.setTitle(video.getVideoName());
        videoMetaData.setDescription(video.getVideoDescription());
        videoMetaData.setTags(video.getVideoTags());
        videoMetaData.setCategoryId(categoryUtils.getCategoryId(video.getCategory()));

        videoObject.setSnippet(videoMetaData);
        InputStreamContent videoFileStream;
        try {
            videoFileStream = new InputStreamContent(VIDEO_FILE_FORMAT,
                    new BufferedInputStream(new FileInputStream(video.getVideoFile())));

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Could not find the video file \"" + video.getVideoFile().getAbsolutePath() +
                    "\". It may have been deleted, moved or renamed since the upload was queued");
        }

        YouTube.Videos.Insert videoInsert = myTube.videos()
                .insert("snippet,statistics,status", videoObject, videoFileStream);
        videoInsert.setNotifySubscribers(video.isTellSubs());

        // getMediaHttpUploader for being able to report progress
        MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(false); // makes the upload resumable?

        MediaHttpUploaderProgressListener progressListener = uploader1 -> {
            // If abortUpload() has been called for this upload while it is uploading
            if(Thread.interrupted()) {
                // Throw an exception (Only IOException allowed)
                throw new IOException("INTERRUPTED");
            }
            switch (uploader1.getUploadState()) {
                case INITIATION_STARTED:
                    Platform.runLater(() -> video.setStatusLabelText(translationsUpload.getString("preparing")));
                    break;
                case INITIATION_COMPLETE:
                    Platform.runLater(() -> video.setProgressBarProgress(0));
                    Platform.runLater(() -> video.setStatusLabelText(translationsUpload.getString("starting")));
                    break;
                case MEDIA_IN_PROGRESS: // uploader1.getProgress() errors, this is not a perfect replacement as
                    // the upload is slightly larger than the video file, but for longer videos it will be close enough
                    double progress = ((double) uploader1.getNumBytesUploaded() / video.getVideoFile().length());
                    Platform.runLater(() -> video.setProgressBarProgress(progress));
                    String newStatusText = String.format(
                            translationsUpload.getString("uploadWithProgress"), (int) Math.floor(progress * 100));
                    Platform.runLater(() -> video.setStatusLabelText(newStatusText));
                    break;
                case MEDIA_COMPLETE:
                    Platform.runLater(() -> video.setProgressBarProgress(1)); // 100% full
                    Platform.runLater(() -> video.setStatusLabelText(translationsUpload.getString("finished")));
                    break;
                case NOT_STARTED:
                    Platform.runLater(() -> video.setStatusLabelText(translationsUpload.getString("notStarted")));
                    break;
            }
        };
        uploader.setProgressListener(progressListener);

        // finally ready for upload!
        Video uploadedVideo = videoInsert.execute();

        // Set thumbnail if selected
        if (video.getThumbNail() != null) {
            Platform.runLater(() -> video.setStatusLabelText(translationsUpload.getString("thumbnail")));
            File thumbFile = video.getThumbNail();
            String contentType = Files.probeContentType(Paths.get(thumbFile.toURI()));

            InputStreamContent thumbnailFileContent;
            try {
                thumbnailFileContent = new InputStreamContent(
                        contentType, new BufferedInputStream(new FileInputStream(thumbFile)));
                thumbnailFileContent.setLength(thumbFile.length());
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("Could not find the thumbnail file \"" + thumbFile.getAbsolutePath() +
                        "\". It may have been deleted, moved or renamed since the upload was queued");
            }
            YouTube.Thumbnails.Set thumbnailSet = myTube.thumbnails().set(uploadedVideo.getId(), thumbnailFileContent);
            thumbnailSet.execute();
        }
        // Add to playlist if it is not null, empty or the "no selected" default value
        String playlistString = video.getSelectedPlaylist();
        if (playlistString != null && !playlistString.equals("null") && !playlistString.equals("") &&
                !playlistString.equals(translationsBasic.getString("noSelected"))) {
            String newStatusText = String.format(translationsUpload.getString("playlist"), video.getSelectedPlaylist());
            Platform.runLater(() -> video.setStatusLabelText(newStatusText));
            ResourceId resourceId = new ResourceId();
            resourceId.setKind("youtube#video");
            resourceId.setVideoId(uploadedVideo.getId());

            PlaylistItemSnippet playlistSnippet = new PlaylistItemSnippet();
            playlistSnippet.setPlaylistId(playlistUtils.getPlaylistId(video.getSelectedPlaylist()));
            playlistSnippet.setResourceId(resourceId);

            PlaylistItem playlistItem = new PlaylistItem();
            playlistItem.setSnippet(playlistSnippet);
            YouTube.PlaylistItems.Insert playlistInsert = myTube.playlistItems().insert("snippet,contentDetails", playlistItem);
            playlistInsert.execute();
        }
        String link = "https://youtu.be/" + uploadedVideo.getId();
        String newStatusText = String.format(translationsUpload.getString("doneWithLink"), link);
        Platform.runLater(() -> video.setStatusLabelText(newStatusText));
        Platform.runLater(() -> video.setStatusLabelOnClickUrl(link));
    }
}
