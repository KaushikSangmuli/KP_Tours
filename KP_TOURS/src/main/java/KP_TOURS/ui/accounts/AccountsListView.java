package KP_TOURS.ui.accounts;

import KP_TOURS.model.Account;
import KP_TOURS.repository.AccountRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import static KP_TOURS.ui.trip.TripFormDialog.openEditDialog;

public class AccountsListView {

    public static Parent getView() {

        VBox root = new VBox(22);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("main-content");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Accounts List");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search accounts...");
        searchField.getStyleClass().add("premium-search");
        searchField.setPrefWidth(280);

        header.getChildren().addAll(title, spacer, searchField);

        TableView<Account> table = new TableView<>();
        table.getStyleClass().add("premium-table");

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

            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            private final HBox box = new HBox(8, viewBtn, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER_LEFT);

                viewBtn.getStyleClass().add("table-action-button");
                editBtn.getStyleClass().add("table-action-button");
                deleteBtn.getStyleClass().add("table-action-button");

                viewBtn.setOnAction(e -> {
                    Account account = getTableView().getItems().get(getIndex());
                    showAccountDetails(account);
                });

                editBtn.setOnAction(e -> {
                    Account account = getTableView().getItems().get(getIndex());
                    openEditDialog(account, table);
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
                table.setItems(
                        FXCollections.observableArrayList(
                                repository.findAll()
                        )
                );
                return;
            }

            table.setItems(
                    FXCollections.observableArrayList(
                            repository.searchByName(newVal)
                    )
            );
        });

        VBox panel = new VBox(16, table);
        panel.getStyleClass().add("premium-panel");

        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(panel, Priority.ALWAYS);

        root.getChildren().addAll(header, panel);

        return root;
    }

    private static void showAccountDetails(Account account) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Account Details");
        alert.setHeaderText(account.getName());

        alert.setContentText(
                "Account No: " + safe(account.getAccountNo()) + "\n" +
                        "Group: " + safe(account.getAccountGroup()) + "\n" +
                        "City: " + safe(account.getCity()) + "\n" +
                        "Phone No: " + safe(account.getPhoneNo()) + "\n" +
                        "Email: " + safe(account.getEmail()) + "\n" +
                        "Address: " + safe(account.getAddress())
        );

        alert.showAndWait();
    }

    private static void openEditDialog(Account account, TableView<Account> table) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Account");

        TextField nameField = new TextField(account.getName());
        TextField cityField = new TextField(account.getCity());
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

        form.add(new Label("Name"), 0, 0);
        form.add(nameField, 1, 0);

        form.add(new Label("City"), 0, 1);
        form.add(cityField, 1, 1);

        form.add(new Label("Phone No"), 0, 2);
        form.add(phoneField, 1, 2);

        form.add(new Label("Email"), 0, 3);
        form.add(emailField, 1, 3);

        form.add(new Label("Group"), 0, 4);
        form.add(groupBox, 1, 4);

        form.add(new Label("Address"), 0, 5);
        form.add(addressArea, 1, 5);

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
                    table.setItems(
                            FXCollections.observableArrayList(
                                    repository.findAll()
                            )
                    );
                }
            }
        });
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
        return value == null || value.isBlank()
                ? "-"
                : value;
    }
}