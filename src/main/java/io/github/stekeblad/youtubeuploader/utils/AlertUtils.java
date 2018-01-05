package io.github.stekeblad.youtubeuploader.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;

public class AlertUtils {

    /**
     * Convenient method for creating a @code{Alert} dialog with a single CLOSE button
     * @param header Header to display in window title
     * @param content The message to display to the user
     * @return a Alert ready to display or continue to modify
     */
    public static Alert simpleClose(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(ButtonType.CLOSE);
        return alert;
    }

    /**
     * Convenient method for creating a @code{Alert} dialog with a YES and a NO button.
     * To set what to do on the buttons you can do like this:
     * <pre>{@code alert.showAndWait().ifPresent(response -> {
     *     if (response == ButtonType.YES) {
     *         doOnYes();
     *     } else if(response == ButtonType.NO) {
     *         doOnNo();
     *     }
     * });}</pre>
     *
     * @param header Header to display in window title
     * @param content The message to display to the user
     * @return a Alert that needs actions bound to the buttons but is otherwise ready to be shown
     */
    public static Alert yesNo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alert;
    }
}
