package KP_TOURS.ui.purchasesales;

import KP_TOURS.model.*;
import KP_TOURS.repository.AccountRepository;
import KP_TOURS.repository.TripDocumentRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.repository.PurchaseSalesRepository;
import KP_TOURS.repository.TripRepository;
import javafx.stage.FileChooser;
import org.controlsfx.control.SearchableComboBox;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseSalesView {

    private static final Label footerTotalLabel =
            new Label("Purchase: ₹ 0.00    |    Sale: ₹ 0.00    |    Profit: ₹ 0.00");

    private PurchaseSalesView() {
    }

    private static AccountRepository accountRepository =
            new AccountRepository();

    public static Parent getView() {

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox root = new VBox(16);
        root.setPadding(new Insets(22));
        root.getStyleClass().add("main-content");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);

        Label title = new Label("Purchases & Sales");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("Create purchase and sales entries with profit calculation.");
        subtitle.getStyleClass().add("section-subtitle");

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button showEntriesBtn = new Button("Show Entries");
        showEntriesBtn.getStyleClass().add("secondary-button");

        showEntriesBtn.setOnAction(e -> {
            System.out.println("Show Entries clicked");

            DashboardView.loadScreen(
                    PurchaseSalesListView.getView()
            );
        });

        header.getChildren().addAll(titleBox, spacer, showEntriesBtn);

        VBox topCard = card();

        GridPane topGrid = grid4();

        Label billNoLabel = new Label("Auto Generated");
        billNoLabel.getStyleClass().add("bill-number");


        PurchaseSalesRepository billRepository =
                new PurchaseSalesRepository();

        billNoLabel.setText(
                billRepository.getNextBillNo()
        );

        DatePicker entryDate = new DatePicker(LocalDate.now());
        entryDate.getStyleClass().add("premium-input");
        entryDate.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> typeBox = combo("Other Purchasable", "Flight");
        typeBox.setValue("Other Purchasable");

        Label billNoTitle = new Label("Bill No");
        billNoTitle.getStyleClass().add("form-label");

        billNoLabel.getStyleClass().add("bill-number");

        HBox billBox = new HBox(8);
        billBox.setAlignment(Pos.CENTER_LEFT);

        billBox.getChildren().addAll(
                billNoTitle,
                billNoLabel
        );
        topGrid.add(billBox, 0, 0, 2, 1);

        topGrid.add(label("Date"), 2, 0);
        topGrid.add(entryDate, 3, 0);

        topGrid.add(label("Purchase Type"), 0, 1);
        topGrid.add(typeBox, 1, 1);

        topCard.getChildren().add(topGrid);

        HBox detailsRow = new HBox(16);
        detailsRow.setFillHeight(true);

        VBox purchaseCard = titledCard("Purchase Details");
        VBox salesCard = titledCard("Sales Details");

        HBox.setHgrow(purchaseCard, Priority.ALWAYS);
        HBox.setHgrow(salesCard, Priority.ALWAYS);

        GridPane purchaseGrid = grid2();
        GridPane salesGrid = grid2();



        SearchableComboBox<Account> bankCreditBox =
                new SearchableComboBox<>();

        bankCreditBox.setMaxWidth(Double.MAX_VALUE);
        bankCreditBox.setPromptText("Select Bank / Credit");
        bankCreditBox.getStyleClass().add("premium-combo");

        bankCreditBox.setItems(
                FXCollections.observableArrayList(
                        accountRepository.findAllCreditors()
                )
        );

        TextField purchaseFromField = input("Purchase from / supplier");
        TextField qtyField = input("Quantity");
        qtyField.setText("1");
        TextArea purchaseDescription = area("Description");
        TextField purchaseAmountField = input("Purchase amount");
        TextArea purchaseRemark = area("Purchase remark");

        purchaseGrid.add(label("Bank / Credit"), 0, 0);
        purchaseGrid.add(bankCreditBox, 1, 0);

        purchaseGrid.add(label("Purchase From"), 0, 1);
        purchaseGrid.add(purchaseFromField, 1, 1);

        purchaseGrid.add(label("Description"), 0, 2);
        purchaseGrid.add(purchaseDescription, 1, 2);

        purchaseGrid.add(label("Purchase Amount"), 0, 3);
        purchaseGrid.add(purchaseAmountField, 1, 3);

        purchaseGrid.add(label("Remark"), 0, 4);
        purchaseGrid.add(purchaseRemark, 1, 4);

        purchaseGrid.add(label("Qty"), 0, 5);
        purchaseGrid.add(qtyField, 1, 5);

        ComboBox<String> paymentModeBox = combo("CASH", "CREDIT", "CARD");
        SearchableComboBox<Account> customerBox =
                new SearchableComboBox<>();

        customerBox.setMaxWidth(Double.MAX_VALUE);
        customerBox.setPromptText("Select Customer");
        customerBox.getStyleClass().add("premium-combo");

        customerBox.setItems(
                FXCollections.observableArrayList(
                        accountRepository.findAllDebtors()
                )
        );

        TextField refByField = input("Reference by");

        TextArea salesDescription = area("Auto copied from purchase description");
        salesDescription.setEditable(false);

        TextField saleAmountField = input("Sale amount");
        TextArea salesRemark = area("Sales remark");

        salesGrid.add(label("Payment Mode"), 0, 0);
        salesGrid.add(paymentModeBox, 1, 0);

        salesGrid.add(label("Customer Name"), 0, 1);
        salesGrid.add(customerBox, 1, 1);

        salesGrid.add(label("Ref By"), 0, 2);
        salesGrid.add(refByField, 1, 2);

        salesGrid.add(label("Description"), 0, 3);
        salesGrid.add(salesDescription, 1, 3);

        salesGrid.add(label("Sale Amount"), 0, 4);
        salesGrid.add(saleAmountField, 1, 4);

        salesGrid.add(label("Remark"), 0, 5);
        salesGrid.add(salesRemark, 1, 5);

        purchaseDescription.textProperty().addListener(
                (obs, oldVal, newVal) ->
                        salesDescription.setText(newVal)
        );

        purchaseCard.getChildren().add(purchaseGrid);
        salesCard.getChildren().add(salesGrid);

        detailsRow.getChildren().addAll(purchaseCard, salesCard);

        VBox flightCard = titledCard("Flight Details");

        GridPane flightGrid = grid4();

        DatePicker travelDate = new DatePicker(LocalDate.now());
        travelDate.getStyleClass().add("premium-input");
        travelDate.setMaxWidth(Double.MAX_VALUE);

        TextField sectorField = input("Example: BOM → IDR");
        TextField airlineField = input("Airline name");
        TextField pnrField = input("PNR / Reference No");

        List<File> selectedDocuments = new ArrayList<>();

        Button uploadDocsBtn = new Button("Upload Documents");
        uploadDocsBtn.getStyleClass().add("secondary-button");

        Label selectedDocsLabel = new Label("No documents selected");
        selectedDocsLabel.getStyleClass().add("section-subtitle");

        uploadDocsBtn.setOnAction(e -> {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Flight Documents");

            List<File> files =
                    fileChooser.showOpenMultipleDialog(null);

            if (files == null || files.isEmpty()) {
                return;
            }

            selectedDocuments.clear();
            selectedDocuments.addAll(files);

            selectedDocsLabel.setText(
                    selectedDocuments.size() + " document(s) selected"
            );
        });

        flightGrid.add(label("Travel Date"), 0, 0);
        flightGrid.add(travelDate, 1, 0);

        flightGrid.add(label("Sector"), 2, 0);
        flightGrid.add(sectorField, 3, 0);

        flightGrid.add(label("Airline"), 0, 1);
        flightGrid.add(airlineField, 1, 1);

        flightGrid.add(label("PNR No"), 2, 1);
        flightGrid.add(pnrField, 3, 1);

        flightGrid.add(label("Documents"), 0, 2);
        flightGrid.add(uploadDocsBtn, 1, 2);
        flightGrid.add(selectedDocsLabel, 2, 2, 2, 1);



        flightCard.getChildren().add(flightGrid);

        flightCard.setVisible(false);
        flightCard.setManaged(false);

        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> {

            boolean isFlight =
                    "Flight".equalsIgnoreCase(newVal);

            flightCard.setVisible(isFlight);
            flightCard.setManaged(isFlight);
        });

        VBox footerBar = new VBox(4);
        footerBar.getStyleClass().add("premium-panel");

        footerTotalLabel.getStyleClass().add("summary-value");

        footerBar.getChildren().add(footerTotalLabel);

        Runnable updateFooter = () -> {

            int qty =
                    parseInt(qtyField.getText());

            double purchaseRate =
                    parseDouble(purchaseAmountField.getText());

            double saleRate =
                    parseDouble(saleAmountField.getText());

            double purchaseTotal =
                    purchaseRate * qty;

            double saleTotal =
                    saleRate * qty;

            double profit =
                    saleTotal - purchaseTotal;

            footerTotalLabel.setText(
                    "Purchase: ₹ "
                            + format(purchaseTotal)
                            + "    |    Sale: ₹ "
                            + format(saleTotal)
                            + "    |    Profit: ₹ "
                            + format(profit)
            );
        };

        purchaseAmountField.textProperty().addListener(
                (obs, oldVal, newVal) ->
                        updateFooter.run()
        );

        saleAmountField.textProperty().addListener(
                (obs, oldVal, newVal) ->
                        updateFooter.run()
        );

        qtyField.textProperty().addListener(
                (obs, oldVal, newVal) ->
                        updateFooter.run()
        );

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("secondary-button");

        Button saveBtn = new Button("Save Entry");
        saveBtn.getStyleClass().add("primary-button");

        clearBtn.setOnAction(e -> {

            entryDate.setValue(LocalDate.now());
            typeBox.setValue("Other Purchasable");

            bankCreditBox.getSelectionModel().clearSelection();
            purchaseFromField.clear();
            purchaseDescription.clear();
            purchaseAmountField.clear();
            purchaseRemark.clear();

            paymentModeBox.getSelectionModel().clearSelection();
            customerBox.getSelectionModel().clearSelection();
            refByField.clear();
            salesDescription.clear();
            saleAmountField.clear();
            salesRemark.clear();

            travelDate.setValue(LocalDate.now());
            sectorField.clear();
            airlineField.clear();
            pnrField.clear();
            qtyField.setText("1");

            updateFooter.run();
        });

        saveBtn.setOnAction(e -> {

            if (typeBox.getValue() == null) {
                alert("Please select purchase type");
                return;
            }

            if (entryDate.getValue() == null) {
                alert("Please select entry date");
                return;
            }

            int qty = parseInt(qtyField.getText());

            double purchaseRate = parseDouble(purchaseAmountField.getText());
            double saleRate = parseDouble(saleAmountField.getText());

            double purchaseAmount = purchaseRate * qty;
            double saleAmount = saleRate * qty;

            if (purchaseAmount <= 0) {
                alert("Please enter purchase amount");
                return;
            }

            if (saleAmount <= 0) {
                alert("Please enter sale amount");
                return;
            }

            PurchaseSales ps = new PurchaseSales();

            ps.setEntryDate(entryDate.getValue());
            ps.setPurchaseType(typeBox.getValue());
            ps.setPurchaseFrom(bankCreditBox.getValue().getUuid());
            ps.setCustomerUuid(
                    customerBox.getValue() != null
                            ? customerBox.getValue().getUuid()
                            : null
            );
            ps.setDescription(purchaseDescription.getText().trim());
            ps.setPurchaseRemark(purchaseRemark.getText().trim());
            ps.setSalesRemark(salesRemark.getText().trim());
            ps.setPaymentMode(paymentModeBox.getValue());

            ps.setQty(qty);
            ps.setPurchaseRate(purchaseRate);
            ps.setSellRate(saleRate);

            if ("Flight".equalsIgnoreCase(typeBox.getValue())) {
                ps.setTravelDate(travelDate.getValue());
                ps.setSector(sectorField.getText().trim());
                ps.setAirlineName(airlineField.getText().trim());
                ps.setPnrNo(pnrField.getText().trim());
            }

            PurchaseSalesRepository purchaseSalesRepository =
                    new PurchaseSalesRepository();

            System.out.println("=================================");
            System.out.println("UUID           = " + ps.getUuid());
            System.out.println("BILL NO        = " + ps.getBillNo());
            System.out.println("PURCHASE FROM  = " + ps.getPurchaseFrom());
            System.out.println("CUSTOMER UUID  = " + ps.getCustomerUuid());
            System.out.println("PAYMENT MODE   = " + ps.getPaymentMode());
            System.out.println("=================================");

            boolean saved =
                    purchaseSalesRepository.save(ps);
            if (!saved) {
                alert("Failed to save purchase/sales entry");
                return;
            }


            billNoLabel.setText(ps.getBillNo());

            PurchaseSalesRepository purchaseSalesRepositoryForBill =
                    new PurchaseSalesRepository();

            billNoLabel.setText(
                    purchaseSalesRepositoryForBill.getNextBillNo()
            );

            if ("Flight".equalsIgnoreCase(typeBox.getValue())) {

                Trip trip = new Trip();

                trip.setPurchaseSalesUuid(ps.getUuid());
                trip.setTripDate(
                        ps.getTravelDate() != null
                                ? ps.getTravelDate()
                                : ps.getEntryDate()
                );

                trip.setName(
                        customerBox.getValue() != null
                                ? customerBox.getValue().getName()
                                : ""
                );
                trip.setSector(sectorField.getText().trim());
                trip.setAirlineName(airlineField.getText().trim());
                trip.setSellAmount(saleAmount);
                trip.setPurchaseAmount(purchaseAmount);
                trip.setBookedBy(paymentModeBox.getValue());
                trip.setPnrNo(pnrField.getText().trim());
                trip.setStatus(TripStatus.PENDING);
                trip.setDescription(purchaseDescription.getText().trim());

                TripRepository tripRepository =
                        new TripRepository();

                boolean tripSaved =
                        tripRepository.save(trip);

                System.out.println("Trip saved = " + tripSaved);
                System.out.println("Trip UUID = " + trip.getUuid());
                System.out.println("PurchaseSales UUID = " + ps.getUuid());

                if (tripSaved) {

                    ps.setLinkedTripUuid(trip.getUuid());

                    purchaseSalesRepository.update(ps);

                    try {

                        TripDocumentRepository tripDocumentRepository =
                                new TripDocumentRepository();

                        for (File file : selectedDocuments) {

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
                                            + file.getName();

                            Path targetPath =
                                    uploadDir.resolve(safeFileName);

                            Files.copy(
                                    file.toPath(),
                                    targetPath,
                                    StandardCopyOption.REPLACE_EXISTING
                            );

                            TripDocument document =
                                    new TripDocument();

                            document.setTripUuid(
                                    trip.getUuid()
                            );

                            document.setFileName(
                                    file.getName()
                            );

                            document.setFilePath(
                                    targetPath.toString()
                            );

                            tripDocumentRepository.save(document);
                        }

                    } catch (Exception ex) {

                        ex.printStackTrace();

                        alert("Trip created, but document upload failed.");
                    }

                    TripCacheManager.initialize(
                            tripRepository.findAll()
                    );

                } else {

                    alert("Purchase/Sales saved, but calendar trip was not created.");
                    return;
                }
            }

            alert("Purchase/Sales entry saved successfully");

            clearBtn.fire();

            billNoLabel.setText(
                    purchaseSalesRepository.getNextBillNo()
            );

        });

        actions.getChildren().addAll(
                clearBtn,
                saveBtn
        );

        root.getChildren().addAll(
                header,
                topCard,
                detailsRow,
                flightCard,
                footerBar,
                actions
        );

        scroll.setContent(root);

        return scroll;
    }

    private static VBox card() {

        VBox card = new VBox(12);
        card.getStyleClass().add("premium-panel");
        card.setMaxWidth(Double.MAX_VALUE);

        return card;
    }

    private static VBox titledCard(String titleText) {

        VBox card = card();

        Label title = new Label(titleText);
        title.getStyleClass().add("ledger-form-title");

        card.getChildren().add(title);

        return card;
    }

    private static GridPane grid4() {

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints l1 = new ColumnConstraints();
        l1.setMinWidth(120);

        ColumnConstraints f1 = new ColumnConstraints();
        f1.setHgrow(Priority.ALWAYS);

        ColumnConstraints l2 = new ColumnConstraints();
        l2.setMinWidth(100);

        ColumnConstraints f2 = new ColumnConstraints();
        f2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(
                l1,
                f1,
                l2,
                f2
        );

        return grid;
    }

    private static GridPane grid2() {

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints labelCol =
                new ColumnConstraints();

        labelCol.setMinWidth(125);

        ColumnConstraints fieldCol =
                new ColumnConstraints();

        fieldCol.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(
                labelCol,
                fieldCol
        );

        return grid;
    }

    private static TextField input(String prompt) {

        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("premium-input");
        field.setMaxWidth(Double.MAX_VALUE);

        return field;
    }

    private static TextArea area(String prompt) {

        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefRowCount(2);
        area.getStyleClass().add("premium-text-area");
        area.setMaxWidth(Double.MAX_VALUE);

        return area;
    }

    private static ComboBox<String> combo(String... values) {

        ComboBox<String> combo =
                new ComboBox<>(
                        FXCollections.observableArrayList(values)
                );

        combo.setPromptText("Select");
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.getStyleClass().add("premium-combo");

        return combo;
    }

    private static Label label(String text) {

        Label label = new Label(text);
        label.getStyleClass().add("form-label");

        return label;
    }

    private static double parseDouble(String value) {

        try {

            if (value == null || value.isBlank()) {
                return 0;
            }

            return Double.parseDouble(
                    value.trim()
            );

        } catch (Exception e) {

            return 0;
        }
    }

    private static String format(double amount) {

        return String.format("%,.2f", amount);
    }

    private static int parseInt(String value) {
        try {
            if (value == null || value.isBlank()) {
                return 1;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 1;
        }
    }

    private static void alert(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setContentText(message);

        alert.showAndWait();
    }
}