package KP_TOURS.ui.creditnotes;

import KP_TOURS.model.Account;
import KP_TOURS.model.CreditNote;
import KP_TOURS.model.PurchaseSales;
import KP_TOURS.repository.AccountRepository;
import KP_TOURS.repository.CreditNoteRepository;
import KP_TOURS.repository.PurchaseSalesRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.controlsfx.control.SearchableComboBox;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CreditNotesView {

    private static final CreditNoteRepository    repository =
            new CreditNoteRepository();
    private static final AccountRepository       accountRepository =
            new AccountRepository();
    private static final PurchaseSalesRepository psRepository =
            new PurchaseSalesRepository();

    // ── State ─────────────────────────────────────────────────────────
    private SearchableComboBox<Account> customerBox;
    private ComboBox<String>            billNoBox;
    private TableView<CreditNote>       table;

    private TextField billDateField;
    private TextField creditorField;
    private TextField particularsField;
    private TextField qtyField;
    private TextField rateField;
    private TextField purchaseAmtField;
    private TextField saleAmtField;
    private TextField bankRefundField;
    private TextField partyRefundField;
    private Label     diffLabel;
    private TextField ticketNoField;
    private ComboBox<String> paymentModeBox;
    private TextArea  remarkArea;
    private Label     cnNoLabel;
    private DatePicker datePicker;

    private final String[]  selectedPsUuid   = {null};
    private final String[]  selectedCustUuid = {null};
    private final String[]  selectedCredUuid = {null};
    private final double[]  selectedSaleAmt  = {0};
    private final double[]  selectedPurchAmt = {0};
    private final int[]     selectedQty      = {1};
    private final double[]  selectedRate     = {0};

    private static Parent cachedView;
    private static CreditNotesView instance;

    public static Parent getView() {
        if (cachedView != null) {
            instance.refreshCnNo();
            return cachedView;
        }
        instance   = new CreditNotesView();
        cachedView = instance.buildView();
        return cachedView;
    }

    public static javafx.scene.Node getFocusTarget() {
        return instance != null ? instance.customerBox : null;
    }

    public static void clearCache() {
        cachedView = null;
        instance   = null;
    }

    // ── Build full screen ─────────────────────────────────────────────
    private Parent buildView() {

        VBox root = new VBox(0);
        root.getStyleClass().add("main-content");

        root.getChildren().addAll(
                buildHeader(),
                buildForm(),
                buildListSection()
        );

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.sceneProperty().addListener((obs, o, n) -> {
            if (n != null) Platform.runLater(() -> customerBox.requestFocus());
        });

        return scroll;
    }

    // ── Header ────────────────────────────────────────────────────────
    private HBox buildHeader() {

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(22, 22, 10, 22));

        VBox titleBox = new VBox(3);
        Label title    = new Label("Credit Notes");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label(
                "Create refund credit notes against cancelled bills.");
        subtitle.getStyleClass().add("section-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hint = new Label("ESC → Clear form");
        hint.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8;");

        header.getChildren().addAll(titleBox, spacer, hint);
        return header;
    }

    // ── Create Form (always visible at top) ───────────────────────────
    private VBox buildForm() {

        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("premium-panel");
        formCard.setPadding(new Insets(20));
        VBox.setMargin(formCard, new Insets(0, 22, 14, 22));

        // ── Row 0: CN No + Date ───────────────────────────────────────
        cnNoLabel = new Label(repository.getNextCreditNoteNo());
        cnNoLabel.getStyleClass().add("bill-number");
        cnNoLabel.setStyle(
                "-fx-font-size:16px; -fx-font-weight:bold; " +
                        "-fx-text-fill:#1e3a8a;");

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        // ── Row 1: Customer ───────────────────────────────────────────
        customerBox = new SearchableComboBox<>();
        customerBox.setItems(FXCollections.observableArrayList(
                accountRepository.findAllDebtors()));
        customerBox.setPromptText("Search & select customer...");
        customerBox.setMaxWidth(Double.MAX_VALUE);

        // ── Row 2: Bill No ────────────────────────────────────────────
        billNoBox = new ComboBox<>();
        billNoBox.setPromptText("Select Bill No (select customer first)");
        billNoBox.setMaxWidth(Double.MAX_VALUE);

        // ── Read-only fields ──────────────────────────────────────────
        billDateField    = readOnly();
        creditorField    = readOnly();
        particularsField = readOnly();
        qtyField         = readOnly();
        rateField        = readOnly();
        purchaseAmtField = readOnly();
        saleAmtField     = readOnly();

        // ── Editable refund fields ────────────────────────────────────
        bankRefundField  = editableField("Bank Refund Amount");
        partyRefundField = editableField("Party Refund Amount");

        diffLabel = new Label("₹ 0.00");
        diffLabel.setStyle(
                "-fx-font-weight:bold; -fx-font-size:14px; " +
                        "-fx-text-fill:#16a34a;");

        ticketNoField  = editableField("Ticket / PNR No");
        paymentModeBox = new ComboBox<>();
        paymentModeBox.getItems().addAll("CASH", "CREDIT", "CARD");
        paymentModeBox.setValue("CASH");
        paymentModeBox.setMaxWidth(Double.MAX_VALUE);

        remarkArea = new TextArea();
        remarkArea.setPromptText("Remark");
        remarkArea.setPrefRowCount(2);
        remarkArea.setMaxWidth(Double.MAX_VALUE);

        Button saveBtn  = new Button("Save Credit Note");
        Button clearBtn = new Button("Clear");
        saveBtn.getStyleClass().add("primary-button");
        clearBtn.getStyleClass().add("secondary-button");

        // ── Listeners ─────────────────────────────────────────────────
        partyRefundField.textProperty()
                .addListener((obs, o, n) -> updateDiff());

        customerBox.valueProperty().addListener((obs, o, customer) -> {
            // Don't steal focus back onto the customer box here — it is
            // already focused while the user is choosing/navigating it.
            // Re-requesting focus mid-navigation is what caused arrow-key
            // browsing to feel like it was "jumping" to another item.
            clearForm(false, false);
            if (customer == null) {
                selectedCustUuid[0] = null;
                billNoBox.setItems(FXCollections.emptyObservableList());
                loadAllEntries();
                return;
            }
            selectedCustUuid[0] = customer.getUuid();

            List<PurchaseSales> bills = psRepository.findAll().stream()
                    .filter(ps -> customer.getUuid()
                            .equals(ps.getCustomerUuid()))
                    .filter(ps -> "ACTIVE".equalsIgnoreCase(ps.getStatus()))
                    .collect(Collectors.toList());

            billNoBox.setItems(FXCollections.observableArrayList(
                    bills.stream().map(PurchaseSales::getBillNo)
                            .collect(Collectors.toList())));

            // Filter list below to this customer's credit notes
            loadEntriesForCustomer(customer.getUuid());
        });

        billNoBox.valueProperty().addListener((obs, o, billNo) -> {
            if (billNo == null) return;

            // Find the PS record
            psRepository.findAll().stream()
                    .filter(ps -> billNo.equals(ps.getBillNo()))
                    .findFirst()
                    .ifPresent(ps -> {
                        selectedPsUuid[0]   = ps.getUuid();
                        selectedCredUuid[0] = ps.getPurchaseFrom();
                        selectedSaleAmt[0]  = ps.getTotalSale();
                        selectedPurchAmt[0] = ps.getTotalPurchase();
                        selectedQty[0]      = ps.getQty();
                        selectedRate[0]     = ps.getSellRate();

                        billDateField.setText(
                                ps.getEntryDate() != null
                                        ? ps.getEntryDate().toString() : "-");

                        Account cred = accountRepository
                                .findByUuid(ps.getPurchaseFrom());
                        creditorField.setText(
                                cred != null ? cred.getName() : "-");

                        particularsField.setText(
                                ps.getDescription() != null
                                        ? ps.getDescription() : "-");

                        qtyField.setText(String.valueOf(ps.getQty()));
                        rateField.setText(
                                String.format("%,.2f", ps.getSellRate()));
                        purchaseAmtField.setText(
                                String.format("%,.2f", ps.getTotalPurchase()));
                        saleAmtField.setText(
                                String.format("%,.2f", ps.getTotalSale()));

                        // Default refund values
                        partyRefundField.setText(
                                String.format("%.2f", ps.getTotalSale()));
                        bankRefundField.setText(
                                String.format("%.2f", ps.getTotalPurchase()));

                        updateDiff();
                        // NOTE: focus is intentionally NOT forced to
                        // bankRefundField here. Doing that on every value
                        // change (including arrow-key highlight changes
                        // while the dropdown is open) was stealing focus
                        // out from under the user mid-navigation, which is
                        // what looked like "the next bill auto-selects
                        // itself". The Enter-key chain below already moves
                        // focus to bankRefundField once the user actually
                        // confirms a Bill No.
                    });
        });

        // ── Save ──────────────────────────────────────────────────────
        saveBtn.setOnAction(e -> handleSave());
        saveBtn.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                handleSave();
            }
        });

        // ── Clear ─────────────────────────────────────────────────────
        clearBtn.setOnAction(e -> clearForm(true));

        // ── ESC anywhere clears form ──────────────────────────────────
        formCard.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                clearForm(true);
            }
        });

        // ── Enter flow ────────────────────────────────────────────────
        setEnter(customerBox,      () -> billNoBox.requestFocus());
        setEnter(billNoBox,        () -> bankRefundField.requestFocus());
        setEnter(bankRefundField,  () -> partyRefundField.requestFocus());
        setEnter(partyRefundField, () -> ticketNoField.requestFocus());
        setEnter(ticketNoField,    () -> paymentModeBox.requestFocus());
        setEnter(paymentModeBox,   () -> remarkArea.requestFocus());
        setEnter(remarkArea,       () -> saveBtn.requestFocus());

        // ── Grid layout ───────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);

        ColumnConstraints lc  = new ColumnConstraints(130);
        ColumnConstraints fc  = new ColumnConstraints(
                180, 220, Double.MAX_VALUE);
        ColumnConstraints lc2 = new ColumnConstraints(130);
        ColumnConstraints fc2 = new ColumnConstraints(
                180, 220, Double.MAX_VALUE);
        fc.setHgrow(Priority.ALWAYS);
        fc2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(lc, fc, lc2, fc2);

        int r = 0;

        // Row 0: CN No + Date
        grid.add(fl("CN No"),          0, r); grid.add(cnNoLabel,       1, r);
        grid.add(fl("Date"),           2, r); grid.add(datePicker,      3, r); r++;

        // Row 1: Customer (full width)
        grid.add(fl("Customer"),       0, r);
        grid.add(customerBox,          1, r, 3, 1); r++;

        // Row 2: Bill No (full width)
        grid.add(fl("Bill No"),        0, r);
        grid.add(billNoBox,            1, r, 3, 1); r++;

        // Row 3: Bill Date + Creditor
        grid.add(fl("Bill Date"),      0, r); grid.add(billDateField,   1, r);
        grid.add(fl("Bank/Creditor"),  2, r); grid.add(creditorField,   3, r); r++;

        // Row 4: Particulars (full width)
        grid.add(fl("Particulars"),    0, r);
        grid.add(particularsField,     1, r, 3, 1); r++;

        // Row 5: Qty + Rate
        grid.add(fl("Qty"),            0, r); grid.add(qtyField,        1, r);
        grid.add(fl("Rate ₹"),         2, r); grid.add(rateField,       3, r); r++;

        // Row 6: Purchase Amt + Sale Amt
        grid.add(fl("Purchase Amt ₹"), 0, r); grid.add(purchaseAmtField,1, r);
        grid.add(fl("Sale Amt ₹"),     2, r); grid.add(saleAmtField,    3, r); r++;

        // Separator
        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(4, 0, 4, 0));
        grid.add(sep, 0, r, 4, 1); r++;

        // Row 7: Bank Refund + Party Refund
        grid.add(fl("Bank Refund ₹"),  0, r); grid.add(bankRefundField, 1, r);
        grid.add(fl("Party Refund ₹"), 2, r); grid.add(partyRefundField,3, r); r++;

        // Row 8: Diff + Payment Mode
        grid.add(fl("Diff ₹"),         0, r); grid.add(diffLabel,       1, r);
        grid.add(fl("Payment Mode"),   2, r); grid.add(paymentModeBox,  3, r); r++;

        // Row 9: Ticket No (full width)
        grid.add(fl("Ticket No"),      0, r);
        grid.add(ticketNoField,        1, r, 3, 1); r++;

        // Row 10: Remark (full width)
        grid.add(fl("Remark"),         0, r);
        grid.add(remarkArea,           1, r, 3, 1); r++;

        // Row 11: Buttons (right-aligned)
        HBox btnRow = new HBox(10, clearBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        grid.add(btnRow, 0, r, 4, 1);

        formCard.getChildren().add(grid);
        return formCard;
    }

    // ── List section ──────────────────────────────────────────────────
    private VBox buildListSection() {

        VBox listCard = new VBox(12);
        listCard.getStyleClass().add("premium-panel");
        listCard.setPadding(new Insets(16));
        VBox.setMargin(listCard, new Insets(0, 22, 22, 22));
        VBox.setVgrow(listCard, Priority.ALWAYS);

        HBox listHeader = new HBox(10);
        listHeader.setAlignment(Pos.CENTER_LEFT);

        Label listTitle = new Label("Credit Notes");
        listTitle.getStyleClass().add("ledger-form-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button refreshBtn = new Button("↺ Refresh");
        refreshBtn.getStyleClass().add("secondary-button");
        refreshBtn.setOnAction(e -> {
            if (selectedCustUuid[0] != null) {
                loadEntriesForCustomer(selectedCustUuid[0]);
            } else {
                loadAllEntries();
            }
        });

        listHeader.getChildren().addAll(listTitle, sp, refreshBtn);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        loadAllEntries();

        listCard.getChildren().addAll(listHeader, table);
        return listCard;
    }

    // ✅ Load account map once for efficient UUID→name lookups
    java.util.Map<String, Account> accountMap =
            accountRepository.findAllAsMap();
    // ── Table ─────────────────────────────────────────────────────────
    private TableView<CreditNote> buildTable() {

        TableView<CreditNote> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPrefHeight(400);

        TableColumn<CreditNote, String> cnNoCol = new TableColumn<>("CN No");
        cnNoCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCreditNoteNo()));
        cnNoCol.setPrefWidth(65); cnNoCol.setMaxWidth(80);

        TableColumn<CreditNote, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEntryDate() != null
                        ? d.getValue().getEntryDate().toString() : ""));
        dateCol.setPrefWidth(90); dateCol.setMaxWidth(110);

        TableColumn<CreditNote, String> billNoCol = new TableColumn<>("Bill No");
        billNoCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getBillNo()));
        billNoCol.setPrefWidth(65); billNoCol.setMaxWidth(80);

        TableColumn<CreditNote, String> customerCol =
                new TableColumn<>("Customer");

        customerCol.setPrefWidth(130); customerCol.setMinWidth(100);

        TableColumn<CreditNote, String> creditorCol =
                new TableColumn<>("Bank/Creditor");
        // Customer column
        customerCol.setCellValueFactory(d -> {
            Account acc = accountMap.get(d.getValue().getCustomerUuid());
            return new SimpleStringProperty(acc != null ? acc.getName() : "-");
        });

