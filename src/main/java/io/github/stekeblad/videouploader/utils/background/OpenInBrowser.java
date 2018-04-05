package io.github.stekeblad.videouploader.utils.background;

import javafx.concurrent.Task;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Attempts to open a web page in the user's default browser by first creating a new thread and opening it from there.
 * Calling Desktop.getDesktop().browse(...) on the UI thread does not work on all systems, on some it just fails silently.
 */
public class OpenInBrowser {

    /**
     * Opens uri in the user's default browser or throws a Exception while trying
     *
     * @param uri the address to the web page that should be opened
     * @param ueh A UncaughtExceptionHandler to call if an Exception is thrown, can be null
     */
    public static void openInBrowser(URI uri, Thread.UncaughtExceptionHandler ueh) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(uri);
                    return null;
                } else {
                    throw new UnsupportedOperationException("Desktop not supported");
                }
            }
        };
        Thread thread = new Thread(task);
        thread.setUncaughtExceptionHandler(ueh);
        thread.start();
    }

    /**
     * Attempts to open uri in the user's default browser.
     * If a error occurs this method fails silently. (No return value or Exception)
     *
     * @param uri The address of a web page to open
     */
    public static void openInBrowser(String uri) {
        try {
            openInBrowser(new URI(uri), null);
        } catch (URISyntaxException e) {
            System.err.println("OpenInBrowser: URISyntaxException when opening \"" + uri + "\"");
        }
    }
}
