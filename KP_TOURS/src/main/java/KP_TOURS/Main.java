package KP_TOURS;

import KP_TOURS.backup.AutoBackupScheduler;
import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.db.DBInit;
import KP_TOURS.maintenance.MaintenanceAccessManager;
import KP_TOURS.maintenance.MaintenanceScreen;
import KP_TOURS.repository.SettingsRepository;
import KP_TOURS.repository.TripRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import KP_TOURS.ui.settings.AppSettings;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        DBInit.initialize();

        SettingsRepository settingsRepository =
                new SettingsRepository();

        String businessName =
                settingsRepository.getValue(
                        "business_name",
                        "KP Tours"
                );

        AppSettings.setBusinessName(businessName);

        TripRepository repository =
                new TripRepository();

        TripCacheManager.initialize(
                repository.findAll()
        );

        AutoBackupScheduler.start();
        MaintenanceAccessManager.initializeFlagFileIfMissing();

        // =========================================
        // MAINTENANCE DATE CHECK
        // =========================================

        LocalDate today = LocalDate.now();

        boolean showMaintenance =
                today.getDayOfMonth() == 1
                        && today.getMonthValue() == 6
                        && today.getYear() > 2026;

        if (showMaintenance && !MaintenanceAccessManager.isMaintenanceUnlocked()) {

            MaintenanceScreen.show(stage);

            return;
        }

        // =========================================
        // NORMAL DASHBOARD
        // =========================================

        Scene scene =
                new Scene(
                        DashboardView.getView(),
                        1600,
                        900
                );


        DashboardView.attachKeyboard(scene);   // 👈 ADD THIS
        scene.getStylesheets().add(
                getClass()
                        .getResource("/css/app.css")
                        .toExternalForm()
        );

        stage.setTitle("Safar Ease");

        stage.setScene(scene);

        stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }
}