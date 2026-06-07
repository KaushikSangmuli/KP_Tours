package KP_TOURS.ui.purchasesales;

import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.model.Account;
import KP_TOURS.model.PurchaseSales;
import KP_TOURS.model.Trip;
import KP_TOURS.repository.AccountRepository;
import KP_TOURS.repository.PurchaseSalesRepository;
import KP_TOURS.repository.TripRepository;
import KP_TOURS.ui.dashboard.DashboardView;
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

    private final TripRepository tripRepository =new TripRepository();

    private TableView<PurchaseSales> table;
    private TextField searchField;

    public static Parent getView() {
        return new PurchaseSalesListView().buildView();
    }

    private Parent buildView() {

        VBox root = new VBox(16);
        root.setPadding(new Insets(22));
        root.getStyleClass().add("main-content");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);

        Label title = new Label("Purchase & Sales Entries");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("Search, view, edit, and cancel purchase/sales entries.");
        subtitle.getStyleClass().add("section-subtitle");

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("secondary-button");
        backBtn.setOnAction(e -> DashboardView.loadScreen(PurchaseSalesView.getView()));

        header.getChildren().addAll(titleBox, spacer, backBtn);

        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.getStyleClass().add("premium-panel");
        searchBar.setPadding(new Insets(16));

        searchField = new TextField();
        searchField.setPromptText("Search by bill no, description, PNR, sector...");
        searchField.getStyleClass().add("premium-input");
        searchField.setPrefWidth(420);

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("primary-button");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("secondary-button");

        searchBtn.setOnAction(e -> searchEntries());
        refreshBtn.setOnAction(e -> loadEntries());

        searchField.setOnAction(e -> searchEntries());

        searchBar.getChildren().addAll(
                searchField,
                searchBtn,
                refreshBtn
        );

        table = createTable();

        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("premium-panel");
        tableCard.setPadding(new Insets(16));
        tableCard.getChildren().add(table);

        root.getChildren().addAll(
                header,
                searchBar,
                tableCard
        );

        loadEntries();

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);

        return scrollPane;
    }

    private TableView<PurchaseSales> createTable() {

        TableView<PurchaseSales> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(620);

        TableColumn<PurchaseSales, String> billNoCol =
                new TableColumn<>("Bill No");
        billNoCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBillNo())
        );

        TableColumn<PurchaseSales, String> dateCol =
                new TableColumn<>("Date");
        dateCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getEntryDate() != null
                                ? data.getValue().getEntryDate().toString()
                                : ""
                )
        );

        TableColumn<PurchaseSales, String> typeCol =
                new TableColumn<>("Type");
        typeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPurchaseType())
        );

        TableColumn<PurchaseSales, String> paymentModeCol =
                new TableColumn<>("Payment");
        paymentModeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPaymentMode())
        );

        TableColumn<PurchaseSales, Number> qtyCol =
                new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQty())
        );

        TableColumn<PurchaseSales, Number> purchaseCol =
                new TableColumn<>("Purchase");
        purchaseCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getTotalPurchase())
        );

        TableColumn<PurchaseSales, Number> saleCol =
                new TableColumn<>("Sale");
        saleCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getTotalSale())
        );

        TableColumn<PurchaseSales, Number> profitCol =
                new TableColumn<>("Profit");
        profitCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getProfit())
        );

        TableColumn<PurchaseSales, String> statusCol =
                new TableColumn<>("Status");
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus())
        );

        TableColumn<PurchaseSales, Void> actionCol =
                new TableColumn<>("Actions");

        actionCol.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("Edit");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(8, editBtn, cancelBtn);

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

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });

        tableView.getColumns().addAll(
                billNoCol,
                dateCol,
                typeCol,
                paymentModeCol,
                qtyCol,
                purchaseCol,
                saleCol,
                profitCol,
                statusCol,
                actionCol
        );

        return tableView;
    }

    private void loadEntries() {

        List<PurchaseSales> entries =
                repository.findAll();

        table.setItems(
                FXCollections.observableArrayList(entries)
        );
    }

    private void searchEntries() {

        String keyword =
                searchField.getText() == null
                        ? ""
                        : searchField.getText().trim();

        if (keyword.isBlank()) {
            loadEntries();
            return;
        }

        table.setItems(
                FXCollections.observableArrayList(
                        repository.search(keyword)
                )
        );
    }

    private void openEditDialog(PurchaseSales item) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Purchase/Sales Entry");
        dialog.setHeaderText("Bill No: " + item.getBillNo());

        ButtonType updateButtonType =
                new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                updateButtonType,
                ButtonType.CANCEL
        );

        DatePicker entryDate =
                new DatePicker(item.getEntryDate() != null ? item.getEntryDate() : LocalDate.now());

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Other Purchasable", "Flight");
        typeBox.setValue(item.getPurchaseType());

        SearchableComboBox<Account> creditorBox = new SearchableComboBox<>();
        creditorBox.setItems(
                FXCollections.observableArrayList(
                        accountRepository.findAllCreditors()
                )
        );
        selectAccountByUuidOrName(creditorBox, item.getPurchaseFrom());

        SearchableComboBox<Account> customerBox = new SearchableComboBox<>();
        customerBox.setItems(
                FXCollections.observableArrayList(
                        accountRepository.findAllDebtors()
                )
        );
        selectAccountByUuidOrName(customerBox, item.getCustomerUuid());

        TextArea descriptionArea = new TextArea(item.getDescription());

        DatePicker travelDate = new DatePicker(item.getTravelDate());
        TextField sectorField = new TextField(item.getSector());
        TextField airlineField = new TextField(item.getAirlineName());
        TextField pnrField = new TextField(item.getPnrNo());

        TextField qtyField = new TextField(String.valueOf(item.getQty()));
        TextField purchaseRateField = new TextField(String.valueOf(item.getPurchaseRate()));
        TextField saleRateField = new TextField(String.valueOf(item.getSellRate()));

        ComboBox<String> paymentModeBox = new ComboBox<>();
        paymentModeBox.getItems().addAll("CASH", "CREDIT", "CARD");
        paymentModeBox.setValue(item.getPaymentMode());

        TextArea purchaseRemarkArea = new TextArea(item.getPurchaseRemark());
        TextArea salesRemarkArea = new TextArea(item.getSalesRemark());

        TabPane tabPane = new TabPane();

        Tab basicTab = new Tab("Basic Details");
        Tab flightTab = new Tab("Flight Details");
        Tab financialTab = new Tab("Financial Details");
        Tab documentsTab = new Tab("Documents");
        documentsTab.setClosable(false);


        basicTab.setClosable(false);
        flightTab.setClosable(false);
        financialTab.setClosable(false);
        documentsTab.setContent(
                createDocumentsTab(item)
        );

        GridPane basicGrid = createEditGrid();

        int basicRow = 0;

        basicGrid.add(new Label("Date"), 0, basicRow);
        basicGrid.add(entryDate, 1, basicRow);

        basicGrid.add(new Label("Purchase Type"), 2, basicRow);
        basicGrid.add(typeBox, 3, basicRow);

        basicRow++;

        basicGrid.add(new Label("Creditor"), 0, basicRow);
        basicGrid.add(creditorBox, 1, basicRow);

        basicGrid.add(new Label("Customer"), 2, basicRow);
        basicGrid.add(customerBox, 3, basicRow);

        basicRow++;

        basicGrid.add(new Label("Description"), 0, basicRow);
        basicGrid.add(descriptionArea, 1, basicRow, 3, 1);

        basicTab.setContent(wrapInScroll(basicGrid));

        GridPane flightGrid = createEditGrid();

        int flightRow = 0;

        flightGrid.add(new Label("Travel Date"), 0, flightRow);
        flightGrid.add(travelDate, 1, flightRow);

        flightGrid.add(new Label("Sector"), 2, flightRow);
        flightGrid.add(sectorField, 3, flightRow);

        flightRow++;

        flightGrid.add(new Label("Airline"), 0, flightRow);
        flightGrid.add(airlineField, 1, flightRow);

        flightGrid.add(new Label("PNR"), 2, flightRow);
        flightGrid.add(pnrField, 3, flightRow);

        flightTab.setContent(wrapInScroll(flightGrid));

        GridPane financialGrid = createEditGrid();

        int financialRow = 0;

        financialGrid.add(new Label("Qty"), 0, financialRow);
        financialGrid.add(qtyField, 1, financialRow);

        financialGrid.add(new Label("Payment Mode"), 2, financialRow);
        financialGrid.add(paymentModeBox, 3, financialRow);

        financialRow++;

        financialGrid.add(new Label("Purchase Rate"), 0, financialRow);
        financialGrid.add(purchaseRateField, 1, financialRow);

        financialGrid.add(new Label("Sale Rate"), 2, financialRow);
        financialGrid.add(saleRateField, 3, financialRow);

        financialRow++;

        financialGrid.add(new Label("Purchase Remark"), 0, financialRow);
        financialGrid.add(purchaseRemarkArea, 1, financialRow, 3, 1);

        financialRow++;

        financialGrid.add(new Label("Sales Remark"), 0, financialRow);
        financialGrid.add(salesRemarkArea, 1, financialRow, 3, 1);

        financialTab.setContent(wrapInScroll(financialGrid));

        tabPane.getTabs().addAll(
                basicTab,
                flightTab,
                financialTab,
                documentsTab
        );

        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().setPrefSize(1200, 800);

        dialog.setResultConverter(button -> button);

        dialog.showAndWait().ifPresent(result -> {

            if (result != updateButtonType) {
                return;
            }

            Account selectedCreditor = creditorBox.getValue();

            if (selectedCreditor == null) {
                alert("Please select creditor");
                return;
            }

            item.setEntryDate(entryDate.getValue());
            item.setPurchaseType(typeBox.getValue());
            item.setPurchaseFrom(selectedCreditor.getUuid());

            item.setCustomerUuid(
                    customerBox.getValue() != null
                            ? customerBox.getValue().getUuid()
                            : null
            );

            item.setDescription(descriptionArea.getText());
            item.setTravelDate(travelDate.getValue());
            item.setSector(sectorField.getText());
            item.setAirlineName(airlineField.getText());
            item.setPnrNo(pnrField.getText());

            item.setQty(parseInt(qtyField.getText()));
            item.setPurchaseRate(parseDouble(purchaseRateField.getText()));
            item.setSellRate(parseDouble(saleRateField.getText()));
            item.setPaymentMode(paymentModeBox.getValue());

            item.setPurchaseRemark(purchaseRemarkArea.getText());
            item.setSalesRemark(salesRemarkArea.getText());

            boolean updated = repository.update(item);

            if (updated) {
                alert("Entry updated successfully");
                loadEntries();
            } else {
                alert("Failed to update entry");
            }
        });
    }

    private void cancelEntry(PurchaseSales item) {

        Alert confirm =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Cancel Entry");
        confirm.setHeaderText("Cancel Bill No: " + item.getBillNo());
        confirm.setContentText("Are you sure you want to cancel this entry?");

        confirm.showAndWait().ifPresent(result -> {

            if (result != ButtonType.OK) {
                return;
            }

            boolean cancelled =
                    repository.cancel(item.getUuid());

            if (cancelled) {
                alert("Entry cancelled successfully");
                loadEntries();
            } else {
                alert("Failed to cancel entry");
            }
        });
    }

    private void selectAccountByUuidOrName(
            SearchableComboBox<Account> combo,
            String value
    ) {

        if (value == null || value.isBlank()) {
            return;
        }

        for (Account account : combo.getItems()) {

            boolean uuidMatch =
                    value.equals(account.getUuid());

            boolean nameMatch =
                    value.equalsIgnoreCase(account.getName());

            if (uuidMatch || nameMatch) {
                combo.setValue(account);
                return;
            }
        }
    }

    private int parseInt(String value) {

        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDouble(String value) {

        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void alert(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private GridPane createEditGrid() {

        GridPane grid = new GridPane();

        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(22));

        return grid;
    }

    private ScrollPane wrapInScroll(GridPane grid) {

        ScrollPane scrollPane = new ScrollPane(grid);

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;"
        );

        return scrollPane;
    }

    private Parent createDocumentsTab(PurchaseSales item) {

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));

        if (item.getLinkedTripUuid() == null || item.getLinkedTripUuid().isBlank()) {

            Label message = new Label(
                    "Documents can be managed only for entries linked with calendar trip."
            );

            message.setStyle(
                    "-fx-font-size: 14px;" +
                            "-fx-text-fill: #64748b;"
            );

            root.getChildren().add(message);

            return root;
        }

        TableView<TripDocument> documentTable =
                new TableView<>();

        documentTable.setPrefHeight(360);
        documentTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<TripDocument, String> fileNameCol =
                new TableColumn<>("File Name");

        fileNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFileName()
                )
        );

        TableColumn<TripDocument, String> createdAtCol =
                new TableColumn<>("Created At");

        createdAtCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCreatedAt() != null
                                ? data.getValue().getCreatedAt().toString()
                                : ""
                )
        );

        documentTable.getColumns().addAll(
                fileNameCol,
                createdAtCol
        );

        Runnable loadDocuments = () -> {

            List<TripDocument> documents =
                    tripDocumentRepository.findByTripUuid(
                            item.getLinkedTripUuid()
                    );

            documentTable.setItems(
                    FXCollections.observableArrayList(documents)
            );
        };

        Button addBtn = new Button("Add Document");
        addBtn.getStyleClass().add("primary-button");

        Button openBtn = new Button("Open");
        openBtn.getStyleClass().add("secondary-button");

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");

        addBtn.setOnAction(e -> {

            FileChooser fileChooser =
                    new FileChooser();

            fileChooser.setTitle("Select Document");

            File selectedFile =
                    fileChooser.showOpenDialog(null);

            if (selectedFile == null) {
                return;
            }

            try {

                Path uploadDir =
                        Path.of(
                                System.getProperty("user.home"),
                                "PrabalAppData",
                                "uploads"
                        );

                Files.createDirectories(uploadDir);

                String safeFileName =
                        System.currentTimeMillis()
                                + "_"
                                + selectedFile.getName();

                Path targetPath =
                        uploadDir.resolve(safeFileName);

                Files.copy(
                        selectedFile.toPath(),
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                TripDocument document =
                        new TripDocument();

                document.setTripUuid(
                        item.getLinkedTripUuid()
                );

                document.setFileName(
                        selectedFile.getName()
                );

                document.setFilePath(
                        targetPath.toString()
                );

                boolean saved =
                        tripDocumentRepository.save(document);

                if (saved) {
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

            if (selected == null) {
                alert("Please select a document");
                return;
            }

            try {

                File file =
                        new File(selected.getFilePath());

                if (!file.exists()) {
                    alert("File not found");
                    return;
                }

                Desktop.getDesktop().open(file);

            } catch (Exception ex) {

                ex.printStackTrace();
                alert("Failed to open document");
            }
        });

        deleteBtn.setOnAction(e -> {

            TripDocument selected =
                    documentTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                alert("Please select a document");
                return;
            }

            Alert confirm =
                    new Alert(Alert.AlertType.CONFIRMATION);

            confirm.setTitle("Delete Document");
            confirm.setHeaderText("Delete " + selected.getFileName() + "?");
            confirm.setContentText("This will remove the document from this trip.");

            confirm.showAndWait().ifPresent(result -> {

                if (result != ButtonType.OK) {
                    return;
                }

                boolean dbDeleted =
                        tripDocumentRepository.deleteByUuid(
                                selected.getUuid()
                        );

                if (dbDeleted) {

                    try {
                        File file =
                                new File(selected.getFilePath());

                        if (file.exists()) {
                            file.delete();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    alert("Document deleted successfully");
                    loadDocuments.run();

                } else {

                    alert("Failed to delete document");
                }
            });
        });

        HBox actions =
                new HBox(10, addBtn, openBtn, deleteBtn);

        actions.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                documentTable,
                actions
        );

        loadDocuments.run();

        return root;
    }
}