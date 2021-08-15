package io.github.stekeblad.videouploader.extensions.jfx;

import io.github.stekeblad.videouploader.managers.SettingsManager;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.Constants;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;

public class MyStage extends Stage {

    private final String myWindowPropertyName;

    public MyStage(String windowPropertyName) {
        myWindowPropertyName = windowPropertyName;
    }

    public void makeScene(Parent root, WindowDimensionsRestriction dimensions) {
        SettingsManager settingsManager = SettingsManager.getSettingsManager();
        WindowFrame points = settingsManager.getWindowFrame(myWindowPropertyName);
        Scene scene = new Scene(root, points.getWidth(), points.getHeight());
        setScene(scene);
        setX(points.getX());
        setY(points.getY());
        setMinHeight(dimensions.getMinH());
        setMinWidth(dimensions.getMinW());
        setMaxHeight(dimensions.getMaxH());
        setMaxWidth(dimensions.getMaxW());
    }

    /**
     * calls triggerController(controller) and opens the window
     *
     * @param controller a class implementing MyControllerBase
     */
    public void prepareControllerAndShow(Object controller) {
        triggerController(controller);
        show();
    }

    /**
     * calls triggerController(controller), opens the window and wait for it to close
     *
     * @param controller a class implementing MyControllerBase
     */
    public void prepareControllerAndShowAndWait(Object controller) {
        triggerController(controller);
        showAndWait();
    }

    /**
     * Connects the onCloseRequest to the controller's onWindowClose method, set up to save window size and location on
     * close and calls myinit() on the controller.
     * If the given controller does not implement MyControllerBase then this method returns without doing anything
     *
     * @param controller a class implementing MyControllerBase
     */
    private void triggerController(Object controller) {
        SettingsManager settingsManager = SettingsManager.getSettingsManager();
        if (controller instanceof IWindowController) {
            IWindowController cont = (IWindowController) controller;
            setOnCloseRequest(event -> {
                if (cont.onWindowClose()) { // true == close && false == doNotClose
                    WindowFrame toSave = new WindowFrame(getX(), getY(), getWidth(), getHeight());
                    settingsManager.setWindowFrame(myWindowPropertyName, toSave);
                } else {
                    event.consume();
                }

                // Make sure all settings is saved on exit do not save when not needed
                if (myWindowPropertyName.equals(Constants.WindowPropertyNames.MAIN)) {
                    try {
                        settingsManager.saveSettings();
                    } catch (IOException exception) {
                        AlertUtils.exceptionDialog("Saving failed - Stekeblads Video Uploader",
                                "The following error caused the saving of some changes to fail:",
                                exception);
                    }
                }
            });
            cont.myInit();

            // Trick to recover "lost" windows that opened outside the screen
            // press ctrl + alt + shift + c and wish upon some magic
            getScene().setOnKeyPressed((event) -> {
                if (event.getCode() == KeyCode.C && event.isAltDown() && event.isControlDown() && event.isShiftDown()) {
                    centerOnScreen();
                    event.consume();
                }
            });
        }
    }
}