package KP_TOURS.ui.accountledger;

import KP_TOURS.model.Account;
import KP_TOURS.model.LedgerRow;
import KP_TOURS.model.LedgerSummaryDTO;
import KP_TOURS.repository.AccountLedgerRepository;
import KP_TOURS.repository.AccountRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;

import java.time.LocalDate;
import java.util.List;

public class AccountLedgerView {

    private final AccountRepository accountRepository = new AccountRepository();
    private final AccountLedgerRepository ledgerRepository = new AccountLedgerRepository();

    private TextField accountSearchField;
    private ListView<Account> accountDropdown;
    private Popup dropdownPopup;
    private HBox fromDateField;
    private HBox toDateField;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private Button loadButton;
    private Account selectedAccount;
    private Label outstandingLabel;
    private Label profitLabel;
    private Label totalDebitLabel;
    private Label totalCreditLabel;
    private Label closingBalanceLabel;
    private TableView<LedgerRow> table;

    private static Parent cachedView;
    private static AccountLedgerView instance;

    public static Parent getView() {
        if (cachedView != null) return cachedView;
        instance = new AccountLedgerView();
        cachedView = instance.buildView();
        return cachedView;
    }

    public static javafx.scene.Node getFocusTarget() {
        return instance != null ? instance.accountSearchField : null;
    }

    public static void clearCache() {
        cachedView = null;
        instance = null;
    }

    private Parent buildView() {

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("main-content");

        Label title = new Label("Account Ledger");
        title.getStyleClass().add("section-title");

        HBox filterRow = createFilterRow();
        table = createTable();
        HBox footer = createFooter();

        VBox tableCard = new VBox(10, table, footer);
        tableCard.getStyleClass().add("premium-panel");
        tableCard.setPadding(new Insets(15));

        root.getChildren().addAll(title, filterRow, tableCard);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);

