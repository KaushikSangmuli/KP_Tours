package KP_TOURS.ui.accounts;

import KP_TOURS.model.Account;
import KP_TOURS.repository.AccountRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AccountsListView {

    public static TableView<Account> table;
    private static TextField searchField;
    private static Parent cachedView;

    public static Parent getView() {

        if (cachedView != null) {
            refreshTable(); // refresh data without rebuilding UI
            return cachedView;
        }

        VBox root = new VBox(22);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("main-content");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Accounts List");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search accounts...");
        searchField.getStyleClass().add("premium-search");
        searchField.setPrefWidth(280);

        header.getChildren().addAll(title, spacer, searchField);

        table = new TableView<>();
        table.getStyleClass().add("premium-table");
        table.setFocusTraversable(true);

        TableColumn<Account, String> accountNoCol = new TableColumn<>("Account No");
        accountNoCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAccountNo())
        );

        TableColumn<Account, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getName())
        );

        TableColumn<Account, String> groupCol = new TableColumn<>("Group");
        groupCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAccountGroup())
        );

        TableColumn<Account, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCity())
        );

        TableColumn<Account, String> phoneCol = new TableColumn<>("Phone No");
        phoneCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPhoneNo())
        );

        TableColumn<Account, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEmail())
        );
        TableColumn<Account, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(260);

        actionCol.setCellFactory(param -> new TableCell<>() {

            private final Button viewBtn   = new Button("View");
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            private final HBox box = new HBox(8, viewBtn, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER_LEFT);

                viewBtn.getStyleClass().add("table-action-button");
                editBtn.getStyleClass().add("table-action-button");
                deleteBtn.getStyleClass().add("table-action-button");

                viewBtn.setOnAction(e -> {
                    Account account = getTableView().getItems().get(getIndex());
                    showAccountDetailsPopup(account);
                    Platform.runLater(() -> table.requestFocus());
                });

                editBtn.setOnAction(e -> {
                    Account account = getTableView().getItems().get(getIndex());
                    openEditDialog(account);
                });

                deleteBtn.setOnAction(e -> {
                    Account account = getTableView().getItems().get(getIndex());
                    deleteAccount(account, table);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        accountNoCol.setPrefWidth(130);
        nameCol.setPrefWidth(220);
        groupCol.setPrefWidth(130);
        cityCol.setPrefWidth(150);
        phoneCol.setPrefWidth(150);
        emailCol.setPrefWidth(240);

        table.getColumns().addAll(
                accountNoCol,
                nameCol,
                groupCol,
                cityCol,
                phoneCol,
                emailCol,
                actionCol
        );

        AccountRepository repository = new AccountRepository();

        table.setItems(
                FXCollections.observableArrayList(
                        repository.findAll()
                )
        );

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                table.setItems(FXCollections.observableArrayList(repository.findAll()));
                return;
            }
            table.setItems(FXCollections.observableArrayList(repository.searchByName(newVal)));
        });

        // ✅ ESC on search field → focus table
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                table.requestFocus();
                if (!table.getItems().isEmpty()) {
                    table.getSelectionModel().selectFirst();
                }
            }
        });

        // ✅ Table keyboard handling
        table.setOnKeyPressed(e -> {
            switch (e.getCode()) {

                case ENTER -> {
                    e.consume();
                    Account selected = table.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        showAccountDetailsPopup(selected);
                        // after popup closes, focus returns to table automatically
                        Platform.runLater(() -> table.requestFocus());
                    }
                }

                case ESCAPE -> {
                    e.consume();
                    // ✅ ESC → back to AccountView, focus nameField
                    cachedView = null; // clear so fresh data loads next time if needed
                    DashboardView.loadScreen(AccountView.getView());
                    Platform.runLater(() -> AccountView.focusNameField());
                }
            }
        });

        VBox panel = new VBox(16, table);
        panel.getStyleClass().add("premium-panel");

        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(panel, Priority.ALWAYS);

        root.getChildren().addAll(header, panel);

        cachedView = root;
        return cachedView;
    }

    // ✅ Called on return visits to refresh data without rebuilding UI
    private static void refreshTable() {
        AccountRepository repository = new AccountRepository();
        table.setItems(FXCollections.observableArrayList(repository.findAll()));
        searchField.clear();
    }

    public static javafx.scene.Node getFocusTarget() {
        return table;
    }

    // ✅ Account details as a proper popup with ESC to close
    private static void showAccountDetailsPopup(Account account) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Account Details - " + account.getName());

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("trip-form-root");

        Label heading = new Label(account.getName());
        heading.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        GridPane details = new GridPane();
        details.setHgap(16);
        details.setVgap(10);

        details.add(detailLabel("Account No"),  0, 0); details.add(detailValue(account.getAccountNo()),    1, 0);
        details.add(detailLabel("Group"),        0, 1); details.add(detailValue(account.getAccountGroup()), 1, 1);
        details.add(detailLabel("City"),         0, 2); details.add(detailValue(account.getCity()),         1, 2);
        details.add(detailLabel("Phone No"),     0, 3); details.add(detailValue(account.getPhoneNo()),      1, 3);
        details.add(detailLabel("Email"),        0, 4); details.add(detailValue(account.getEmail()),        1, 4);
        details.add(detailLabel("Address"),      0, 5); details.add(detailValue(account.getAddress()),      1, 5);

        Button closeBtn = new Button("Close  [ESC]");
        closeBtn.getStyleClass().add("secondary-button");
        closeBtn.setOnAction(e -> stage.close());

        HBox footer = new HBox(closeBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(heading, details, footer);

        Scene scene = new Scene(root, 440, 340);
        scene.getStylesheets().add(
                AccountsListView.class
                        .getResource("/css/app.css")
                        .toExternalForm()
        );

        // ✅ ESC closes popup
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                stage.close();
            }
        });

        stage.setScene(scene);
        stage.showAndWait(); // blocks — table gets focus after this returns
    }

    private static Label detailLabel(String text) {
        Label l = new Label(text + ":");
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }

    private static Label detailValue(String text) {
        Label l = new Label(safe(text));
        l.setStyle("-fx-font-size: 13px;");
        return l;
    }

    private static void deleteAccount(Account account, TableView<Account> table) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Account");
        confirm.setHeaderText("Are you sure you want to delete this account?");
        confirm.setContentText(account.getName());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                AccountRepository repository = new AccountRepository();
                boolean deleted = repository.delete(account.getUuid());
                if (deleted) {
                    table.getItems().remove(account);
                }
            }
        });
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static void openEditDialog(Account account) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Account");

        TextField nameField  = new TextField(account.getName());
        TextField cityField  = new TextField(account.getCity());
        TextField phoneField = new TextField(account.getPhoneNo());
        TextField emailField = new TextField(account.getEmail());

        TextArea addressArea = new TextArea(account.getAddress());
        addressArea.setPrefRowCount(3);

        ComboBox<String> groupBox = new ComboBox<>(
                FXCollections.observableArrayList("Creditor", "Debtor")
        );
        groupBox.setValue(account.getAccountGroup());

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        form.add(new Label("Name"),     0, 0); form.add(nameField,  1, 0);
        form.add(new Label("City"),     0, 1); form.add(cityField,  1, 1);
        form.add(new Label("Phone No"), 0, 2); form.add(phoneField, 1, 2);
        form.add(new Label("Email"),    0, 3); form.add(emailField, 1, 3);
        form.add(new Label("Group"),    0, 4); form.add(groupBox,   1, 4);
        form.add(new Label("Address"),  0, 5); form.add(addressArea,1, 5);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {

                account.setName(nameField.getText().trim());
                account.setCity(cityField.getText().trim());
                account.setPhoneNo(phoneField.getText().trim());
                account.setEmail(emailField.getText().trim());
                account.setAddress(addressArea.getText().trim());
                account.setAccountGroup(groupBox.getValue());

                AccountRepository repository = new AccountRepository();
                boolean updated = repository.update(account);

                if (updated) {
                    refreshTable();
                }
            }
        });
    }
}