// Creditor column
        creditorCol.setCellValueFactory(d -> {
            Account acc = accountMap.get(d.getValue().getCreditorUuid());
            return new SimpleStringProperty(acc != null ? acc.getName() : "-");
        });
        creditorCol.setPrefWidth(120); creditorCol.setMinWidth(100);

        TableColumn<CreditNote, String> particularsCol =
                new TableColumn<>("Particulars");
        particularsCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getParticulars()));
        particularsCol.setPrefWidth(130);

        TableColumn<CreditNote, Number> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getQty()));
        qtyCol.setPrefWidth(45); qtyCol.setMaxWidth(60);

        TableColumn<CreditNote, Number> rateCol = new TableColumn<>("Rate ₹");
        rateCol.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getRate()));
        rateCol.setPrefWidth(85); rateCol.setMaxWidth(100);

        TableColumn<CreditNote, Number> purchaseCol =
                new TableColumn<>("Purchase ₹");
        purchaseCol.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getPurchaseAmount()));
        purchaseCol.setPrefWidth(90); purchaseCol.setMaxWidth(110);

        TableColumn<CreditNote, Number> saleCol = new TableColumn<>("Sale ₹");
        saleCol.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getAmount()));
        saleCol.setPrefWidth(90); saleCol.setMaxWidth(110);

        TableColumn<CreditNote, Number> bankRefCol =
                new TableColumn<>("Bank Ref ₹");
        bankRefCol.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getBankRefund()));
        bankRefCol.setPrefWidth(90); bankRefCol.setMaxWidth(110);

        TableColumn<CreditNote, Number> partyRefCol =
                new TableColumn<>("Party Ref ₹");
        partyRefCol.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getPartyRefund()));
        partyRefCol.setPrefWidth(90); partyRefCol.setMaxWidth(110);

        TableColumn<CreditNote, Number> diffCol = new TableColumn<>("Diff ₹");
        diffCol.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getDiff()));
        diffCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    double v = item.doubleValue();
                    setText(String.format("%,.2f", v));
                    setStyle(v >= 0
                            ? "-fx-text-fill:#16a34a;-fx-font-weight:bold;"
                            : "-fx-text-fill:#dc2626;-fx-font-weight:bold;");
                }
            }
        });
        diffCol.setPrefWidth(85); diffCol.setMaxWidth(100);

        TableColumn<CreditNote, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(85); actionCol.setMaxWidth(100);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Cancel");
            private final HBox   box = new HBox(btn);
            {
                box.setAlignment(Pos.CENTER);
                btn.getStyleClass().add("danger-button");
                btn.setOnAction(e -> {
                    CreditNote item =
                            getTableView().getItems().get(getIndex());
                    cancelNote(item);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tv.getColumns().addAll(
                cnNoCol, dateCol, billNoCol, customerCol, creditorCol,
                particularsCol, qtyCol, rateCol, purchaseCol, saleCol,
                bankRefCol, partyRefCol, diffCol, actionCol
        );

        return tv;
    }

    // ── Save handler ──────────────────────────────────────────────────
    private void handleSave() {

        if (customerBox.getValue() == null) {
            alert("Please select a customer"); return;
        }
        if (billNoBox.getValue() == null
                || billNoBox.getValue().isBlank()) {
            alert("Please select a Bill No"); return;
        }
        if (selectedPsUuid[0] == null) {
            alert("Bill not found. Please re-select."); return;
        }
        if (parseDouble(bankRefundField.getText()) <= 0
                && parseDouble(partyRefundField.getText()) <= 0) {
            alert("Please enter at least one refund amount"); return;
        }

        double saleAmt  = selectedSaleAmt[0];
        double partyRef = parseDouble(partyRefundField.getText());
        double bankRef  = parseDouble(bankRefundField.getText());

        CreditNote cn = new CreditNote();
        cn.setCreditNoteNo(repository.getNextCreditNoteNo());
        cn.setEntryDate(datePicker.getValue());
        cn.setPurchaseSalesUuid(selectedPsUuid[0]);
        cn.setBillNo(billNoBox.getValue());
        cn.setCustomerUuid(selectedCustUuid[0]);
        cn.setCreditorUuid(selectedCredUuid[0]);
        cn.setQty(selectedQty[0]);
        cn.setRate(selectedRate[0]);
        cn.setPurchaseAmount(selectedPurchAmt[0]);
        cn.setAmount(saleAmt);
        cn.setBankRefund(bankRef);
        cn.setPartyRefund(partyRef);
        cn.calculateDiff();
        cn.setParticulars(particularsField.getText());
        cn.setTicketNo(ticketNoField.getText().trim());
        cn.setPaymentMode(paymentModeBox.getValue());
        cn.setRemark(remarkArea.getText().trim());

        // Get bill date
        psRepository.findAll().stream()
                .filter(ps -> ps.getUuid().equals(selectedPsUuid[0]))
                .findFirst()
                .ifPresent(ps -> cn.setBillDate(ps.getEntryDate()));

        // Cancel the linked PS bill first
        boolean billCancelled = psRepository.cancel(selectedPsUuid[0]);
        if (!billCancelled) {
            alert("Failed to cancel the linked bill. Credit note not saved.");
            return;
        }

        // Save credit note + post PayReceive entries
        boolean saved = repository.save(cn);
        if (saved) {
            alert("Credit Note " + cn.getCreditNoteNo()
                    + " saved.\nBill No " + cn.getBillNo()
                    + " cancelled.\nDiff retained: ₹"
                    + String.format("%,.2f", cn.getDiff()));

            // Refresh list for this customer
            String custUuid = selectedCustUuid[0];
            clearForm(false);
            if (custUuid != null) {
                loadEntriesForCustomer(custUuid);
            } else {
                loadAllEntries();
            }
            // Refresh CN No
            refreshCnNo();
        } else {
            alert("Failed to save Credit Note.");
        }
    }

    // ── Cancel a credit note ──────────────────────────────────────────
    private void cancelNote(CreditNote item) {
        if ("CANCELLED".equalsIgnoreCase(item.getStatus())) {
            alert("Already cancelled."); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Credit Note");
        confirm.setHeaderText("Cancel CN No: " + item.getCreditNoteNo());
        confirm.setContentText("Are you sure?");
        confirm.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            if (repository.cancel(item.getUuid())) {
                if (selectedCustUuid[0] != null) {
                    loadEntriesForCustomer(selectedCustUuid[0]);
                } else {
                    loadAllEntries();
                }
            } else {
                alert("Failed to cancel.");
            }
        });
    }

    // ── Data loading ──────────────────────────────────────────────────
    private void loadAllEntries() {
        table.setItems(
                FXCollections.observableArrayList(repository.findAll()));
    }

    private void loadEntriesForCustomer(String customerUuid) {
        List<CreditNote> filtered = repository.findAll().stream()
                .filter(cn -> customerUuid.equals(cn.getCustomerUuid()))
                .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(filtered));
    }

    // ── Clear form ────────────────────────────────────────────────────
    private void clearForm(boolean clearCustomerToo) {
        clearForm(clearCustomerToo, true);
    }

    private void clearForm(boolean clearCustomerToo, boolean refocusCustomer) {
        if (clearCustomerToo) {
            customerBox.setValue(null);
            billNoBox.setItems(FXCollections.emptyObservableList());
            loadAllEntries();
        }
        billNoBox.setValue(null);
        billDateField.clear();
        creditorField.clear();
        particularsField.clear();
        qtyField.clear();
        rateField.clear();
        purchaseAmtField.clear();
        saleAmtField.clear();
        bankRefundField.setText("0");
        partyRefundField.setText("0");
        diffLabel.setText("₹ 0.00");
        diffLabel.setStyle(
                "-fx-font-weight:bold; -fx-font-size:14px; " +
                        "-fx-text-fill:#16a34a;");
        ticketNoField.clear();
        remarkArea.clear();
        paymentModeBox.setValue("CASH");
        datePicker.setValue(LocalDate.now());
        selectedPsUuid[0]   = null;
        selectedCredUuid[0] = null;
        selectedSaleAmt[0]  = 0;
        selectedPurchAmt[0] = 0;
        selectedQty[0]      = 1;
        selectedRate[0]     = 0;
        if (refocusCustomer) {
            Platform.runLater(() -> customerBox.requestFocus());
        }
    }

    // ── Diff ──────────────────────────────────────────────────────────
    private void updateDiff() {
        double diff = selectedSaleAmt[0]
                - parseDouble(partyRefundField.getText());
        diffLabel.setText("₹ " + String.format("%,.2f", diff));
        diffLabel.setStyle(
                "-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill:"
                        + (diff >= 0 ? "#16a34a" : "#dc2626") + ";");
    }

    private void refreshCnNo() {
        if (cnNoLabel != null) {
            cnNoLabel.setText(repository.getNextCreditNoteNo());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private TextField readOnly() {
        TextField tf = new TextField();
        tf.setEditable(false);
        tf.setFocusTraversable(false);
        tf.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private TextField editableField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("premium-input");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private Label fl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight:bold; -fx-font-size:12px; " +
                "-fx-text-fill:black;");
        return l;
    }

    private void setEnter(javafx.scene.Node node, Runnable next) {
        node.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                Platform.runLater(next);
            }
        });
    }

    private double parseDouble(String v) {
        try {
            return Double.parseDouble(
                    v == null ? "0" : v.trim().replace(",", ""));
        } catch (Exception e) { return 0; }
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION) {{
            setHeaderText(null);
            setContentText(msg);
        }}.showAndWait();
    }
}