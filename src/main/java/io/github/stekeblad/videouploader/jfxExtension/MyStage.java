package io.github.stekeblad.videouploader.jfxExtension;

import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.WindowDimensionsRestriction;
import io.github.stekeblad.videouploader.utils.WindowFrame;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MyStage extends Stage {

    private String myWindowPropertyName;

    public MyStage(String windowPropertyName) {
        myWindowPropertyName = windowPropertyName;
    }

    public void makeScene(Parent root, WindowDimensionsRestriction dimensions) {
        ConfigManager configManager = ConfigManager.INSTANCE;
        WindowFrame points = configManager.getWindowRectangle(myWindowPropertyName);
        Scene scene = new Scene(root, points.width, points.height);
        setScene(scene);
        setX(points.x);
        setY(points.y);
        setMinHeight(dimensions.minH);
        setMinWidth(dimensions.minW);
        setMaxHeight(dimensions.maxH);
        setMaxWidth(dimensions.maxW);
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
                if (myWindowPropertyName.equals(ConfigManager.WindowPropertyNames.MAIN)) {
                    ConfigManager.INSTANCE.saveSettings();
                }
            });
            cont.myInit();
        }
    }
}