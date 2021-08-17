package io.github.stekeblad.videouploader.utils.background;

import io.github.stekeblad.videouploader.models.NewVideoPresetModel;
import io.github.stekeblad.videouploader.models.NewVideoUploadModel;
import io.github.stekeblad.videouploader.tagProcessing.ITagProcessor;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.jcodec.api.NotSupportedException;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Can apply a preset to a set of video files in a background thread and return VideoUpload objects.
 */
public class PresetApplicator {
    private final Map<String, Future<?>> tasks;
    private Consumer<NewVideoUploadModel> successCallback = null;
    private BiConsumer<File, Throwable> errorCallback = null;
    private final ExecutorService exec;

    private NewVideoPresetModel lastPreset;
    private List<ITagProcessor> tagProcessors = null;

    public PresetApplicator() {
        exec = Executors.newSingleThreadExecutor(Thread::new);
        tasks = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Set a method to be called every time PresetApplicator has successfully applied a preset to a video.
     * Setting this callback is required and needs to be set before providing any videos to apply a preset to.
     * If you attempt to change the callback while the PresetApplicator is working an NotSupportedException will be thrown.
     *
     * @param presetApplicatorSuccessCallback the callback to call for every created VideoUpload
     * @throws NotSupportedException if you try to change the callback while the PresetApplicator is working
     */
    public void setSuccessCallback(Consumer<NewVideoUploadModel> presetApplicatorSuccessCallback) throws NotSupportedException {
        boolean empty;
        synchronized (tasks) {
            empty = tasks.keySet().isEmpty();
        }
        if (!empty)
            throw new NotSupportedException("Success callback can not be changed while the PresetApplicator is working");
        else
            successCallback = presetApplicatorSuccessCallback;
    }

    /**
     * Set a method to be called if any unhandled exceptions are thrown.
     * Setting this callback is not required. It is recommended to call this method
     * shortly after an instance of the class is created and before providing any videos to apply a preset to.
     * If you attempt to change the callback while the PresetApplicator is working an NotSupportedException will be thrown.
     *
     * @param presetApplicatorErrorCallback the callback to call on exceptions
     * @throws NotSupportedException if you try to change the callback while the PresetApplicator is working
     */
    public void setErrorCallback(BiConsumer<File, Throwable> presetApplicatorErrorCallback) throws NotSupportedException {
        boolean empty;
        synchronized (tasks) {
            empty = tasks.keySet().isEmpty();
        }
        if (!empty)
            throw new NotSupportedException("Error callback can not be changed while the PresetApplicator is working");
        else
            errorCallback = presetApplicatorErrorCallback;
    }

    /**
     * @return true if the PresetApplicator is working with apply a preset to one or more videos
     */
    public boolean getIsActive() {
        return !tasks.keySet().isEmpty();
    }

    /**
     * Aborts all work with applying presets to videos
     *
     * @return a set with the names of all files that was queued but never finished processing
     */
    public String[] kill() {
        exec.shutdownNow();
        synchronized (tasks) {
            return tasks.keySet().toArray(new String[0]);
        }
    }

    public void applyPreset(List<File> videoFiles, NewVideoPresetModel preset, int autoNum) {
        for (File videoFile : videoFiles) {
            // assume the user will not send a file to PresetApplicator that is already queued
            String cancelName = videoFile.getAbsolutePath();
            int taskAutoNum = autoNum; // variable used inside task "needs to be final or effectively final"
            // Create a task
            Task<Void> newTask = new Task<>() {
                @Override
                // Define what it does
                protected Void call() {
                    try {
                        //Apply
                        NewVideoUploadModel readyUpload = apply(videoFile, preset, taskAutoNum);
                        Platform.runLater(() -> successCallback.accept(readyUpload));
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (errorCallback != null) {
                            Platform.runLater(() -> errorCallback.accept(videoFile, e));
                        }
                    }
                    // remove the task from the list
                    synchronized (tasks) {
                        tasks.remove(cancelName);
                    }
                    return null;
                }
            };
            newTask.setOnFailed(event -> {
                if (errorCallback != null) {
                    Platform.runLater(() -> errorCallback.accept(videoFile, newTask.getException()));
                }
            });
            Future<?> futureTask = exec.submit(newTask);
            synchronized (tasks) {
                tasks.put(cancelName, futureTask); // save the future to be able to abort the task
            }
            autoNum++;
        }

    }

    /**
     * Does the thing we actually care about
     *
     * @param videoFile a video file to apply a preset to
     * @param preset    the preset to apply
     * @param autoNum   automatic episode numbering number
     * @return a VideoUpload, ready to be uploaded
     */
    private NewVideoUploadModel apply(File videoFile, NewVideoPresetModel preset, int autoNum) {
        // Find tagProcessors if tagProcessors list is null
        if (tagProcessors == null) {
            tagProcessors = new ArrayList<>();
            ServiceLoader<ITagProcessor> tagProcessorServiceLoader = ServiceLoader.load(ITagProcessor.class);
            for (ITagProcessor tagProcessor : tagProcessorServiceLoader) {
                tagProcessors.add(tagProcessor);
            }
        }

        // Update tag processors if they are initialized to the wrong preset
        if (!preset.equals(lastPreset)) {
            for (ITagProcessor tagProcessor : tagProcessors) {
                tagProcessor.init(preset, autoNum);
            }
            lastPreset = preset;
        }

        // run the tag processors on the preset and video file
        String name = preset.getVideoName();
        String description = preset.getVideoDescription();
        List<String> videoTags = preset.getVideoTags();

        for (ITagProcessor processor : tagProcessors) {
            name = processor.processTitle(name, videoFile);
            description = processor.processDescription(description, videoFile);
            videoTags = processor.processTags(videoTags, videoFile);
        }

        NewVideoUploadModel upload = new NewVideoUploadModel();
        upload.setVideoName(name);
        upload.setVideoDescription(description);
        upload.setVisibility(preset.getVisibility());
        upload.setVideoTags(videoTags);
        upload.setSelectedPlaylist(preset.getSelectedPlaylist());
        upload.setSelectedCategory(preset.getSelectedCategory());
        upload.setTellSubs(preset.isTellSubs());
        upload.setMadeForKids(preset.isMadeForKids());
        upload.setThumbnailPath(preset.getThumbnailPath());
        upload.setVideoFile(videoFile);

        return upload;
    }
}
