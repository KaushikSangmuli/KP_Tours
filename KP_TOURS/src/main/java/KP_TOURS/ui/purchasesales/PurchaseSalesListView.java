package KP_TOURS.ui.purchasesales;

import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.model.Account;
import KP_TOURS.model.PurchaseSales;
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

import java.util.List;

public class PurchaseSalesListView {

    private final PurchaseSalesRepository repository =
            new PurchaseSalesRepository();
    private final AccountRepository accountRepository =
            new AccountRepository();
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
                "Search, view, and cancel purchase/sales entries.");
        subtitle.getStyleClass().add("section-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hint = new Label("ESC → Back  |  Enter → Cancel selected");
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
                    if (selected != null) cancelEntry(selected);
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
        actionCol.setPrefWidth(90);
        actionCol.setMinWidth(90);
        actionCol.setMaxWidth(110);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(6, cancelBtn);
            {
                box.setAlignment(Pos.CENTER);
                cancelBtn.getStyleClass().add("danger-button");
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
                if (selected != null) cancelEntry(selected);
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

    private void cancelEntry(PurchaseSales item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Entry");
        confirm.setHeaderText("Cancel Bill No: " + item.getBillNo());
        confirm.setContentText("Are you sure you want to cancel this entry?");
        confirm.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;

            boolean cancelled = repository.cancel(item.getUuid());
            if (!cancelled) {
                alert("Failed to cancel entry");
                return;
            }

            // If this was a Flight entry linked to a calendar trip,
            // remove that trip from cache + DB as well.
            if ("Flight".equalsIgnoreCase(item.getPurchaseType())
                    && item.getLinkedTripUuid() != null
                    && !item.getLinkedTripUuid().isBlank()) {

                String tripUuid = item.getLinkedTripUuid();

                TripCacheManager.removeTrip(tripUuid);
                tripRepository.deleteByUuid(tripUuid);

                Platform.runLater(() -> {
                    DashboardView.refreshCalendar();
                    DashboardView.loadTripsForDate(DashboardView.selectedDate);
                    DashboardView.updateSummaryCards();
                });
            }

            alert("Entry cancelled successfully");
            loadEntries();
        });
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}