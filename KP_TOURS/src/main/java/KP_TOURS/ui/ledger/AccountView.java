package KP_TOURS.ui.ledger;

import KP_TOURS.model.Account;
import KP_TOURS.repository.AccountRepository;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AccountView {

    public static Parent getView() {

        VBox root = new VBox(22);
        root.setFillWidth(true);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("main-content");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("A/C Ledger");
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

        TextField nameField = input("Enter account name");
        TextField cityField = input("Enter city");
        TextField phoneField = input("Enter phone number");
        TextField emailField = input("Enter email address");

        TextArea addressArea = new TextArea();
        addressArea.setPromptText("Enter full address");
        addressArea.setPrefRowCount(3);
        addressArea.getStyleClass().add("premium-text-area");

        ComboBox<String> groupBox = new ComboBox<>(
                FXCollections.observableArrayList(
                        "Creditor",
                        "Debtor"
                )
        );
        groupBox.setPromptText("Select account group");
        groupBox.setMaxWidth(Double.MAX_VALUE);
        groupBox.getStyleClass().add("premium-combo");

        form.add(label("Name"), 0, 0);
        form.add(nameField, 1, 0);

        form.add(label("City"), 2, 0);
        form.add(cityField, 3, 0);

        form.add(label("Phone No"), 0, 1);
        form.add(phoneField, 1, 1);

        form.add(label("Email"), 2, 1);
        form.add(emailField, 3, 1);

        form.add(label("Account Group"), 0, 2);
        form.add(groupBox, 1, 2);

        form.add(label("Address"), 0, 3);
        form.add(addressArea, 1, 3, 3, 1);

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

        form.getColumnConstraints().addAll(
                labelCol1,
                fieldCol1,
                labelCol2,
                fieldCol2
        );

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewAccountBtn = new Button("View Accounts");
        viewAccountBtn.getStyleClass().add("secondary-button");

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("secondary-button");

        Button saveBtn = new Button("Save Account");
        saveBtn.getStyleClass().add("primary-button");

        Button showListBtn = new Button("Show List");
        showListBtn.getStyleClass().add("secondary-button");

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

            account.setName(
                    nameField.getText().trim()
            );

            account.setAddress(
                    addressArea.getText().trim()
            );

            account.setCity(
                    cityField.getText().trim()
            );

            account.setPhoneNo(
                    phoneField.getText().trim()
            );

            account.setEmail(
                    emailField.getText().trim()
            );

            account.setAccountGroup(
                    groupBox.getValue()
            );


            boolean saved =
                    repository.save(account);

            if (saved) {

                alert("Account saved successfully");

                nameField.clear();
                cityField.clear();
                phoneField.clear();
                emailField.clear();
                addressArea.clear();

                groupBox.getSelectionModel()
                        .clearSelection();

            } else {

                alert("Failed to save account");
            }
        });

        actions.getChildren().addAll(
                viewAccountBtn,
                showListBtn,
                clearBtn,
                saveBtn
        );

        card.getChildren().addAll(
                formTitle,
                formSubtitle,
                form,
                actions
        );

        VBox.setVgrow(card, Priority.ALWAYS);

        root.getChildren().addAll(header, card, summaryGrid);

        for (int i = 0; i < 4; i++) {

            ColumnConstraints col = new ColumnConstraints();

            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);

            summaryGrid.getColumnConstraints().add(col);
        }

        return root;
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
}