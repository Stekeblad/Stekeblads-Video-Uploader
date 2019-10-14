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
        //ensureOnScreen();
        //ensureValidDimensions();




/*
        // get information about how the saved window dimension and location map to the current screen setup
        Screen currentScreen = Utils.getScreenForRectangle(
                new Rectangle2D(points.getX(), points.getY(), points.getWidth(), points.getHeight()));
        Rectangle2D windowBounds = currentScreen.getVisualBounds();

        // Is window outside screen?
        if (points.getX() < windowBounds.getMinX() || (points.getX() + points.getWidth()) > windowBounds.getMaxX()
                || points.getY() < windowBounds.getMinY() || (points.getY() + points.getHeight()) > windowBounds.getMaxY()) {
            points = new WindowFrame(windowBounds.getMinX(), windowBounds.getMinY(), points.getWidth(), points.getHeight());
        }

        // Is window larger than screen?
        if ((points.getX() + points.getWidth()) > windowBounds.getMaxX()
                || (points.getY() + points.getHeight()) > windowBounds.getMaxY()) {
            // make it fit to screen
            points = new WindowFrame(points.getX(), points.getY(),windowBounds.getWidth(), windowBounds.getHeight());
        }
*/
        Scene scene = new Scene(root, points.width, points.height);
        setScene(scene);
        setX(points.x);
        setY(points.y);

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
            setOnCloseRequest(event -> { // true == close && false == doNotClose
                if (cont.onWindowClose()) {
                    WindowFrame toSave = new WindowFrame(getX(), getY(), getWidth(), getHeight());
                    ConfigManager.INSTANCE.setWindowRectangle(myWindowPropertyName, toSave);
                } else {
                    event.consume();
                }
            });

            cont.myInit();
        }
    }
}
