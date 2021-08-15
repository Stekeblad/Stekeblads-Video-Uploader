package io.github.stekeblad.videouploader.extensions.jfx;

public interface IWindowController {

    /**
     * Called as a late initialization event shortly before the window is displayed to the user
     */
    void myInit();

    /**
     * Do something when window close is triggered.
     *
     * @return true to allow the window to be closed, false if it should remain open
     */
    boolean onWindowClose();
}
