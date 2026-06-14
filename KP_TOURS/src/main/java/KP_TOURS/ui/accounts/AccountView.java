package KP_TOURS.ui.accounts;

import KP_TOURS.model.Account;
import KP_TOURS.repository.AccountRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

import static KP_TOURS.ui.accounts.AccountsListView.table;

public class AccountView {

    private static Account selectedViewAccount;
    public static TextField nameField;
    private static Parent cachedView;

    public static Parent getView() {

        if (cachedView != null) {
            return cachedView;
        }

        VBox root = new VBox(22);
        root.setFillWidth(true);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("main-content");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Account");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer);

        VBox card = new VBox(20);
        card.getStyleClass().add("premium-panel");
        card.setMaxWidth(Double.MAX_VALUE);

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(16);
        summaryGrid.setVgap(16);
        summaryGrid.setMaxWidth(Double.MAX_VALUE);

        AccountRepository repository = new AccountRepository();

        summaryGrid.add(accountCard("Total Accounts", String.valueOf(repository.countAll())), 0, 0);
        summaryGrid.add(accountCard("Creditors", String.valueOf(repository.countByGroup("Creditor"))), 1, 0);
        summaryGrid.add(accountCard("Debtors", String.valueOf(repository.countByGroup("Debtor"))), 2, 0);
        summaryGrid.add(accountCard("Recently Added", "Latest accounts"), 3, 0);

        Label formTitle = new Label("Create Account");
        formTitle.getStyleClass().add("ledger-form-title");

        Label formSubtitle = new Label("Add customer, supplier, creditor or debtor details.");
        formSubtitle.getStyleClass().add("section-subtitle");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);
        form.setMaxWidth(Double.MAX_VALUE);

        nameField = input("Enter account name");
        TextField cityField   = input("Enter city");
        TextField phoneField  = input("Enter phone number");
        TextField emailField  = input("Enter email address");

        Button saveBtn        = new Button("Save Account");
        Button viewAccountBtn = new Button("View Accounts");
        Button clearBtn       = new Button("Clear");
        Button showListBtn    = new Button("Show List");

        saveBtn.getStyleClass().add("primary-button");
        viewAccountBtn.getStyleClass().add("secondary-button");
        clearBtn.getStyleClass().add("secondary-button");
        showListBtn.getStyleClass().add("secondary-button");

        TextArea addressArea = new TextArea();
        addressArea.setPromptText("Enter full address");
        addressArea.setPrefRowCount(3);
        addressArea.getStyleClass().add("premium-text-area");

        ComboBox<String> groupBox = new ComboBox<>(
                FXCollections.observableArrayList("Creditor", "Debtor")
        );
        groupBox.setPromptText("Select account group");
        groupBox.setMaxWidth(Double.MAX_VALUE);
        groupBox.getStyleClass().add("premium-combo");

        // ✅ Load all accounts upfront
        List<Account> allAccounts = repository.findAll();

        // ✅ Custom searchable dropdown using TextField + ListView
        TextField viewSearchField = new TextField();
        viewSearchField.setPromptText("Type account name to view");
        viewSearchField.setMaxWidth(Double.MAX_VALUE);
        viewSearchField.getStyleClass().add("premium-input");

        ListView<Account> viewDropdown = new ListView<>();
        viewDropdown.setMaxHeight(150);
        viewDropdown.setVisible(false);
        viewDropdown.setManaged(false);
        viewDropdown.getStyleClass().add("premium-table");

        viewDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                setText(empty || account == null
                        ? null
                        : account.getName() + " - " + safe(account.getPhoneNo()));
            }
        });

        viewDropdown.setItems(FXCollections.observableArrayList(allAccounts));

        // ✅ Filter as user types
        viewSearchField.textProperty().addListener((obs, oldVal, newVal) -> {

            Account selected = selectedViewAccount;
            if (selected != null && selected.getName().equals(newVal)) return;

            if (newVal == null || newVal.isBlank()) {
                viewDropdown.setItems(FXCollections.observableArrayList(allAccounts));
            } else {
                List<Account> filtered = allAccounts.stream()
                        .filter(a -> a.getName().toLowerCase()
                                .contains(newVal.toLowerCase()))
                        .toList();
                viewDropdown.setItems(FXCollections.observableArrayList(filtered));
            }

            boolean hasItems = !viewDropdown.getItems().isEmpty();
            viewDropdown.setVisible(hasItems);
            viewDropdown.setManaged(hasItems);
        });

        // ✅ Keyboard navigation in search field
        viewSearchField.setOnKeyPressed(e -> {
            switch (e.getCode()) {

                case DOWN -> {
                    e.consume();
                    viewDropdown.setVisible(true);
                    viewDropdown.setManaged(true);
                    int cur = viewDropdown.getSelectionModel().getSelectedIndex();
                    if (cur < viewDropdown.getItems().size() - 1) {
                        viewDropdown.getSelectionModel().select(cur + 1);
                    } else {
                        viewDropdown.getSelectionModel().selectFirst();
                    }
                    viewDropdown.scrollTo(viewDropdown.getSelectionModel().getSelectedIndex());
                }

                case UP -> {
                    e.consume();
                    int cur = viewDropdown.getSelectionModel().getSelectedIndex();
                    if (cur > 0) {
                        viewDropdown.getSelectionModel().select(cur - 1);
                    } else {
                        viewDropdown.getSelectionModel().selectLast();
                    }
                    viewDropdown.scrollTo(viewDropdown.getSelectionModel().getSelectedIndex());
                }

                case ENTER -> {
                    e.consume();
                    Account highlighted = viewDropdown.getSelectionModel().getSelectedItem();
                    if (highlighted != null) {
                        selectedViewAccount = highlighted;
                        viewSearchField.setText(highlighted.getName());
                        viewDropdown.setVisible(false);
                        viewDropdown.setManaged(false);
                    }
                    Platform.runLater(() -> viewAccountBtn.requestFocus());
                }

                case ESCAPE -> {
                    e.consume();
                    viewDropdown.setVisible(false);
                    viewDropdown.setManaged(false);
                    viewSearchField.clear();
                    selectedViewAccount = null;
                    viewDropdown.setItems(FXCollections.observableArrayList(allAccounts));
                    Platform.runLater(() -> nameField.requestFocus());
                }
            }
        });

        // ✅ Click to select from dropdown
        viewDropdown.setOnMouseClicked(e -> {
            Account selected = viewDropdown.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedViewAccount = selected;
                viewSearchField.setText(selected.getName());
                viewDropdown.setVisible(false);
                viewDropdown.setManaged(false);
                Platform.runLater(() -> viewAccountBtn.requestFocus());
            }
        });

        // ✅ Hide dropdown when focus leaves
        viewSearchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && !viewDropdown.isFocused()) {
                Platform.runLater(() -> {
                    if (!viewDropdown.isFocused()) {
                        viewDropdown.setVisible(false);
                        viewDropdown.setManaged(false);
                    }
                });
            }
        });

        VBox viewAccountWrapper = new VBox(0, viewSearchField, viewDropdown);
        viewAccountWrapper.setMaxWidth(Double.MAX_VALUE);

        // ✅ Enter key navigation between fields
        nameField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                cityField.requestFocus();
            }
        });

        cityField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                phoneField.requestFocus();
            }
        });

        phoneField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                emailField.requestFocus();
            }
        });

        emailField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                groupBox.requestFocus();
            }
        });

        groupBox.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                addressArea.requestFocus();
            }
        });

        addressArea.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                saveBtn.requestFocus();
            }
        });

        viewAccountBtn.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                viewAccountBtn.fire();
                Platform.runLater(() -> viewSearchField.requestFocus());
            }
        });

        saveBtn.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Save");
                confirm.setHeaderText("Save Account");
                confirm.setContentText("Are you sure you want to save this account?");

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        saveBtn.fire();
                    } else {
                        Platform.runLater(() -> nameField.requestFocus());
                    }
                });
            }
        });

        showListBtn.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                showListBtn.fire();
            }
        });

        // ✅ Button actions
        clearBtn.setOnAction(e -> {
            nameField.clear();
            cityField.clear();
            phoneField.clear();
            emailField.clear();
            addressArea.clear();
            groupBox.getSelectionModel().clearSelection();
        });

        saveBtn.setOnAction(e -> {

            if (nameField.getText().isBlank()) {
                alert("Name is required");
                return;
            }

            if (groupBox.getValue() == null) {
                alert("Please select account group");
                return;
            }

            Account account = new Account();
            account.setName(nameField.getText().trim());
            account.setAddress(addressArea.getText().trim());
            account.setCity(cityField.getText().trim());
            account.setPhoneNo(phoneField.getText().trim());
            account.setEmail(emailField.getText().trim());
            account.setAccountGroup(groupBox.getValue());

            boolean saved = repository.save(account);

            if (saved) {
                alert("Account saved successfully");
                nameField.clear();
                cityField.clear();
                phoneField.clear();
                emailField.clear();
                addressArea.clear();
                groupBox.getSelectionModel().clearSelection();
            } else {
                alert("Failed to save account");
            }
        });

        showListBtn.setOnAction(e -> {
            DashboardView.loadScreen(AccountsListView.getView());
            Platform.runLater(() ->
                    Platform.runLater(() -> AccountsListView.getFocusTarget().requestFocus())
            );
        });

        viewAccountBtn.setOnAction(e -> {
            if (selectedViewAccount == null) {
                alert("Please select account from dropdown");
                return;
            }
            showAccountDetails(selectedViewAccount);
        });

        // ✅ Form layout
        form.add(label("Name"),          0, 0); form.add(nameField,          1, 0);
        form.add(label("City"),          2, 0); form.add(cityField,          3, 0);
        form.add(label("Phone No"),      0, 1); form.add(phoneField,         1, 1);
        form.add(label("Email"),         2, 1); form.add(emailField,         3, 1);
        form.add(label("Account Group"), 0, 2); form.add(groupBox,           1, 2);
        form.add(label("View Account"),  2, 2); form.add(viewAccountWrapper, 3, 2);
        form.add(label("Address"),       0, 3); form.add(addressArea,        1, 3, 3, 1);

        ColumnConstraints labelCol1 = new ColumnConstraints();
        labelCol1.setMinWidth(110);

        ColumnConstraints fieldCol1 = new ColumnConstraints();
        fieldCol1.setPercentWidth(40);
        fieldCol1.setHgrow(Priority.ALWAYS);

        ColumnConstraints labelCol2 = new ColumnConstraints();
        labelCol2.setMinWidth(110);

        ColumnConstraints fieldCol2 = new ColumnConstraints();
        fieldCol2.setPercentWidth(40);
        fieldCol2.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(labelCol1, fieldCol1, labelCol2, fieldCol2);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getChildren().addAll(viewAccountBtn, showListBtn, clearBtn, saveBtn);

        card.getChildren().addAll(formTitle, formSubtitle, form, actions);
        VBox.setVgrow(card, Priority.ALWAYS);

        root.getChildren().addAll(header, card, summaryGrid);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            summaryGrid.getColumnConstraints().add(col);
        }

        cachedView = root;
        return cachedView;
    }

    private static TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("premium-input");
        return field;
    }

    private static Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }
    private static void alert(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setContentText(message);

        alert.showAndWait();
    }
    private static VBox accountCard(String title, String value) {

        VBox card = new VBox(8);
        card.getStyleClass().add("summary-card");
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("summary-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("summary-value");

        Label subtitle = new Label("Accounts");
        subtitle.getStyleClass().add("summary-subtitle");

        card.getChildren().addAll(titleLabel, valueLabel, subtitle);

        return card;
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

    private static String safe(String value) {
        return value == null || value.isBlank()
                ? "-"
                : value;
    }
    // In each screen e.g. AccountView.java
    public static javafx.scene.Node getFocusTarget() {
        return nameField; // just return the node, don't call requestFocus here
    }


    public static void focusNameField() {
        if (nameField != null) {
            Platform.runLater(() -> nameField.requestFocus());
        }
    }
}