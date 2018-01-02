package io.github.lilahamstern;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmBox {

    // Setting variables
    static boolean answer;

    public static boolean display(String title, String message) {
        // Setting popup window settings and more
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinHeight(150);
        window.setMinWidth(350);
        window.setMaxHeight(150);
        window.setMaxWidth(350);
        Label label = new Label();
        label.setText(message);
        label.setWrapText(true);

        // Creating button yes and no
        Button yesBtn = new Button("Yes");
        Button noBtn = new Button("No");
        // If u press yesbtn this function is called
        yesBtn.setOnAction(e -> {
            answer = true;
            window.close();
        });
        // if u press noBtn this function is called
        noBtn.setOnAction(e -> {
            answer = false;
            window.close();
        });

        // Putting out the buttons and settings some more things for the buttons and window
        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, yesBtn, noBtn);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

        // Return value  true or false
        return answer;
    }
}
