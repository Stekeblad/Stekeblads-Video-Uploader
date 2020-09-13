package io.github.stekeblad.videouploader.updater;

import io.github.stekeblad.videouploader.utils.FileUtils;
import io.github.stekeblad.videouploader.utils.HttpOperations;
import javafx.concurrent.Task;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import static io.github.stekeblad.videouploader.utils.Constants.DATA_DIR;

/**
 * UpdaterCore contains the backend logic for update checking and "installing" of new updates. Its not really installing
 * because Stekeblads Video Uploader does not register anything with the operating system, it keeps all its files in the
 * directory of the jar. The installation is just replacing the old jar with a new.
 * <p>
 * Most of the work is done on a background thread to avoid locking other stuff.
 * <p>
 * Between downloading and applying an update it must pass a verification. The downloaded file from GitHub must match
 * the signature from the update info retrieved from stekeblad.se when performing public key signature validation using
 * the public key embedded in the program (resources/crypto). See {@link CheckSignatureWithTink}
 */
public class UpdaterCore {
    private final Consumer<String> _statusFeed;
    private UpdateInfo _info;
    private Thread checkThread;
    private Thread updateThread;

    /**
     * Constructor for UpdateCore
     *
     * @param statusFeed an String Consumer to send messages about the current update progress. (can be null)
     */
    public UpdaterCore(Consumer<String> statusFeed) {
        _statusFeed = statusFeed;

    }

    /**
     * Sends a interrupt signal to the update threads
     */
    public void abort() {
        if (checkThread != null)
            checkThread.interrupt();

        if (updateThread != null)
            updateThread.interrupt();
    }

