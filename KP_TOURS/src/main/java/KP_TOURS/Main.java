package KP_TOURS;

import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.db.DBInit;
import KP_TOURS.repository.TripRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        DBInit.initialize();

        TripRepository repository =
                new TripRepository();

        TripCacheManager.initialize(
                repository.findAll()
        );

        Scene scene =
                new Scene(
                        DashboardView.getView(),
                        1600,
                        900
                );

        scene.getStylesheets().add(
                getClass()
                        .getResource("/css/app.css")
                        .toExternalForm()
        );

        stage.setTitle("AK Technologies ");

        stage.setScene(scene);

        stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }
}