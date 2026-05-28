package KP_TOURS.ui.settings;

import KP_TOURS.backup.BackupManager;
import KP_TOURS.repository.SettingsRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SettingsView {

    private SettingsView() {
    }

    public static Parent getView() {

        VBox root = new VBox(22);

        root.setPadding(new Insets(24));

        root.getStyleClass().add("main-content");

        Label title = new Label("Settings");
        title.getStyleClass().add("section-title");

        VBox businessCard = new VBox(18);
        businessCard.getStyleClass().add("premium-panel");

        Label businessTitle = new Label("Business Settings");
        businessTitle.getStyleClass().add("ledger-form-title");

        Label businessSubtitle = new Label(
                "Manage business name and application branding."
        );
        businessSubtitle.getStyleClass().add("section-subtitle");

        GridPane form = new GridPane();

        form.setHgap(14);
        form.setVgap(14);

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(150);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(
                labelCol,
                fieldCol
        );

        Label businessNameLabel =
                new Label("Business Name");

        businessNameLabel.getStyleClass().add("form-label");

        TextField businessNameField =
                new TextField();

        businessNameField.setPromptText(
                "Enter business name"
        );

        businessNameField.getStyleClass()
                .add("premium-input");

        businessNameField.setMaxWidth(Double.MAX_VALUE);

        SettingsRepository repository =
                new SettingsRepository();

        String savedName =
                repository.getValue(
                        "business_name",
                        "Admin"
                );

        businessNameField.setText(savedName);

        form.add(
                businessNameLabel,
                0,
                0
        );

        form.add(
                businessNameField,
                1,
                0
        );

        HBox actions = new HBox(12);

        actions.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn =
                new Button("Save Settings");

        saveBtn.getStyleClass().add("primary-button");

        saveBtn.setOnAction(e -> {

            String name =
                    businessNameField.getText().trim();

            boolean saved =
                    repository.saveOrUpdate(
                            "business_name",
                            name
                    );

            if (saved) {

                AppSettings.setBusinessName(name);

                Alert alert =
                        new Alert(Alert.AlertType.INFORMATION);

                alert.setContentText(
                        "Settings saved successfully"
                );

                alert.showAndWait();
            }
        });

        actions.getChildren().add(saveBtn);

        businessCard.getChildren().addAll(
                businessTitle,
                businessSubtitle,
                form,
                actions
        );

        VBox backupCard = new VBox(18);

        backupCard.getStyleClass().add("premium-panel");

        Label backupTitle =
                new Label("Backup & Restore");

        backupTitle.getStyleClass().add("ledger-form-title");

        Label backupSubtitle =
                new Label(
                        "Manage application backups and restore data."
                );

        backupSubtitle.getStyleClass()
                .add("section-subtitle");

        HBox backupActions = new HBox(12);

        Button backupBtn =
                new Button("Backup Now");

        backupBtn.getStyleClass().add("secondary-button");

        backupBtn.setOnAction(e ->
                BackupManager.createBackup()
        );

        Button restoreBtn =
                new Button("Restore Backup");

        restoreBtn.getStyleClass().add("secondary-button");

        restoreBtn.setOnAction(e ->
                BackupManager.restoreBackup()
        );

        backupActions.getChildren().addAll(
                backupBtn,
                restoreBtn
        );

        backupCard.getChildren().addAll(
                backupTitle,
                backupSubtitle,
                backupActions
        );

        root.getChildren().addAll(
                title,
                businessCard,
                backupCard
        );

        return root;
    }
}