    /**
     * Downloads information about the latest available version and compares it to the current version.
     * The method performs the update check in a background thread and returns without waiting for it to finnish.
     * To be notified when the updating finishes you need to provide a callback.
     * If the callback is not null and a newer version is available then the callback will be sent a UpdateInfo with
     * information about the new update. If the callback is not null but no new update is available or the update check
     * failed then a null will be sent to the callback.
     * <p>
     * Update checking is disabled when running in development environment because file paths is very different compared
     * to when running as a jar, you do not want to download the current release when developing the next and
     * {@link UpdaterCore#determineCurrentVersion() the current version file} can not be trusted in development environment.
     *
     * @param callback A consumer to notify when the update check finishes (can be null)
     */
    public void checkForUpdate(Consumer<UpdateInfo> callback) {
        if (!FileUtils.isProductionMode()) {
            acceptStatusFeed("Update check canceled, detected running in development mode");
            return;
        }

        Task<UpdateInfo> newTask = new Task<>() {
            @Override
            // Define what it does
            protected UpdateInfo call() {
                try {
                    acceptStatusFeed("Determining current version...");
                    VersionFormat currentVersion = determineCurrentVersion();
                    if (Thread.interrupted()) {
                        acceptStatusFeed("Aborted");
                        return null;
                    } else if (currentVersion == null) {
                        // If unknown, then we update
                        acceptStatusFeed("Unknown" + System.lineSeparator());
                        currentVersion = new VersionFormat("0.0.1");
                    } else {
                        acceptStatusFeed(currentVersion.toString() + System.lineSeparator());
                    }

                    acceptStatusFeed("Getting update information from server...");
                    _info = fetchLatestVersionInfo();
                    if (Thread.interrupted()) {
                        acceptStatusFeed("Aborted");
                        return null;
                    } else if (_info == null) {
                        acceptStatusFeed("Failed");
                        return null;
                    }
                    acceptStatusFeed("Success" + System.lineSeparator());

                    if (_info.getVersion().compareTo(currentVersion) <= 0) {
                        acceptStatusFeed("No new update available");
                        _info = null;
                        return null;
                    } else {
                        acceptStatusFeed("Update found");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return _info;
            }
        };
        newTask.setOnFailed(event -> {
            if (callback != null) {
                callback.accept(null);
            }
        });
        newTask.setOnSucceeded(event -> {
            if (callback != null) {
                callback.accept(newTask.getValue());

            }
        });
        checkThread = new Thread(newTask);
        checkThread.start();
    }

    /**
     * Downloads, verifies and schedules an update to be "installed"
     * If this instance of UpdaterCore was created with a non-null statusFeed it will receive updates about the
     * update progress. This method launches the updating in a background thread and returns without waiting for it to finnish.
     * To be notified when the updating finishes you need to provide a callback.
     *
     * @param callback a consumer to be notified with true if the updating was a success and false in all other cases. (can be null)
     * @throws IllegalStateException If UpdaterCore.checkForUpdate() has not yet been called or
     *                               the update check did not return a newer version than the current one.
     */
    public void installUpdate(Consumer<Boolean> callback) {
        if (_info == null)
            throw new IllegalStateException("No update available");
        Task<Boolean> newTask = new Task<>() {
            @Override
            // Define what it does
            protected Boolean call() {
                try {
                    acceptStatusFeed(System.lineSeparator() + "Downloading update...");

                    byte[] updateBytes = HttpOperations.getBytes(_info.getUpdateUrl());
                    if (Thread.interrupted()) {
                        acceptStatusFeed("Aborted");
                        return null;
                    } else if (updateBytes == null) {
                        acceptStatusFeed("Failed");
                        return false;
                    }

                    acceptStatusFeed("Done" + System.lineSeparator() +
                            "Verifying download...");
                    byte[] signatureBytes = Base64.getDecoder().decode(_info.getSignature());
                    if (Thread.interrupted()) {
                        acceptStatusFeed("Aborted");
                        return null;
                    } else if (!CheckSignatureWithTink.verifySignature(updateBytes, signatureBytes)) {
                        acceptStatusFeed("Not valid, aborting update");
                        return false;
                    }

                    acceptStatusFeed("Update is valid" + System.lineSeparator() +
                            "Preparing to update...");
                    String filename = "Stekeblads_Video_Uploader-" + _info.getVersion().toString() + ".jar";
                    Path updateFilePath = Paths.get(DATA_DIR, filename);
                    try {
                        Files.write(updateFilePath, updateBytes);
                    } catch (IOException e) {
                        acceptStatusFeed("Failed");
                        return false;
                    }
                    if (Thread.interrupted()) {
                        acceptStatusFeed("Aborted");
                        return null;
                    }
                    scheduleUpdate(updateFilePath);

                    acceptStatusFeed("Ready" + System.lineSeparator() +
                            "Restart to update");
                } catch (Exception ignored) {
                    return false;
                }
                return true;
            }
        };
        newTask.setOnFailed(event -> {
            if (callback != null) {
                callback.accept(false);
            }
        });
        newTask.setOnSucceeded(event -> {
            if (callback != null) {
                callback.accept(newTask.getValue());
            }
        });
        updateThread = new Thread(newTask);
        updateThread.start();
    }

    /**
     * Just performs a null check around _statusFeed before calling _statusFeed.accept(statusText)
     *
     * @param statusText Text to forward to _statusFeed.accept if a status feed exists
     */
    private void acceptStatusFeed(String statusText) {
        if (_statusFeed != null)
            _statusFeed.accept(statusText);
    }

    /**
     * Reads the automatically generated resource file /generated/CurrentVersion.properties and parses its content.
     * The returned version number can then be compared with the version number from the update check to see if a newer
     * version is available. The automatically generated file is created when creating release builds with Gradle and the
     * content (or even the files existence) of the file should not be trusted when running in development environment.
     *
     * @return VersionInfo with the program's current version
     */
    private VersionFormat determineCurrentVersion() {
        try {
            URL versionFileUrl = UpdaterCore.class.getResource("/generated/CurrentVersion.properties");
            if (versionFileUrl == null)
                return null;
            List<String> versionString = FileUtils.getAllResourceLines(versionFileUrl.toURI());
            return new VersionFormat(versionString.get(0));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Contacts the update server and retrieves information about the latest available version.
     *
     * @return A UpdateInfo object with information about the latest available update, or null if the information could
     * not be fetched.
     * @throws JSONException If there is a syntax error in the response from the update server, the data contains a duplicated key
     *                       or if any of the expected keys in the update response is missing.
     */
    private UpdateInfo fetchLatestVersionInfo() throws JSONException {
        String updateInformation = HttpOperations.getString("https://stekeblad.se/latestuploaderversion.json");
        UpdateInfo info = null;
        if (updateInformation != null) {
            JSONObject updateJson = new JSONObject(updateInformation);
            info = new UpdateInfo();
            info.setBody(updateJson.getString("body"));
            info.setHeading(updateJson.getString("heading"));
            info.setUpdateUrl(updateJson.getString("download-url"));
            info.setVersion(updateJson.getString("latest-version"));
            info.setSignature(updateJson.getString("signature"));
        }
        return info;
    }

    /**
     * Adds a Java shutdown hook to replace the current version with the new downloaded one.
     *
     * @param hereIsUpdate The path to there the new version is temporary stored
     * @throws URISyntaxException If the path to the currently executing jar could not be extracted
     */
    private void scheduleUpdate(Path hereIsUpdate) throws URISyntaxException {
        final Path hereIsRunningVersion = new File(UpdaterCore.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.move(hereIsUpdate, hereIsRunningVersion.resolveSibling(hereIsUpdate.getFileName()));
                // Windows locks the file while the program is running, use the ping command as a short delay so it can properly exit and
                // then delete the file. On Linux/Unix/Mac type systems that is not a problem.
                if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
                    Runtime.getRuntime().exec("cmd /c ping localhost -n 3 > nul && del \"" + hereIsRunningVersion.toString() + "\"");
                else
                    Files.deleteIfExists(hereIsRunningVersion);
            } catch (IOException ignored) {
            }
        }));
    }
}
