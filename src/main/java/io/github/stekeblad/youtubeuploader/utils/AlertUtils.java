package io.github.stekeblad.youtubeuploader.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;

import java.util.Optional;

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
     * <pre>{@code
     * Optional<ButtonType> buttonChoice = alert.showAndWait();
     * if (buttonChoice.isPresent()) {
     *     if (buttonChoice.get() == ButtonType.YES) {
     *         doOnYes();
     *     } else { // ButtonType.NO or Closed with [X] button
     *         doOnNo();
     *     }
     * }
     * </pre>
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

    public static String threeButtons(String header, String content, String btn1Text, String btn2Text, String btn3Text) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(header);
        alert.setContentText(content);
        alert.getButtonTypes().addAll(new ButtonType(btn1Text), new ButtonType(btn2Text), new ButtonType(btn3Text));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            return result.get().getText();
        } else {
            return null;
        }
    }
}
