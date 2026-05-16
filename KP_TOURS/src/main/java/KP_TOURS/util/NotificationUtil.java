package KP_TOURS.util;


import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NotificationUtil {

    public static void showSuccess(String message) {
        show(message, "#16a34a");
    }

    public static void showError(String message) {
        show(message, "#dc2626");
    }

    public static void showInfo(String message) {
        show(message, "#2563eb");
    }

    private static void show(
            String message,
            String color
    ) {

        Stage stage = getActiveStage();

        if (stage == null) {
            return;
        }

        Popup popup = new Popup();

        Label label = new Label(message);

        label.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;"
        );

        StackPane root = new StackPane(label);

        root.setPadding(new Insets(14, 20, 14, 20));

        root.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0.2, 0, 4);"
        );

        root.setOpacity(0);

        popup.getContent().add(root);

        popup.show(stage);

        root.applyCss();
        root.layout();

        double x =
                stage.getX()
                        + stage.getWidth()
                        - root.getWidth()
                        - 30;

        double y =
                stage.getY()
                        + 40;

        popup.setX(x);
        popup.setY(y);

        FadeTransition fadeIn =
                new FadeTransition(Duration.millis(250), root);

        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn =
                new TranslateTransition(Duration.millis(250), root);

        slideIn.setFromX(40);
        slideIn.setToX(0);

        fadeIn.play();
        slideIn.play();

        PauseTransition wait =
                new PauseTransition(Duration.seconds(3));

        wait.setOnFinished(e -> {

            FadeTransition fadeOut =
                    new FadeTransition(Duration.millis(250), root);

            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            fadeOut.setOnFinished(event ->
                    popup.hide()
            );

            fadeOut.play();
        });

        wait.play();
    }

    private static Stage getActiveStage() {

        for (javafx.stage.Window window : Stage.getWindows()) {

            if (window.isShowing()) {
                return (Stage) window;
            }
        }

        return null;
    }
}