        return scrollPane;
    }

    private HBox createFilterRow() {

        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        List<Account> allAccounts = accountRepository.findAll();

        // ✅ Search field
        accountSearchField = new TextField();
        accountSearchField.setPromptText("Type to search account...");
        accountSearchField.setPrefWidth(280);
        accountSearchField.getStyleClass().add("premium-input");

        // ✅ Dropdown as Popup
        accountDropdown = new ListView<>();
        accountDropdown.setPrefWidth(280);
        accountDropdown.setPrefHeight(150);
        accountDropdown.getStyleClass().add("premium-table");

        accountDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                setText(empty || account == null
                        ? null
                        : account.getName() + " - " + account.getAccountGroup());
            }
        });

        accountDropdown.setItems(FXCollections.observableArrayList(allAccounts));

        dropdownPopup = new Popup();
        dropdownPopup.setAutoHide(true);
        dropdownPopup.setHideOnEscape(true);
        dropdownPopup.getContent().add(accountDropdown);

        // ✅ Show popup below search field
        Runnable showPopup = () -> {
            if (accountDropdown.getItems().isEmpty()) return;
            javafx.geometry.Bounds bounds =
                    accountSearchField.localToScreen(
                            accountSearchField.getBoundsInLocal());
            if (bounds != null && accountSearchField.getScene() != null) {
                dropdownPopup.show(
                        accountSearchField,
                        bounds.getMinX(),
                        bounds.getMaxY() + 2
                );
            }
        };

        Runnable hidePopup = () -> dropdownPopup.hide();

        // ✅ Filter as user types
        accountSearchField.textProperty().addListener((obs, oldVal, newVal) -> {

            if (selectedAccount != null
                    && selectedAccount.getName().equals(newVal)) return;

            if (newVal == null || newVal.isBlank()) {
                accountDropdown.setItems(
                        FXCollections.observableArrayList(allAccounts));
            } else {
                List<Account> filtered = allAccounts.stream()
                        .filter(a -> a.getName().toLowerCase()
                                .contains(newVal.toLowerCase()))
                        .toList();
                accountDropdown.setItems(
                        FXCollections.observableArrayList(filtered));
            }

            if (!accountDropdown.getItems().isEmpty()) {
                showPopup.run();
            } else {
                hidePopup.run();
            }
        });

        // ✅ Keyboard navigation
        accountSearchField.setOnKeyPressed(e -> {
            switch (e.getCode()) {

                case DOWN -> {
                    e.consume();
                    showPopup.run();
                    int cur = accountDropdown.getSelectionModel()
                            .getSelectedIndex();
                    if (cur < accountDropdown.getItems().size() - 1) {
                        accountDropdown.getSelectionModel().select(cur + 1);
                    } else {
                        accountDropdown.getSelectionModel().selectFirst();
                    }
                    accountDropdown.scrollTo(
                            accountDropdown.getSelectionModel()
                                    .getSelectedIndex());
                }

                case UP -> {
                    e.consume();
                    int cur = accountDropdown.getSelectionModel()
                            .getSelectedIndex();
                    if (cur > 0) {
                        accountDropdown.getSelectionModel().select(cur - 1);
                    } else {
                        accountDropdown.getSelectionModel().selectLast();
                    }
                    accountDropdown.scrollTo(
                            accountDropdown.getSelectionModel()
                                    .getSelectedIndex());
                }

                case ENTER -> {
                    e.consume();
                    Account highlighted =
                            accountDropdown.getSelectionModel().getSelectedItem();

                    if (highlighted == null && !accountDropdown.getItems().isEmpty()) {
                        highlighted = accountDropdown.getItems().get(0);
                    }

                    if (highlighted != null) {
                        selectedAccount = highlighted;
                        accountSearchField.setText(highlighted.getName());
                        dropdownPopup.hide();
                    }

                    focusDateField(fromDateField);
                }

                case ESCAPE -> {
                    e.consume();
                    hidePopup.run();
                    accountSearchField.clear();
                    selectedAccount = null;
                    accountDropdown.setItems(
                            FXCollections.observableArrayList(allAccounts));
                }
            }
        });

        // ✅ Click to select from dropdown
        accountDropdown.setOnMouseClicked(e -> {
            Account selected =
                    accountDropdown.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedAccount = selected;
                accountSearchField.setText(selected.getName());
                hidePopup.run();
                focusDateField(fromDateField);
            }
        });

        accountDropdown.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                Account highlighted =
                        accountDropdown.getSelectionModel().getSelectedItem();
                if (highlighted != null) {
                    selectedAccount = highlighted;
                    accountSearchField.setText(highlighted.getName());
                    dropdownPopup.hide();
                    focusDateField(fromDateField);
                }
            }
        });

        // ✅ Hide popup when focus leaves search field
        accountSearchField.focusedProperty().addListener(
                (obs, wasFocused, isFocused) -> {
                    if (!isFocused) {
                        Platform.runLater(() -> {
                            if (!dropdownPopup.isFocused()) {
                                hidePopup.run();
                            }
                        });
                    }
                });

        fromDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        toDatePicker   = new DatePicker(LocalDate.now());

        fromDateField = createSmartDateField(fromDatePicker, true);
        toDateField   = createSmartDateField(toDatePicker, false);

        outstandingLabel = new Label("Outstanding ₹0");
        outstandingLabel.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");

        profitLabel = new Label("Profit ₹0");
        profitLabel.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");

        loadButton = new Button("Load");
        loadButton.getStyleClass().add("primary-button");
        loadButton.setOnAction(e -> loadLedger());

        loadButton.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                loadLedger();
                Platform.runLater(() -> table.requestFocus());
            }
        });

        // ✅ No wrapper needed — Popup floats independently
        row.getChildren().addAll(
                new Label("Account"), accountSearchField,
                new Label("From"),    fromDateField,
                new Label("To"),      toDateField,
                outstandingLabel,
                profitLabel,
                loadButton
        );

        return row;
    }

    private void focusDateField(HBox dateField) {
        if (dateField.getUserData() instanceof TextField tf) {
            Platform.runLater(tf::requestFocus);
        }
    }

    private HBox createSmartDateField(DatePicker picker, boolean isFrom) {

        TextField ddField   = new TextField();
        TextField mmField   = new TextField();
        TextField yyyyField = new TextField();

        ddField.setPrefWidth(32);
        mmField.setPrefWidth(32);
        yyyyField.setPrefWidth(50);

        ddField.setAlignment(Pos.CENTER);
        mmField.setAlignment(Pos.CENTER);
        yyyyField.setAlignment(Pos.CENTER);

        ddField.getStyleClass().add("date-subfield");
        mmField.getStyleClass().add("date-subfield");
        yyyyField.getStyleClass().add("date-subfield");

        ddField.setPromptText("DD");
        mmField.setPromptText("MM");
        yyyyField.setPromptText("YYYY");

        Label sep1 = new Label("/");
        Label sep2 = new Label("/");
        sep1.getStyleClass().add("date-separator");
        sep2.getStyleClass().add("date-separator");

        if (picker.getValue() != null) {
            ddField.setText(String.format("%02d",
                    picker.getValue().getDayOfMonth()));
            mmField.setText(String.format("%02d",
                    picker.getValue().getMonthValue()));
            yyyyField.setText(String.format("%04d",
                    picker.getValue().getYear()));
        }

        Runnable syncToPicker = () -> {
            try {
                int dd   = Integer.parseInt(ddField.getText().trim());
                int mm   = Integer.parseInt(mmField.getText().trim());
                int yyyy = Integer.parseInt(yyyyField.getText().trim());
                picker.setValue(LocalDate.of(yyyy, mm, dd));
            } catch (Exception ignored) {}
        };

        Runnable moveToNext = () -> {
            syncToPicker.run();
            if (isFrom) {
                focusDateField(toDateField);
            } else {
                Platform.runLater(() -> loadButton.requestFocus());
            }
        };

        // ✅ DD
        ddField.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP -> {
                    e.consume();
                    LocalDate d = picker.getValue() != null
                            ? picker.getValue() : LocalDate.now();
                    picker.setValue(d.plusDays(1));
                    ddField.setText(String.format("%02d",
                            picker.getValue().getDayOfMonth()));
                }
                case DOWN -> {
                    e.consume();
                    LocalDate d = picker.getValue() != null
                            ? picker.getValue() : LocalDate.now();
                    picker.setValue(d.minusDays(1));
                    ddField.setText(String.format("%02d",
                            picker.getValue().getDayOfMonth()));
                }
                case ENTER, TAB -> {
                    e.consume();
                    syncToPicker.run();
                    Platform.runLater(() -> mmField.requestFocus());
                }
                case ESCAPE -> {
                    e.consume();
                    Platform.runLater(() -> accountSearchField.requestFocus());
                }
                case DIGIT0, DIGIT1, DIGIT2, DIGIT3, DIGIT4,
                     DIGIT5, DIGIT6, DIGIT7, DIGIT8, DIGIT9,
                     NUMPAD0, NUMPAD1, NUMPAD2, NUMPAD3, NUMPAD4,
                     NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9 -> {
                    e.consume();
                    String digit = extractDigit(e.getCode());
                    String current = ddField.getText().replaceAll("[^0-9]", "");
                    if (current.length() >= 2) current = "";
                    current += digit;
                    ddField.setText(current);
                    ddField.positionCaret(current.length());
                    if (current.length() == 2) {
                        syncToPicker.run();
                        Platform.runLater(() -> mmField.requestFocus());
                    }
                }
                default -> e.consume();
            }
        });

        // ✅ MM
        mmField.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP -> {
                    e.consume();
                    LocalDate d = picker.getValue() != null
                            ? picker.getValue() : LocalDate.now();
                    picker.setValue(d.plusMonths(1));
                    mmField.setText(String.format("%02d",
                            picker.getValue().getMonthValue()));
                }
                case DOWN -> {
                    e.consume();
                    LocalDate d = picker.getValue() != null
                            ? picker.getValue() : LocalDate.now();
                    picker.setValue(d.minusMonths(1));
                    mmField.setText(String.format("%02d",
                            picker.getValue().getMonthValue()));
                }
                case ENTER, TAB -> {
                    e.consume();
                    syncToPicker.run();
                    Platform.runLater(() -> yyyyField.requestFocus());
                }
                case BACK_SPACE -> {
                    e.consume();
                    mmField.clear();
                    Platform.runLater(() -> ddField.requestFocus());
                }
                case ESCAPE -> {
                    e.consume();
                    Platform.runLater(() -> accountSearchField.requestFocus());
                }
                case DIGIT0, DIGIT1, DIGIT2, DIGIT3, DIGIT4,
                     DIGIT5, DIGIT6, DIGIT7, DIGIT8, DIGIT9,
                     NUMPAD0, NUMPAD1, NUMPAD2, NUMPAD3, NUMPAD4,
                     NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9 -> {
                    e.consume();
                    String digit = extractDigit(e.getCode());
                    String current = mmField.getText().replaceAll("[^0-9]", "");
                    if (current.length() >= 2) current = "";
                    current += digit;
                    mmField.setText(current);
                    mmField.positionCaret(current.length());
                    if (current.length() == 2) {
                        syncToPicker.run();
                        Platform.runLater(() -> yyyyField.requestFocus());
                    }
                }
                default -> e.consume();
            }
        });

        // ✅ YYYY
        yyyyField.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP -> {
                    e.consume();
                    LocalDate d = picker.getValue() != null
                            ? picker.getValue() : LocalDate.now();
                    picker.setValue(d.plusYears(1));
                    yyyyField.setText(String.format("%04d",
                            picker.getValue().getYear()));
                }
                case DOWN -> {
                    e.consume();
                    LocalDate d = picker.getValue() != null
                            ? picker.getValue() : LocalDate.now();
                    picker.setValue(d.minusYears(1));
                    yyyyField.setText(String.format("%04d",
                            picker.getValue().getYear()));
                }
                case ENTER, TAB -> {
                    e.consume();
                    moveToNext.run();
                }
                case BACK_SPACE -> {
                    e.consume();
                    yyyyField.clear();
                    Platform.runLater(() -> mmField.requestFocus());
                }
                case ESCAPE -> {
                    e.consume();
                    Platform.runLater(() -> accountSearchField.requestFocus());
                }
                case DIGIT0, DIGIT1, DIGIT2, DIGIT3, DIGIT4,
                     DIGIT5, DIGIT6, DIGIT7, DIGIT8, DIGIT9,
                     NUMPAD0, NUMPAD1, NUMPAD2, NUMPAD3, NUMPAD4,
                     NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9 -> {
                    e.consume();
                    String digit = extractDigit(e.getCode());
                    String current = yyyyField.getText().replaceAll("[^0-9]", "");
                    if (current.length() >= 4) current = "";
                    current += digit;
                    yyyyField.setText(current);
                    yyyyField.positionCaret(current.length());
                    if (current.length() == 4) syncToPicker.run();
                }
                default -> e.consume();
            }
        });

        ddField.focusedProperty().addListener((obs, was, is) -> {
            if (is) Platform.runLater(ddField::selectAll);
        });
        mmField.focusedProperty().addListener((obs, was, is) -> {
            if (is) Platform.runLater(mmField::selectAll);
        });
        yyyyField.focusedProperty().addListener((obs, was, is) -> {
            if (is) Platform.runLater(yyyyField::selectAll);
        });

        HBox dateBox = new HBox(2, ddField, sep1, mmField, sep2, yyyyField);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.getStyleClass().add("date-field-box");
        dateBox.setUserData(ddField);

        return dateBox;
    }

    private String extractDigit(javafx.scene.input.KeyCode code) {
        return code.getName()
                .replace("Numpad ", "")
                .replace("Digit", "")
                .trim();
    }

    private TableView<LedgerRow> createTable() {

        TableView<LedgerRow> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LedgerRow, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDate()));

        TableColumn<LedgerRow, String> particularsCol =
                new TableColumn<>("Particulars");
        particularsCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getParticulars()));

        TableColumn<LedgerRow, Number> debitCol = new TableColumn<>("Debit");
        debitCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getDebit()));

        TableColumn<LedgerRow, Number> creditCol = new TableColumn<>("Credit");
        creditCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getCredit()));

        TableColumn<LedgerRow, String> remarkCol = new TableColumn<>("Remark");
        remarkCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRemark()));

        TableColumn<LedgerRow, String> voucherTypeCol =
                new TableColumn<>("Voucher Type");
        voucherTypeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getVoucherType()));

        TableColumn<LedgerRow, String> voucherNoCol =
                new TableColumn<>("Voucher No");
        voucherNoCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getVoucherNo()));

        TableColumn<LedgerRow, Number> balanceCol = new TableColumn<>("Balance");
        balanceCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getRunningBalance()));

        tableView.getColumns().addAll(
                dateCol, particularsCol, debitCol, creditCol,
                remarkCol, voucherTypeCol, voucherNoCol, balanceCol
        );

        tableView.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(() -> accountSearchField.requestFocus());
            }
        });

        return tableView;
    }

    private void loadLedger() {

        if (selectedAccount == null) {
            new Alert(Alert.AlertType.WARNING, "Please select account.")
                    .showAndWait();
            Platform.runLater(() -> accountSearchField.requestFocus());
            return;
        }

        LedgerSummaryDTO summary = ledgerRepository.generateLedger(
                selectedAccount.getUuid(),
                selectedAccount.getAccountGroup(),
                fromDatePicker.getValue(),
                toDatePicker.getValue()
        );

        outstandingLabel.setText("Outstanding ₹" +
                String.format("%,.2f", summary.getOutstanding()));
        profitLabel.setText("Profit ₹" +
                String.format("%,.2f", summary.getTotalProfit()));
        totalDebitLabel.setText("Debit ₹" +
                String.format("%,.2f", summary.getTotalDebit()));
        totalCreditLabel.setText("Credit ₹" +
                String.format("%,.2f", summary.getTotalCredit()));
        closingBalanceLabel.setText("Closing ₹" +
                String.format("%,.2f", summary.getClosingBalance()));

        table.setItems(FXCollections.observableArrayList(summary.getRows()));
    }

    private HBox createFooter() {

        totalDebitLabel     = new Label("Debit ₹0");
        totalCreditLabel    = new Label("Credit ₹0");
        closingBalanceLabel = new Label("Closing ₹0");

        totalDebitLabel.getStyleClass().add("metric-chip");
        totalCreditLabel.getStyleClass().add("metric-chip");
        closingBalanceLabel.getStyleClass().add("metric-chip-success");

        HBox footer = new HBox(15,
                totalDebitLabel, totalCreditLabel, closingBalanceLabel);
        footer.setAlignment(Pos.CENTER_RIGHT);

        return footer;
    }
}