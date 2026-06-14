package KP_TOURS.ui.purchasesales;

import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.model.Account;
import KP_TOURS.model.PurchaseSales;
import KP_TOURS.model.Trip;
import KP_TOURS.repository.AccountRepository;
import KP_TOURS.repository.PurchaseSalesRepository;
import KP_TOURS.repository.TripRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import java.util.Optional;
import KP_TOURS.model.TripDocument;
import KP_TOURS.repository.TripDocumentRepository;
import javafx.stage.FileChooser;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PurchaseSalesListView {

    private final PurchaseSalesRepository repository =
            new PurchaseSalesRepository();
    private final AccountRepository accountRepository =
            new AccountRepository();
    private final TripDocumentRepository tripDocumentRepository =
            new TripDocumentRepository();
    private final TripRepository tripRepository = new TripRepository();

    private TableView<PurchaseSales> table;
    private static TextField searchField;

    private static Parent cachedView;
    private static PurchaseSalesListView instance;

    public static Parent getView() {
        if (cachedView != null) {
            instance.loadEntries();
            instance.searchField.clear();
            return cachedView;
        }
        instance = new PurchaseSalesListView();
        cachedView = instance.buildView();
        return cachedView;
    }

    public static javafx.scene.Node getFocusTarget() {
        return searchField;
    }

    public static void clearCache() {
        cachedView = null;
        instance = null;
    }

    private Parent buildView() {

        VBox root = new VBox(16);
        root.setPadding(new Insets(22));
        root.getStyleClass().add("main-content");

        // ── Header ──────────────────────────────────────────────────────
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);
        Label title = new Label("Purchase & Sales Entries");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label(
                "Search, view, edit, and cancel purchase/sales entries.");
        subtitle.getStyleClass().add("section-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hint = new Label("ESC → Back  |  Enter → Edit selected");
        hint.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8;");

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("secondary-button");
        backBtn.setOnAction(e -> {
            DashboardView.loadScreen(PurchaseSalesView.getView());
            Platform.runLater(() -> PurchaseSalesView.getFocusTarget()
                    .requestFocus());
        });

        header.getChildren().addAll(titleBox, spacer, hint, backBtn);

        // ── Search bar ──────────────────────────────────────────────────
        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.getStyleClass().add("premium-panel");
        searchBar.setPadding(new Insets(16));

        searchField = new TextField();
        searchField.setPromptText(
                "Search by bill no, client name, description, PNR, sector...");
        searchField.getStyleClass().add("premium-input");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button refreshBtn = new Button("↺ Refresh");
        refreshBtn.getStyleClass().add("secondary-button");
        refreshBtn.setOnAction(e -> {
            searchField.clear();
            loadEntries();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                loadEntries();
            } else {
                searchEntries();
            }
        });

        searchField.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> {
                    e.consume();
                    DashboardView.loadScreen(PurchaseSalesView.getView());
                    Platform.runLater(() -> PurchaseSalesView.getFocusTarget()
                            .requestFocus());
                }
                case DOWN -> {
                    e.consume();
                    if (!table.getItems().isEmpty()) {
                        table.requestFocus();
                        table.getSelectionModel().selectFirst();
                        table.scrollTo(0);
                    }
                }
                case ENTER -> {
                    e.consume();
                    searchEntries();
                }
            }
        });

        searchBar.getChildren().addAll(searchField, refreshBtn);

        // ── Table ───────────────────────────────────────────────────────
        table = createTable();

        table.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> {
                    e.consume();
                    searchField.requestFocus();
                }
                case ENTER -> {
                    e.consume();
                    PurchaseSales selected =
                            table.getSelectionModel().getSelectedItem();
                    if (selected != null) openEditDialog(selected);
                }
            }
        });

        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("premium-panel");
        tableCard.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().add(table);

        root.getChildren().addAll(header, searchBar, tableCard);

        loadEntries();

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);

        scrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> searchField.requestFocus());
            }
        });

        return scrollPane;
    }

    private TableView<PurchaseSales> createTable() {

        TableView<PurchaseSales> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(620);

        accountRepository.loadCache();

        TableColumn<PurchaseSales, String> billNoCol = new TableColumn<>("Bill No");
        billNoCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBillNo()));
        billNoCol.setPrefWidth(70);
        billNoCol.setMinWidth(70);
        billNoCol.setMaxWidth(90);

        TableColumn<PurchaseSales, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEntryDate() != null
                        ? data.getValue().getEntryDate().toString() : ""));
        dateCol.setPrefWidth(90);
        dateCol.setMinWidth(90);
        dateCol.setMaxWidth(110);

        TableColumn<PurchaseSales, String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(data -> {
            String uuid = data.getValue().getCustomerUuid();
            if (uuid == null || uuid.isBlank()) return new SimpleStringProperty("-");
            Account acc = accountRepository.findByUuid(uuid);
            return new SimpleStringProperty(acc != null ? acc.getName() : "-");
        });
        clientCol.setPrefWidth(150);
        clientCol.setMinWidth(120);

        TableColumn<PurchaseSales, String> creditorCol = new TableColumn<>("Bank / Creditor");
        creditorCol.setCellValueFactory(data -> {
            String uuid = data.getValue().getPurchaseFrom();
            if (uuid == null || uuid.isBlank()) return new SimpleStringProperty("-");
            Account acc = accountRepository.findByUuid(uuid);
            return new SimpleStringProperty(acc != null ? acc.getName() : "-");
        });
        creditorCol.setPrefWidth(130);
        creditorCol.setMinWidth(100);

        TableColumn<PurchaseSales, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPurchaseType()));
        typeCol.setPrefWidth(110);
        typeCol.setMinWidth(90);
        typeCol.setMaxWidth(140);

        TableColumn<PurchaseSales, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDescription()));
        descCol.setPrefWidth(140);
        descCol.setMinWidth(100);

        TableColumn<PurchaseSales, String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPaymentMode()));
        paymentCol.setPrefWidth(75);
        paymentCol.setMinWidth(65);
        paymentCol.setMaxWidth(90);

        TableColumn<PurchaseSales, Number> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQty()));
        qtyCol.setPrefWidth(45);
        qtyCol.setMinWidth(40);
        qtyCol.setMaxWidth(55);

        TableColumn<PurchaseSales, Number> purchaseCol = new TableColumn<>("Purchase ₹");
        purchaseCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getTotalPurchase()));
        purchaseCol.setPrefWidth(95);
        purchaseCol.setMinWidth(80);
        purchaseCol.setMaxWidth(110);

        TableColumn<PurchaseSales, Number> saleCol = new TableColumn<>("Sale ₹");
        saleCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getTotalSale()));
        saleCol.setPrefWidth(90);
        saleCol.setMinWidth(80);
        saleCol.setMaxWidth(110);

        TableColumn<PurchaseSales, Number> profitCol = new TableColumn<>("Profit ₹");
        profitCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getProfit()));
        profitCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    double val = item.doubleValue();
                    setText(String.format("%,.2f", val));
                    setStyle(val >= 0
                            ? "-fx-text-fill: #16a34a; -fx-font-weight: bold;"
                            : "-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            }
        });
        profitCol.setPrefWidth(90);
        profitCol.setMinWidth(80);
        profitCol.setMaxWidth(110);

        TableColumn<PurchaseSales, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    setStyle("CANCELLED".equalsIgnoreCase(item)
                            ? "-fx-text-fill: #dc2626; -fx-font-weight: bold;"
                            : "-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                }
            }
        });
        statusCol.setPrefWidth(75);
        statusCol.setMinWidth(65);
        statusCol.setMaxWidth(90);

        TableColumn<PurchaseSales, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(130);
        actionCol.setMinWidth(130);
        actionCol.setMaxWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(6, editBtn, cancelBtn);
            {
                box.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("secondary-button");
                cancelBtn.getStyleClass().add("danger-button");
                editBtn.setOnAction(e -> {
                    PurchaseSales item = getTableView().getItems().get(getIndex());
                    openEditDialog(item);
                });
                cancelBtn.setOnAction(e -> {
                    PurchaseSales item = getTableView().getItems().get(getIndex());
                    cancelEntry(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableView.getColumns().addAll(
                billNoCol, dateCol, clientCol, creditorCol, typeCol,
                descCol, paymentCol, qtyCol, purchaseCol, saleCol,
                profitCol, statusCol, actionCol
        );

        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                PurchaseSales selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) openEditDialog(selected);
            }
        });

        return tableView;
    }

    private void loadEntries() {
        table.setItems(FXCollections.observableArrayList(repository.findAll()));
    }

    private void searchEntries() {
        String keyword = searchField.getText() == null
                ? "" : searchField.getText().trim();
        if (keyword.isBlank()) { loadEntries(); return; }

        List<Account> matchingAccounts = accountRepository.searchByName(keyword);
        List<String> matchingUuids = matchingAccounts.stream()
                .map(Account::getUuid).toList();

        List<PurchaseSales> byKeyword = repository.search(keyword);

        List<PurchaseSales> all = repository.findAll();
        List<PurchaseSales> byName = all.stream()
                .filter(ps ->
                        matchingUuids.contains(ps.getCustomerUuid())
                                || matchingUuids.contains(ps.getPurchaseFrom()))
                .filter(ps -> byKeyword.stream()
                        .noneMatch(b -> b.getUuid().equals(ps.getUuid())))
                .toList();

        List<PurchaseSales> combined = new java.util.ArrayList<>(byKeyword);
        combined.addAll(byName);
        table.setItems(FXCollections.observableArrayList(combined));
    }

    private void openEditDialog(PurchaseSales item) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Purchase/Sales Entry");
        dialog.setHeaderText("Bill No: " + item.getBillNo());

        ButtonType updateButtonType =
                new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(
                updateButtonType, ButtonType.CANCEL);

        // ── Fields ──────────────────────────────────────────────────────
        DatePicker entryDate = new DatePicker(
                item.getEntryDate() != null ? item.getEntryDate() : LocalDate.now());
        entryDate.setPromptText("Entry Date");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Other Purchasable", "Flight");
        typeBox.setValue(item.getPurchaseType());
        typeBox.setPromptText("Purchase Type");

        SearchableComboBox<Account> creditorBox = new SearchableComboBox<>();
        creditorBox.setItems(FXCollections.observableArrayList(
                accountRepository.findAllCreditors()));
        creditorBox.setPromptText("Select Bank / Creditor");
        selectAccountByUuidOrName(creditorBox, item.getPurchaseFrom());

        SearchableComboBox<Account> customerBox = new SearchableComboBox<>();
        customerBox.setItems(FXCollections.observableArrayList(
                accountRepository.findAllDebtors()));
        customerBox.setPromptText("Select Customer");
        selectAccountByUuidOrName(customerBox, item.getCustomerUuid());

        TextArea descriptionArea = new TextArea(item.getDescription());
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        DatePicker travelDate = new DatePicker(item.getTravelDate());
        travelDate.setPromptText("Travel Date");

        TextField sectorField = new TextField(item.getSector());
        sectorField.setPromptText("e.g. DEL-BOM");

        TextField airlineField = new TextField(item.getAirlineName());
        airlineField.setPromptText("e.g. IndiGo");

        TextField pnrField = new TextField(item.getPnrNo());
        pnrField.setPromptText("PNR Number");

        TextField qtyField = new TextField(String.valueOf(item.getQty()));
        qtyField.setPromptText("Quantity");

        TextField purchaseRateField = new TextField(
                String.valueOf(item.getPurchaseRate()));
        purchaseRateField.setPromptText("Purchase Rate");

        TextField saleRateField = new TextField(
                String.valueOf(item.getSellRate()));
        saleRateField.setPromptText("Sale Rate");

        ComboBox<String> paymentModeBox = new ComboBox<>();
        paymentModeBox.getItems().addAll("CASH", "CREDIT", "CARD");
        paymentModeBox.setValue(item.getPaymentMode());
        paymentModeBox.setPromptText("Payment Mode");

        // ── Tabs ─────────────────────────────────────────────────────────
        TabPane tabPane = new TabPane();

        Tab basicTab     = new Tab("Basic Details");
        Tab flightTab    = new Tab("Flight Details");
        Tab financialTab = new Tab("Financial Details");
        Tab documentsTab = new Tab("Documents");

        basicTab.setClosable(false);
        flightTab.setClosable(false);
        financialTab.setClosable(false);
        documentsTab.setClosable(false);

        documentsTab.setContent(createDocumentsTab(item));

        // ── Basic Details grid ───────────────────────────────────────────
        GridPane basicGrid = createEditGrid();
        basicGrid.add(fieldLabel("Entry Date"),    0, 0); basicGrid.add(entryDate,       1, 0);
        basicGrid.add(fieldLabel("Purchase Type"), 2, 0); basicGrid.add(typeBox,         3, 0);
        basicGrid.add(fieldLabel("Bank/Creditor"), 0, 1); basicGrid.add(creditorBox,     1, 1);
        basicGrid.add(fieldLabel("Customer"),      2, 1); basicGrid.add(customerBox,     3, 1);
        basicGrid.add(fieldLabel("Description"),   0, 2); basicGrid.add(descriptionArea, 1, 2, 3, 1);
        basicTab.setContent(wrapInScroll(basicGrid));

        // ── Flight Details grid ──────────────────────────────────────────
        GridPane flightGrid = createEditGrid();
        flightGrid.add(fieldLabel("Travel Date"), 0, 0); flightGrid.add(travelDate,   1, 0);
        flightGrid.add(fieldLabel("Sector"),      2, 0); flightGrid.add(sectorField,  3, 0);
        flightGrid.add(fieldLabel("Airline"),     0, 1); flightGrid.add(airlineField, 1, 1);
        flightGrid.add(fieldLabel("PNR No"),      2, 1); flightGrid.add(pnrField,     3, 1);
        flightTab.setContent(wrapInScroll(flightGrid));

        // ── Financial Details grid ───────────────────────────────────────
        GridPane financialGrid = createEditGrid();
        financialGrid.add(fieldLabel("Qty"),           0, 0); financialGrid.add(qtyField,          1, 0);
        financialGrid.add(fieldLabel("Payment Mode"),  2, 0); financialGrid.add(paymentModeBox,    3, 0);
        financialGrid.add(fieldLabel("Purchase Rate"), 0, 1); financialGrid.add(purchaseRateField, 1, 1);
        financialGrid.add(fieldLabel("Sale Rate"),     2, 1); financialGrid.add(saleRateField,     3, 1);
        financialTab.setContent(wrapInScroll(financialGrid));

        tabPane.getTabs().addAll(basicTab, flightTab, financialTab, documentsTab);
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().setPrefSize(1200, 800);

        // ── FIX: Full Enter-key flow across all tabs, ending on Update ───
        // Basic Details tab: entryDate -> typeBox -> creditorBox -> customerBox -> description
        setEnterFlowFilter(entryDate.getEditor(),   () -> typeBox.requestFocus());
        setEnterFlowFilter(typeBox,                 () -> creditorBox.requestFocus());
        setEnterFlowFilter(creditorBox.getEditor(), () -> customerBox.requestFocus());
        setEnterFlowFilter(customerBox.getEditor(), () -> descriptionArea.requestFocus());

        // description -> jump to Flight Details tab
        setEnterFlowFilter(descriptionArea, () -> {
            tabPane.getSelectionModel().select(flightTab);
            travelDate.getEditor().requestFocus();
        });

        // Flight Details tab: travelDate -> sector -> airline -> pnr
        setEnterFlowFilter(travelDate.getEditor(), () -> sectorField.requestFocus());
        setEnterFlowFilter(sectorField,            () -> airlineField.requestFocus());
        setEnterFlowFilter(airlineField,           () -> pnrField.requestFocus());

        // pnr -> jump to Financial Details tab
        setEnterFlowFilter(pnrField, () -> {
            tabPane.getSelectionModel().select(financialTab);
            qtyField.requestFocus();
        });

        // Financial Details tab: qty -> paymentMode -> purchaseRate -> saleRate
        setEnterFlowFilter(qtyField,          () -> paymentModeBox.requestFocus());
        setEnterFlowFilter(paymentModeBox,    () -> purchaseRateField.requestFocus());
        setEnterFlowFilter(purchaseRateField, () -> saleRateField.requestFocus());

        // saleRate -> fire the Update button
        setEnterFlowFilter(saleRateField, () -> {
            javafx.scene.Node updateNode = dialog.getDialogPane().lookupButton(updateButtonType);
            if (updateNode instanceof Button updateBtn) {
                updateBtn.fire();
            }
        });

        // ── FIX: Always focus Entry Date when the dialog opens ────────────
        dialog.setOnShown(e ->
                Platform.runLater(() -> entryDate.getEditor().requestFocus()));

        // ── FIX: Type change → defer trip unlink until Update is pressed ──
        final String[] pendingTripUnlink = {null};

        typeBox.valueProperty().addListener((obs, oldType, newType) -> {
            if (!"Flight".equals(newType)
                    && item.getLinkedTripUuid() != null
                    && !item.getLinkedTripUuid().isBlank()) {

                Alert warn = new Alert(Alert.AlertType.CONFIRMATION);
                warn.setTitle("Remove Calendar Link?");
                warn.setHeaderText("Purchase type changed");
                warn.setContentText(
                        "Changing to 'Other Purchasable' will remove this entry's " +
                                "link to the calendar trip and delete the trip from the calendar " +
                                "once you click Update. Continue?");

                warn.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        // Mark for removal — actual deletion happens on Update
                        pendingTripUnlink[0] = item.getLinkedTripUuid();
                    } else {
                        typeBox.setValue(oldType); // revert
                    }
                });

            } else if ("Flight".equalsIgnoreCase(newType)) {
                // Switched back to Flight before Update → cancel pending unlink
                pendingTripUnlink[0] = null;
            }
        });

        // ── Handle result ────────────────────────────────────────────────
        dialog.showAndWait().ifPresent(result -> {
            if (result != updateButtonType) return;

            Account selectedCreditor = creditorBox.getValue();
            if (selectedCreditor == null) { alert("Please select creditor"); return; }

            item.setEntryDate(entryDate.getValue());
            item.setPurchaseType(typeBox.getValue());
            item.setPurchaseFrom(selectedCreditor.getUuid());
            item.setCustomerUuid(customerBox.getValue() != null
                    ? customerBox.getValue().getUuid() : null);
            item.setDescription(descriptionArea.getText());
            item.setTravelDate(travelDate.getValue());
            item.setSector(sectorField.getText());
            item.setAirlineName(airlineField.getText());
            item.setPnrNo(pnrField.getText());
            item.setQty(parseInt(qtyField.getText()));
            item.setPurchaseRate(parseDouble(purchaseRateField.getText()));
            item.setSellRate(parseDouble(saleRateField.getText()));
            item.setPaymentMode(paymentModeBox.getValue());

            // If the user confirmed a type change away from Flight,
            // clear the trip link now so it gets persisted with this update
            if (pendingTripUnlink[0] != null) {
                item.setLinkedTripUuid(null);
            }

            boolean updated = repository.update(item);
            if (updated) {

                if (pendingTripUnlink[0] != null) {
                    // ── Now actually remove the linked trip ──────────────
                    String tripUuid = pendingTripUnlink[0];

                    TripCacheManager.removeTrip(tripUuid);
                    tripRepository.deleteByUuid(tripUuid);

                    Platform.runLater(() -> {
                        DashboardView.refreshCalendar();
                        DashboardView.loadTripsForDate(DashboardView.selectedDate);
                        DashboardView.updateSummaryCards();
                    });

                } else if (item.getLinkedTripUuid() != null
                        && !item.getLinkedTripUuid().isBlank()) {
                    // Reflect update in calendar cache immediately
                    Optional<Trip> linkedTrip =
                            TripCacheManager.findByUuid(item.getLinkedTripUuid());
                    linkedTrip.ifPresent(trip -> {
                        trip.setName(customerBox.getValue() != null
                                ? customerBox.getValue().getName()
                                : item.getDescription());
                        trip.setSector(item.getSector());
                        trip.setAirlineName(item.getAirlineName());
                        trip.setPnrNo(item.getPnrNo());
                        trip.setTripDate(item.getTravelDate());
                        trip.setPurchaseAmount(item.getTotalPurchase());
                        trip.setSellAmount(item.getTotalSale());
                        TripCacheManager.updateTrip(trip);
                    });

                    // Refresh dashboard calendar
                    Platform.runLater(() -> {
                        DashboardView.refreshCalendar();
                        DashboardView.loadTripsForDate(DashboardView.selectedDate);
                        DashboardView.updateSummaryCards();
                    });
                }

                alert("Entry updated successfully");
                loadEntries();
            } else {
                alert("Failed to update entry");
            }
        });
    }

    // ── FIX: Black bold labels ────────────────────────────────────────────
    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: black;");
        return lbl;
    }
    private void setEnterFlowFilter(javafx.scene.Node field, Runnable moveToNext) {
        field.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                Platform.runLater(moveToNext);
            }
        });
    }

    /** Legacy helper kept for any future use — prefer setEnterFlowFilter */
    private void setEnterFlow(TextField field, Runnable moveToNext) {
        field.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                moveToNext.run();
            }
        });
    }

    private void cancelEntry(PurchaseSales item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Entry");
        confirm.setHeaderText("Cancel Bill No: " + item.getBillNo());
        confirm.setContentText("Are you sure you want to cancel this entry?");
        confirm.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;
            boolean cancelled = repository.cancel(item.getUuid());
            if (cancelled) { alert("Entry cancelled successfully"); loadEntries(); }
            else alert("Failed to cancel entry");
        });
    }

    private void selectAccountByUuidOrName(
            SearchableComboBox<Account> combo, String value) {
        if (value == null || value.isBlank()) return;
        for (Account account : combo.getItems()) {
            if (value.equals(account.getUuid())
                    || value.equalsIgnoreCase(account.getName())) {
                combo.setValue(account);
                return;
            }
        }
    }

    private int parseInt(String value) {
        try { return Integer.parseInt(value.trim()); }
        catch (Exception e) { return 0; }
    }

    private double parseDouble(String value) {
        try { return Double.parseDouble(value.trim()); }
        catch (Exception e) { return 0; }
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private GridPane createEditGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(22));
        ColumnConstraints labelCol1 = new ColumnConstraints(130);
        ColumnConstraints fieldCol1 = new ColumnConstraints(220, 260, Double.MAX_VALUE);
        ColumnConstraints labelCol2 = new ColumnConstraints(130);
        ColumnConstraints fieldCol2 = new ColumnConstraints(220, 260, Double.MAX_VALUE);
        fieldCol1.setHgrow(Priority.ALWAYS);
        fieldCol2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelCol1, fieldCol1, labelCol2, fieldCol2);
        return grid;
    }

    private ScrollPane wrapInScroll(GridPane grid) {
        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setFitToHeight(false);
        sp.setPannable(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return sp;
    }

    private Parent createDocumentsTab(PurchaseSales item) {

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));

        if (item.getLinkedTripUuid() == null
                || item.getLinkedTripUuid().isBlank()) {
            Label message = new Label(
                    "Documents can be managed only for entries linked with a calendar trip.");
            message.setStyle("-fx-font-size:14px; -fx-text-fill:#64748b;");
            root.getChildren().add(message);
            return root;
        }

        TableView<TripDocument> documentTable = new TableView<>();
        documentTable.setPrefHeight(360);
        documentTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<TripDocument, String> fileNameCol =
                new TableColumn<>("File Name");
        fileNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFileName()));

        TableColumn<TripDocument, String> createdAtCol =
                new TableColumn<>("Created At");
        createdAtCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCreatedAt() != null
                        ? data.getValue().getCreatedAt().toString() : ""));

        documentTable.getColumns().addAll(fileNameCol, createdAtCol);

        Runnable loadDocuments = () -> documentTable.setItems(
                FXCollections.observableArrayList(
                        tripDocumentRepository.findByTripUuid(
                                item.getLinkedTripUuid())));

        Button addBtn    = new Button("Add Document");
        Button openBtn   = new Button("Open");
        Button deleteBtn = new Button("Delete");
        addBtn.getStyleClass().add("primary-button");
        openBtn.getStyleClass().add("secondary-button");
        deleteBtn.getStyleClass().add("danger-button");

        addBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Document");
            File selectedFile = fc.showOpenDialog(null);
            if (selectedFile == null) return;
            try {
                Path uploadDir = Path.of(System.getProperty("user.home"),
                        "PrabalAppData", "uploads");
                Files.createDirectories(uploadDir);
                String safeFileName =
                        System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = uploadDir.resolve(safeFileName);
                Files.copy(selectedFile.toPath(), targetPath,
                        StandardCopyOption.REPLACE_EXISTING);
                TripDocument document = new TripDocument();
                document.setTripUuid(item.getLinkedTripUuid());
                document.setFileName(selectedFile.getName());
                document.setFilePath(targetPath.toString());
                if (tripDocumentRepository.save(document)) {
                    alert("Document added successfully");
                    loadDocuments.run();
                } else {
                    alert("Failed to save document");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                alert("Failed to add document");
            }
        });

        openBtn.setOnAction(e -> {
            TripDocument selected =
                    documentTable.getSelectionModel().getSelectedItem();
            if (selected == null) { alert("Please select a document"); return; }
            try {
                File file = new File(selected.getFilePath());
                if (!file.exists()) { alert("File not found"); return; }
                Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                ex.printStackTrace();
                alert("Failed to open document");
            }
        });

        deleteBtn.setOnAction(e -> {
            TripDocument selected =
                    documentTable.getSelectionModel().getSelectedItem();
            if (selected == null) { alert("Please select a document"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Document");
            confirm.setHeaderText("Delete " + selected.getFileName() + "?");
            confirm.setContentText("This will remove the document from this trip.");
            confirm.showAndWait().ifPresent(result -> {
                if (result != ButtonType.OK) return;
                if (tripDocumentRepository.deleteByUuid(selected.getUuid())) {
                    try {
                        File file = new File(selected.getFilePath());
                        if (file.exists()) file.delete();
                    } catch (Exception ex) { ex.printStackTrace(); }
                    alert("Document deleted successfully");
                    loadDocuments.run();
                } else {
                    alert("Failed to delete document");
                }
            });
        });

        HBox actions = new HBox(10, addBtn, openBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        root.getChildren().addAll(documentTable, actions);
        loadDocuments.run();
        return root;
    }
}