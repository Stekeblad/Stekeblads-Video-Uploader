package io.github.stekeblad.videouploader.jfxExtension;

import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.Constants;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MyStage extends Stage {

    private final String myWindowPropertyName;

    public MyStage(String windowPropertyName) {
        myWindowPropertyName = windowPropertyName;
    }

    public void makeScene(Parent root, WindowDimensionsRestriction dimensions) {
        ConfigManager configManager = ConfigManager.INSTANCE;
        WindowFrame points = configManager.getWindowRectangle(myWindowPropertyName);
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
        if (controller instanceof IWindowController) {
            IWindowController cont = (IWindowController) controller;
            setOnCloseRequest(event -> {
                if (cont.onWindowClose()) { // true == close && false == doNotClose
                    WindowFrame toSave = new WindowFrame(getX(), getY(), getWidth(), getHeight());
                    ConfigManager.INSTANCE.setWindowRectangle(myWindowPropertyName, toSave);
                } else {
                    event.consume();
                }

                // Make sure all settings is saved on exit do not save when not needed
                if (myWindowPropertyName.equals(Constants.WindowPropertyNames.MAIN)) {
                    ConfigManager.INSTANCE.saveSettings();
                }
            });
            cont.myInit();
        }
    }
}