package KP_TOURS.ui.paymentsreceive;


import KP_TOURS.model.Account;
import KP_TOURS.model.OutstandingBill;
import KP_TOURS.model.PayReceive;
import KP_TOURS.model.PayReceiveBillAdjustment;
import KP_TOURS.enums.PayReceiveType;
import KP_TOURS.repository.AccountRepository;
import KP_TOURS.repository.PayReceiveRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.util.converter.DoubleStringConverter;
import org.controlsfx.control.SearchableComboBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class PayReceiveView {

    private final PayReceiveRepository payReceiveRepository = new PayReceiveRepository();
    private final AccountRepository accountRepository = new AccountRepository();

    private Label voucherNoLabel;
    private DatePicker datePicker;
    private ComboBox<PayReceiveType> typeCombo;
    private SearchableComboBox<Account> accountCombo;
    private ComboBox<String> paymentModeCombo;

    private Label totalOutstandingLabel;
    private Label totalAmountLabel;

    private TableView<OutstandingBill> billsTable;

    private TextField referenceField;
    private TextArea remarkArea;

    public static Parent getView() {
        PayReceiveView view = new PayReceiveView();
        return view.buildView();
    }

    private Parent buildView() {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #eef3fb;");

        VBox container = new VBox(18);
        container.setPadding(new Insets(0, 10, 20, 10));

        Label title = new Label("Payables & Receivables");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: 800; -fx-text-fill: #06142e;");

        Label subtitle = new Label("Manage bill-wise payments and receipts with outstanding tracking.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        VBox titleBox = new VBox(4, title, subtitle);

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button showEntriesBtn = new Button("Show Entries");
        showEntriesBtn.getStyleClass().add("secondary-button");

        showEntriesBtn.setOnAction(e -> {
            DashboardView.loadScreen(
                    PayReceiveViewList.getView()
            );
        });

        HBox titleRow = new HBox(14, titleBox, titleSpacer, showEntriesBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(
                titleRow,
                createHeader(),
                createAccountSection(),
                createSummaryCard(),
                createBillsSection(),
                createFooter(),
                createButtons()
        );

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.setCenter(scrollPane);

        initializeDefaults();
        setupListeners();

        return root;
    }

    private VBox createHeader() {

        voucherNoLabel = new Label();
        voucherNoLabel.setMinWidth(110);
        voucherNoLabel.setAlignment(Pos.CENTER);
        voucherNoLabel.setStyle(
                "-fx-font-size: 30px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: #000000;" +
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #dbe5f1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-padding: 10 22;"
        );

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(260);
        styleInput(datePicker);

        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
                PayReceiveType.RECEIVE,
                PayReceiveType.PAY
        );
        typeCombo.setPrefWidth(280);
        styleInput(typeCombo);

        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER_LEFT);

        grid.add(createFormLabel("Voucher No"), 0, 0);
        grid.add(voucherNoLabel, 1, 0);

        grid.add(createFormLabel("Date"), 2, 0);
        grid.add(datePicker, 3, 0);

        grid.add(createFormLabel("Type"), 0, 1);
        grid.add(typeCombo, 1, 1);

        VBox card = new VBox(grid);
        card.setPadding(new Insets(24));
        card.setStyle(cardStyle());

        return card;
    }

    private GridPane createAccountSection() {

        accountCombo = new SearchableComboBox<>();
        accountCombo.setPrefWidth(420);
        styleInput(accountCombo);

        paymentModeCombo = new ComboBox<>();
        paymentModeCombo.getItems().addAll(
                "Cash",
                "Card",
                "Bank"
        );
        paymentModeCombo.setPrefWidth(300);
        styleInput(paymentModeCombo);

        GridPane grid = new GridPane();
        grid.setHgap(28);
        grid.setVgap(18);
        grid.setPadding(new Insets(24));
        grid.setStyle(cardStyle());

        grid.add(createFormLabel("Account"), 0, 0);
        grid.add(accountCombo, 1, 0);

        grid.add(createFormLabel("Payment Mode"), 2, 0);
        grid.add(paymentModeCombo, 3, 0);

        return grid;
    }

    private HBox createSummaryCard() {

        totalOutstandingLabel = createAmountLabel("₹0.00");
        totalAmountLabel = createAmountLabel("₹0.00");

        HBox row = new HBox(18);
        row.getChildren().addAll(
                createMiniCard("Total Outstanding", totalOutstandingLabel),
                createMiniCard("Current Adjustment", totalAmountLabel)
        );

        return row;
    }

    private VBox createBillsSection() {

        Label sectionTitle = new Label("Outstanding Bills");
        sectionTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #020617;");

        billsTable = createBillsTable();

        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle(cardStyle());
        card.getChildren().addAll(sectionTitle, billsTable);

        return card;
    }

    private TableView<OutstandingBill> createBillsTable() {

        TableView<OutstandingBill> table = new TableView<>();
        table.setEditable(true);
        table.setPrefHeight(380);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 14;"
        );

        TableColumn<OutstandingBill, String> billNoCol =
                new TableColumn<>("Bill No");
        billNoCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBillNo())
        );

        TableColumn<OutstandingBill, Number> billAmountCol =
                new TableColumn<>("Bill Amount");
        billAmountCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getBillAmount())
        );

        TableColumn<OutstandingBill, Number> adjustedCol =
                new TableColumn<>("Adjusted");
        adjustedCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getAdjustedAmount())
        );

        TableColumn<OutstandingBill, Number> outstandingCol =
                new TableColumn<>("Outstanding");
        outstandingCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getOutstandingAmount())
        );

        TableColumn<OutstandingBill, String> statusCol =
                new TableColumn<>("Status");
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus())
        );

        TableColumn<OutstandingBill, Double> adjustAmountCol =
                new TableColumn<>("Adjust Amount");

        adjustAmountCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getTempAdjustAmount()).asObject()
        );

        adjustAmountCol.setCellFactory(
                TextFieldTableCell.forTableColumn(new DoubleStringConverter())
        );

        adjustAmountCol.setOnEditCommit(event -> {

            OutstandingBill bill = event.getRowValue();

            double enteredAmount =
                    event.getNewValue() == null ? 0 : event.getNewValue();

            if (enteredAmount < 0) {
                enteredAmount = 0;
            }

            bill.setTempAdjustAmount(enteredAmount);

            updateTotals();
            table.refresh();
        });

        table.getColumns().addAll(
                billNoCol,
                billAmountCol,
                adjustedCol,
                outstandingCol,
                statusCol,
                adjustAmountCol
        );

        return table;
    }

    private VBox createFooter() {

        referenceField = new TextField();
        referenceField.setPromptText("Reference No");
        referenceField.setPrefWidth(420);
        styleInput(referenceField);

        remarkArea = new TextArea();
        remarkArea.setPromptText("Remark");
        remarkArea.setPrefRowCount(3);
        remarkArea.setPrefWidth(420);
        styleInput(remarkArea);

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(18);

        grid.add(createFormLabel("Reference No"), 0, 0);
        grid.add(referenceField, 1, 0);

        grid.add(createFormLabel("Remark"), 0, 1);
        grid.add(remarkArea, 1, 1);

        VBox card = new VBox(grid);
        card.setPadding(new Insets(24));
        card.setStyle(cardStyle());

        return card;
    }

    private HBox createButtons() {

        Button saveButton = new Button("Save Voucher");
        saveButton.setStyle(primaryButtonStyle());

        Button clearButton = new Button("Clear");
        clearButton.setStyle(secondaryButtonStyle());

        saveButton.setOnAction(event -> savePayReceive());
        clearButton.setOnAction(event -> clearForm());

        HBox buttons = new HBox(12, clearButton, saveButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        return buttons;
    }

    private void initializeDefaults() {

        typeCombo.setValue(PayReceiveType.RECEIVE);
        paymentModeCombo.setValue("Cash");

        refreshVoucherNo();
        loadAccounts();
    }

    private void setupListeners() {

        typeCombo.setOnAction(event -> {
            refreshVoucherNo();
            loadAccounts();
            clearBills();
        });

        accountCombo.setOnAction(event -> loadOutstandingBills());
    }

    private void refreshVoucherNo() {

        PayReceiveType type = typeCombo.getValue();

        if (type == null) {
            return;
        }

        String voucherNo =
                payReceiveRepository.generateNextVoucherNo(type);

        voucherNoLabel.setText(voucherNo);
    }

    private void loadAccounts() {

        PayReceiveType type = typeCombo.getValue();

        if (type == null) {
            return;
        }

        List<Account> accounts;

        if (type == PayReceiveType.RECEIVE) {
            accounts = accountRepository.findAllDebtors();
        } else {
            accounts = accountRepository.findAllCreditors();
        }

        accountCombo.setItems(
                FXCollections.observableArrayList(accounts)
        );

        accountCombo.setValue(null);
    }

    private void loadOutstandingBills() {


;

        Account account = accountCombo.getValue();
        PayReceiveType type = typeCombo.getValue();

        if (account == null || type == null) {
            return;
        }

        System.out.println("=================================");
        System.out.println("TYPE      : " + type);
        System.out.println("UUID      : " + account.getUuid());
        System.out.println("NAME      : " + account.getName());
        System.out.println("=================================");

        List<OutstandingBill> bills =
                payReceiveRepository.getOutstandingBillsForAccount(
                        account.getUuid(),
                        type
                );

        billsTable.setItems(
                FXCollections.observableArrayList(bills)
        );

        updateTotals();
    }

    private void updateTotals() {

        double totalOutstanding =
                billsTable.getItems()
                        .stream()
                        .mapToDouble(OutstandingBill::getOutstandingAmount)
                        .sum();

        double totalAdjusted =
                billsTable.getItems()
                        .stream()
                        .mapToDouble(OutstandingBill::getTempAdjustAmount)
                        .sum();

        totalOutstandingLabel.setText(
                "₹" + String.format("%.2f", totalOutstanding)
        );

        totalAmountLabel.setText(
                "₹" + String.format("%.2f", totalAdjusted)
        );
    }

    private void savePayReceive() {

        Account account = accountCombo.getValue();
        PayReceiveType type = typeCombo.getValue();

        if (account == null) {
            showAlert("Please select account.");
            return;
        }

        if (type == null) {
            showAlert("Please select type.");
            return;
        }

        if (paymentModeCombo.getValue() == null) {
            showAlert("Please select payment mode.");
            return;
        }

        List<PayReceiveBillAdjustment> adjustments = new ArrayList<>();

        double totalAmount = 0;

        for (OutstandingBill bill : billsTable.getItems()) {

            double adjustAmount = bill.getTempAdjustAmount();

            if (adjustAmount <= 0) {
                continue;
            }

            PayReceiveBillAdjustment adjustment =
                    new PayReceiveBillAdjustment();

            adjustment.setPurchaseSalesUuid(
                    bill.getPurchaseSalesUuid()
            );

            adjustment.setBillNo(
                    bill.getBillNo()
            );

            adjustment.setAdjustedAmount(
                    adjustAmount
            );

            adjustments.add(adjustment);

            totalAmount += adjustAmount;
        }

        if (adjustments.isEmpty()) {
            showAlert("Please enter adjust amount for at least one bill.");
            return;
        }

        PayReceive payReceive = new PayReceive();

        payReceive.setVoucherNo(
                voucherNoLabel.getText()
        );

        payReceive.setEntryType(type);
        payReceive.setEntryDate(datePicker.getValue());
        payReceive.setAccountUuid(account.getUuid());
        payReceive.setPaymentMode(paymentModeCombo.getValue());
        payReceive.setTotalAmount(totalAmount);
        payReceive.setReferenceNo(referenceField.getText());
        payReceive.setRemark(remarkArea.getText());

        boolean saved =
                payReceiveRepository.save(
                        payReceive,
                        adjustments
                );

        if (saved) {
            showAlert("Saved successfully.");
            clearForm();
            refreshVoucherNo();
            loadOutstandingBills();
        } else {
            showAlert("Failed to save.");
        }
    }

    private void clearForm() {

        referenceField.clear();
        remarkArea.clear();

        if (billsTable != null) {
            for (OutstandingBill bill : billsTable.getItems()) {
                bill.setTempAdjustAmount(0);
            }
            billsTable.refresh();
        }

        updateTotals();
    }

    private void clearBills() {

        if (billsTable != null) {
            billsTable.getItems().clear();
        }

        updateTotals();
    }

    private Label createFormLabel(String text) {

        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: #334155;"
        );

        return label;
    }

    private Label createAmountLabel(String text) {

        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: #020617;"
        );

        return label;
    }

    private VBox createMiniCard(String title, Label value) {

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #64748b;" +
                        "-fx-font-weight: 700;"
        );

        VBox card = new VBox(8, titleLabel, value);
        card.setPadding(new Insets(22));
        card.setPrefWidth(320);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 18, 0, 0, 6);"
        );

        return card;
    }

    private void styleInput(Control control) {

        control.setStyle(
                "-fx-background-color: #f8fafc;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #dbe5f1;" +
                        "-fx-border-radius: 14;" +
                        "-fx-padding: 10 14;" +
                        "-fx-font-size: 14px;"
        );
    }

    private String cardStyle() {
        return """
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-border-color: #e2e8f0;
                -fx-border-radius: 18;
                -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 22, 0, 0, 8);
                """;
    }

    private String primaryButtonStyle() {
        return """
                -fx-background-color: #2537c9;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: 800;
                -fx-background-radius: 14;
                -fx-padding: 12 26;
                -fx-cursor: hand;
                """;
    }

    private String secondaryButtonStyle() {
        return """
                -fx-background-color: white;
                -fx-text-fill: #334155;
                -fx-font-size: 14px;
                -fx-font-weight: 800;
                -fx-background-radius: 14;
                -fx-border-color: #d1d5db;
                -fx-border-radius: 14;
                -fx-padding: 12 26;
                -fx-cursor: hand;
                """;
    }

    private void showAlert(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}