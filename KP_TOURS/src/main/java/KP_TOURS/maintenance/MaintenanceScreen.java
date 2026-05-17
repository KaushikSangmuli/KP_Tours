package KP_TOURS.maintenance;

import KP_TOURS.ui.dashboard.DashboardView;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MaintenanceScreen {

    public static void show(Stage stage) {

        // 🔹 Title
        Label title = new Label("We'll Be Back Soon");
        title.getStyleClass().add("title");

        // 🔹 Subtitle
        Label subtitle = new Label(
                "Our system is under maintenance.\nPlease enter access key to continue."
        );
        subtitle.getStyleClass().add("subtitle");

        // 🔹 Input
        PasswordField keyField = new PasswordField();
        keyField.setPromptText("Enter Maintenance Key");

        Label message = new Label();
        message.getStyleClass().add("error-text");

        Button submitBtn = new Button("Unlock Access");

        submitBtn.setOnAction(e -> handleUnlock(keyField, message, stage));
        keyField.setOnAction(e -> handleUnlock(keyField, message, stage));

        // 🔥 Animated dots loader
        Label loader = new Label("Loading");
        loader.getStyleClass().add("loader");

        Timeline dots = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> loader.setText("Loading.")),
                new KeyFrame(Duration.seconds(1), e -> loader.setText("Loading..")),
                new KeyFrame(Duration.seconds(1.5), e -> loader.setText("Loading..."))
        );
        dots.setCycleCount(Animation.INDEFINITE);
        dots.play();

        // 🔹 Glass Card
        VBox card = new VBox(15, title, subtitle, loader, keyField, submitBtn, message);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("glass-card");

        // 🔥 GIF Background
        Image gif = new Image(
                MaintenanceScreen.class.getResource("/animations/maintenance.gif").toExternalForm()
        );

        ImageView bgGif = new ImageView(gif);
        bgGif.setPreserveRatio(false);
        bgGif.setOpacity(0.6);
        bgGif.setEffect(new GaussianBlur(25));

        // 🔥 Fullscreen Responsive Background
        bgGif.fitWidthProperty().bind(stage.widthProperty());
        bgGif.fitHeightProperty().bind(stage.heightProperty());

        // 🔥 Overlay
        Pane overlay = new Pane();
        overlay.getStyleClass().add("overlay");

        overlay.prefWidthProperty().bind(stage.widthProperty());
        overlay.prefHeightProperty().bind(stage.heightProperty());

        // 🔹 Root Layout (NO TITLE BAR)
        StackPane root = new StackPane(bgGif, overlay, card);
        root.setAlignment(Pos.CENTER);

        // 🔹 Scene
        Scene scene = new Scene(root, 900, 550);

        scene.getStylesheets().add(
                MaintenanceScreen.class.getResource("/css/maintenance.css").toExternalForm()
        );

        // 🔥 Entry Animation
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.seconds(1), card);
        slide.setFromY(50);
        slide.setToY(0);

        fade.play();
        slide.play();

        stage.setScene(scene);
        stage.show();

        keyField.requestFocus();
    }

    private static void handleUnlock(PasswordField keyField, Label message, Stage stage) {

        if ("7000705523".equals(keyField.getText())) {

            message.setText("Access Granted");

            Scene dashboardScene = new Scene(
                    DashboardView.getView(),
                    1500,
                    900
            );

            dashboardScene.getStylesheets().add(
                    MaintenanceScreen.class
                            .getResource("/css/app.css")
                            .toExternalForm()
            );

            stage.setScene(dashboardScene);

        } else {

            message.setText("Invalid Key");
        }
    }
}