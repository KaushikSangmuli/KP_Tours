package KP_TOURS.ui.purchasesales;

import KP_TOURS.model.*;
import KP_TOURS.repository.AccountRepository;
import KP_TOURS.repository.TripDocumentRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import javafx.application.Platform;
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

    private static HBox entryDateField;
    private PurchaseSalesView() {}
    private static AccountRepository accountRepository = new AccountRepository();

    public static javafx.scene.Node getFocusTarget() {
        return entryDateField != null
                ? (TextField) entryDateField.getUserData()
                : null;
    }

    public static void requestFocusSafely(javafx.scene.Node target) {

        if (target == null) return;

        if (target.getScene() != null) {
            Platform.runLater(target::requestFocus);
        } else {
            target.sceneProperty().addListener(new javafx.beans.value.ChangeListener<>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends javafx.scene.Scene> obs,
                                    javafx.scene.Scene oldScene,
                                    javafx.scene.Scene newScene) {
                    if (newScene != null) {
                        obs.removeListener(this);
                        Platform.runLater(target::requestFocus);
                    }
                }
            });
        }
    }
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
        Label subtitle = new Label(
                "Create purchase and sales entries with profit calculation.");
        subtitle.getStyleClass().add("section-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button showEntriesBtn = new Button("Show Entries");
        showEntriesBtn.getStyleClass().add("secondary-button");
        showEntriesBtn.setOnAction(e ->
                DashboardView.loadScreen(PurchaseSalesListView.getView()));

        header.getChildren().addAll(titleBox, spacer, showEntriesBtn);

        VBox topCard = card();
        GridPane topGrid = grid4();

        Label billNoLabel = new Label("Auto Generated");
        billNoLabel.getStyleClass().add("bill-number");

        PurchaseSalesRepository billRepository = new PurchaseSalesRepository();
        billNoLabel.setText(billRepository.getNextBillNo());

        DatePicker entryDatePicker = new DatePicker(LocalDate.now());
        entryDateField = createSmartDateField(entryDatePicker);

        ComboBox<String> typeBox = combo("Other Purchasable", "Flight");
        typeBox.setValue("Other Purchasable");

        Label billNoTitle = new Label("Bill No");
        billNoTitle.getStyleClass().add("form-label");

        HBox billBox = new HBox(8);
        billBox.setAlignment(Pos.CENTER_LEFT);
        billBox.getChildren().addAll(billNoTitle, billNoLabel);

        topGrid.add(billBox,            0, 0, 2, 1);
        topGrid.add(label("Date"),          2, 0); topGrid.add(entryDateField, 3, 0);
        topGrid.add(label("Purchase Type"), 0, 1); topGrid.add(typeBox,        1, 1);

        topCard.getChildren().add(topGrid);

        HBox detailsRow = new HBox(16);
        detailsRow.setFillHeight(true);

        VBox purchaseCard = titledCard("Purchase Details");
        VBox salesCard    = titledCard("Sales Details");
        HBox.setHgrow(purchaseCard, Priority.ALWAYS);
        HBox.setHgrow(salesCard,    Priority.ALWAYS);

        GridPane purchaseGrid = grid2();
        GridPane salesGrid    = grid2();

        SearchableComboBox<Account> bankCreditBox = new SearchableComboBox<>();
        bankCreditBox.setMaxWidth(Double.MAX_VALUE);
        bankCreditBox.setPromptText("Select Bank / Credit");
        bankCreditBox.getStyleClass().add("premium-combo");
        bankCreditBox.setItems(FXCollections.observableArrayList(
                accountRepository.findAllCreditors()));

        TextField purchaseFromField   = input("Purchase from / supplier");
        TextField qtyField            = input("Quantity");
        qtyField.setText("1");
        TextField purchaseAmountField = input("Purchase amount");
        TextField purchaseDescription = input("Description");
        purchaseDescription.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 100 ? change : null));

        purchaseGrid.add(label("Bank / Credit"),   0, 0); purchaseGrid.add(bankCreditBox,       1, 0);
        purchaseGrid.add(label("Purchase From"),   0, 1); purchaseGrid.add(purchaseFromField,   1, 1);
        purchaseGrid.add(label("Description"),     0, 2); purchaseGrid.add(purchaseDescription, 1, 2);
        purchaseGrid.add(label("Purchase Amount"), 0, 3); purchaseGrid.add(purchaseAmountField, 1, 3);
        purchaseGrid.add(label("Qty"),             0, 4); purchaseGrid.add(qtyField,            1, 4);

        ComboBox<String> paymentModeBox = combo("CASH", "CREDIT", "CARD");

        SearchableComboBox<Account> customerBox = new SearchableComboBox<>();
        customerBox.setMaxWidth(Double.MAX_VALUE);
        customerBox.setPromptText("Select Customer");
        customerBox.getStyleClass().add("premium-combo");
        customerBox.setItems(FXCollections.observableArrayList(
                accountRepository.findAllDebtors()));

        TextField refByField      = input("Reference by");
        TextField saleAmountField = input("Sale amount");
        TextField salesDescription = input("Auto copied from purchase description");
        salesDescription.setEditable(false);
        salesDescription.setFocusTraversable(false);
        salesDescription.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 100 ? change : null));

        salesGrid.add(label("Payment Mode"),  0, 0); salesGrid.add(paymentModeBox,   1, 0);
        salesGrid.add(label("Customer Name"), 0, 1); salesGrid.add(customerBox,      1, 1);
        salesGrid.add(label("Ref By"),        0, 2); salesGrid.add(refByField,       1, 2);
        salesGrid.add(label("Description"),   0, 3); salesGrid.add(salesDescription, 1, 3);
        salesGrid.add(label("Sale Amount"),   0, 4); salesGrid.add(saleAmountField,  1, 4);

        salesDescription.textProperty().bind(purchaseDescription.textProperty());

        purchaseCard.getChildren().add(purchaseGrid);
        salesCard.getChildren().add(salesGrid);
        detailsRow.getChildren().addAll(purchaseCard, salesCard);

        // ── Flight Card ──────────────────────────────────────────────────
        VBox flightCard = titledCard("Flight Details");
        GridPane flightGrid = grid4();

        DatePicker travelDatePicker = new DatePicker(LocalDate.now());
        HBox travelDateField = createSmartDateField(travelDatePicker);

        TextField sectorField  = input("Example: BOM → IDR");
        TextField airlineField = input("Airline name");
        TextField pnrField     = input("PNR / Reference No");

        setDateFieldNext(travelDateField, sectorField);

        List<File> selectedDocuments = new ArrayList<>();

        Button uploadDocsBtn = new Button("Upload Documents");
        uploadDocsBtn.getStyleClass().add("secondary-button");
        Label selectedDocsLabel = new Label("No documents selected");
        selectedDocsLabel.getStyleClass().add("section-subtitle");

        uploadDocsBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Flight Documents");
            List<File> files = fc.showOpenMultipleDialog(null);
            if (files == null || files.isEmpty()) return;
            selectedDocuments.clear();
            selectedDocuments.addAll(files);
            selectedDocsLabel.setText(selectedDocuments.size() + " document(s) selected");
        });

        flightGrid.add(label("Travel Date"), 0, 0); flightGrid.add(travelDateField, 1, 0);
        flightGrid.add(label("Sector"),      2, 0); flightGrid.add(sectorField,     3, 0);
        flightGrid.add(label("Airline"),     0, 1); flightGrid.add(airlineField,    1, 1);
        flightGrid.add(label("PNR No"),      2, 1); flightGrid.add(pnrField,        3, 1);
        flightGrid.add(label("Documents"),   0, 2); flightGrid.add(uploadDocsBtn,   1, 2);
        flightGrid.add(selectedDocsLabel,    2, 2, 2, 1);

        flightCard.getChildren().add(flightGrid);
        flightCard.setVisible(false);
        flightCard.setManaged(false);

        typeBox.valueProperty().addListener((obs, o, n) -> {
            boolean isFlight = "Flight".equalsIgnoreCase(n);
            flightCard.setVisible(isFlight);
            flightCard.setManaged(isFlight);
        });

        Button clearBtn = new Button("Clear");
        Button saveBtn  = new Button("Save Entry");
        clearBtn.getStyleClass().add("secondary-button");
        saveBtn.getStyleClass().add("primary-button");

        // ════════════════════════════════════════════════════════════════
        // ENTER FLOW
        // Chain: Date(DD→MM→YYYY) → typeBox → bankCredit → purchaseFrom
        //        → description → purchaseAmount → qty → paymentMode
        //        → customer → refBy → saleAmount
        //        → [if Flight] travelDate → sector → airline → pnr
        //                      → uploadDocs (ask) → saveBtn
        //        → [if Other]  saveBtn
        // ════════════════════════════════════════════════════════════════

        // Date YYYY → typeBox  (via setDateFieldNext)
        setDateFieldNext(entryDateField, typeBox);

        // typeBox → bankCreditBox
        typeBox.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                typeBox.hide();
                Platform.runLater(() -> bankCreditBox.requestFocus());
            }
        });

        // bankCreditBox → purchaseFromField
        bankCreditBox.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                purchaseFromField.requestFocus();
            }
        });

        // purchaseFromField → purchaseDescription
        purchaseFromField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                purchaseDescription.requestFocus();
            }
        });

        // purchaseDescription → purchaseAmountField
        purchaseDescription.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                purchaseAmountField.requestFocus();
            }
        });

        // purchaseAmountField → qtyField
        purchaseAmountField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                qtyField.requestFocus();
            }
        });

        // qtyField → paymentModeBox
        qtyField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                paymentModeBox.requestFocus();
            }
        });

        // paymentModeBox → customerBox
        paymentModeBox.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                paymentModeBox.hide();
                Platform.runLater(() -> customerBox.requestFocus());
            }
        });

        // customerBox → refByField
        customerBox.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                refByField.requestFocus();
            }
        });

        // refByField → saleAmountField
        refByField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                saleAmountField.requestFocus();
            }
        });

        // saleAmountField → if Flight: travelDate DD, else: saveBtn
        saleAmountField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                if ("Flight".equalsIgnoreCase(typeBox.getValue())) {
                    // go to travelDate DD field
                    travelDateField.getChildren().stream()
                            .filter(n -> n instanceof TextField)
                            .findFirst()
                            .ifPresent(n -> Platform.runLater(n::requestFocus));
                } else {
                    saveBtn.requestFocus();
                }
            }
        });

        // ── Flight fields ────────────────────────────────────────────────

        // sector → airline
        sectorField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                airlineField.requestFocus();
            }
        });

        // airline → pnr
        airlineField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                pnrField.requestFocus();
            }
        });

        // pnr → ask upload docs → saveBtn
        pnrField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                // Ask user if they want to upload documents
                Alert uploadAsk = new Alert(Alert.AlertType.CONFIRMATION);
                uploadAsk.setTitle("Upload Documents?");
                uploadAsk.setHeaderText("Do you want to upload flight documents?");
                uploadAsk.setContentText(
                        "Press Yes to select documents, No to skip and proceed to Save.");
                ButtonType yesBtn = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                ButtonType noBtn  = new ButtonType("No",  ButtonBar.ButtonData.NO);
                uploadAsk.getButtonTypes().setAll(yesBtn, noBtn);
                uploadAsk.showAndWait().ifPresent(response -> {
                    if (response == yesBtn) {
                        // trigger file chooser then move to saveBtn
                        FileChooser fc = new FileChooser();
                        fc.setTitle("Select Flight Documents");
                        List<File> files = fc.showOpenMultipleDialog(null);
                        if (files != null && !files.isEmpty()) {
                            selectedDocuments.clear();
                            selectedDocuments.addAll(files);
                            selectedDocsLabel.setText(
                                    selectedDocuments.size() + " document(s) selected");
                        }
                    }
                    // whether Yes or No, move to saveBtn
                    Platform.runLater(() -> saveBtn.requestFocus());
                });
            }
        });

        // ── Save button Enter → confirm → fire ───────────────────────────
        saveBtn.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
                Platform.runLater(PurchaseSalesView::focusEntryDate);
            } else if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Save");
                confirm.setHeaderText("Save Entry");
                confirm.setContentText("Are you sure you want to save this entry?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        saveBtn.fire();
                    } else {
                        Platform.runLater(PurchaseSalesView::focusEntryDate);
                    }
                });
            }
        });

        // ── Footer ───────────────────────────────────────────────────────
        VBox footerBar = new VBox(4);
        footerBar.getStyleClass().add("premium-panel");
        footerTotalLabel.getStyleClass().add("summary-value");
        footerBar.getChildren().add(footerTotalLabel);

        Runnable updateFooter = () -> {
            int    qty           = parseInt(qtyField.getText());
            double purchaseRate  = parseDouble(purchaseAmountField.getText());
            double saleRate      = parseDouble(saleAmountField.getText());
            double purchaseTotal = purchaseRate * qty;
            double saleTotal     = saleRate * qty;
            double profit        = saleTotal - purchaseTotal;
            footerTotalLabel.setText(
                    "Purchase: ₹ " + format(purchaseTotal)
                            + "    |    Sale: ₹ " + format(saleTotal)
                            + "    |    Profit: ₹ " + format(profit));
        };

        purchaseAmountField.textProperty().addListener((o, ov, nv) -> updateFooter.run());
        saleAmountField.textProperty().addListener((o, ov, nv)     -> updateFooter.run());
        qtyField.textProperty().addListener((o, ov, nv)            -> updateFooter.run());

        // ── Clear ────────────────────────────────────────────────────────
        clearBtn.setOnAction(e -> {
            entryDatePicker.setValue(LocalDate.now());
            syncDateFieldFromPicker(entryDateField, entryDatePicker);
            typeBox.setValue("Other Purchasable");
            bankCreditBox.getSelectionModel().clearSelection();
            purchaseFromField.clear();
            purchaseDescription.clear();
            purchaseAmountField.clear();
            qtyField.setText("1");
            paymentModeBox.getSelectionModel().clearSelection();
            customerBox.getSelectionModel().clearSelection();
            refByField.clear();
            saleAmountField.clear();
            travelDatePicker.setValue(LocalDate.now());
            syncDateFieldFromPicker(travelDateField, travelDatePicker);
            sectorField.clear();
            airlineField.clear();
            pnrField.clear();
            selectedDocuments.clear();
            selectedDocsLabel.setText("No documents selected");
            updateFooter.run();
            Platform.runLater(PurchaseSalesView::focusEntryDate);
        });

        // ── Save ─────────────────────────────────────────────────────────
        saveBtn.setOnAction(e -> {

            if (typeBox.getValue() == null) {
                alert("Please select purchase type"); return; }
            if (entryDatePicker.getValue() == null) {
                alert("Please select entry date"); return; }

            int    qty            = parseInt(qtyField.getText());
            double purchaseRate   = parseDouble(purchaseAmountField.getText());
            double saleRate       = parseDouble(saleAmountField.getText());
            double purchaseAmount = purchaseRate * qty;
            double saleAmount     = saleRate * qty;

            if (purchaseAmount <= 0) { alert("Please enter purchase amount"); return; }
            if (saleAmount     <= 0) { alert("Please enter sale amount");     return; }

            PurchaseSales ps = new PurchaseSales();
            ps.setEntryDate(entryDatePicker.getValue());
            ps.setPurchaseType(typeBox.getValue());
            if (bankCreditBox.getValue() == null) {
                alert("Please select bank / credit"); return; }
            ps.setPurchaseFrom(bankCreditBox.getValue().getUuid());
            ps.setCustomerUuid(customerBox.getValue() != null
                    ? customerBox.getValue().getUuid() : null);
            ps.setDescription(purchaseDescription.getText().trim());
            ps.setPaymentMode(paymentModeBox.getValue());
            ps.setQty(qty);
            ps.setPurchaseRate(purchaseRate);
            ps.setSellRate(saleRate);

            if ("Flight".equalsIgnoreCase(typeBox.getValue())) {
                ps.setTravelDate(travelDatePicker.getValue());
                ps.setSector(sectorField.getText().trim());
                ps.setAirlineName(airlineField.getText().trim());
                ps.setPnrNo(pnrField.getText().trim());
            }

            PurchaseSalesRepository purchaseSalesRepository =
                    new PurchaseSalesRepository();

            boolean saved = purchaseSalesRepository.save(ps);
            if (!saved) { alert("Failed to save purchase/sales entry"); return; }

            billNoLabel.setText(new PurchaseSalesRepository().getNextBillNo());

            if ("Flight".equalsIgnoreCase(typeBox.getValue())) {

                Trip trip = new Trip();
                trip.setPurchaseSalesUuid(ps.getUuid());
                trip.setTripDate(ps.getTravelDate() != null
                        ? ps.getTravelDate() : ps.getEntryDate());
                trip.setName(customerBox.getValue() != null
                        ? customerBox.getValue().getName() : "");
                trip.setSector(sectorField.getText().trim());
                trip.setAirlineName(airlineField.getText().trim());
                trip.setSellAmount(saleAmount);
                trip.setPurchaseAmount(purchaseAmount);
                trip.setBookedBy(paymentModeBox.getValue());
                trip.setPnrNo(pnrField.getText().trim());
                trip.setStatus(TripStatus.PENDING);
                trip.setDescription(purchaseDescription.getText().trim());

                TripRepository tripRepository = new TripRepository();
                boolean tripSaved = tripRepository.save(trip);

                if (tripSaved) {
                    ps.setLinkedTripUuid(trip.getUuid());
                    purchaseSalesRepository.update(ps);

                    try {
                        TripDocumentRepository tripDocumentRepository =
                                new TripDocumentRepository();
                        for (File file : selectedDocuments) {
                            Path uploadDir = Path.of(
                                    System.getProperty("user.home"),
                                    "PrabalAppData", "uploads");
                            Files.createDirectories(uploadDir);
                            String safeFileName =
                                    System.currentTimeMillis() + "_" + file.getName();
                            Path targetPath = uploadDir.resolve(safeFileName);
                            Files.copy(file.toPath(), targetPath,
                                    StandardCopyOption.REPLACE_EXISTING);
                            TripDocument document = new TripDocument();
                            document.setTripUuid(trip.getUuid());
                            document.setFileName(file.getName());
                            document.setFilePath(targetPath.toString());
                            tripDocumentRepository.save(document);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        alert("Trip created, but document upload failed.");
                    }

                    // ✅ Add to cache directly (no full reload needed)
                    TripCacheManager.addTrip(trip);

                    // ✅ Refresh dashboard calendar live
                    Platform.runLater(() -> {
                        DashboardView.refreshCalendar();
                        DashboardView.loadTripsForDate(DashboardView.selectedDate);
                        DashboardView.updateSummaryCards();
                    });

                } else {
                    alert("Purchase/Sales saved, but calendar trip was not created.");
                    return;
                }
            }

            alert("Purchase/Sales entry saved successfully");
            clearBtn.fire();
            billNoLabel.setText(purchaseSalesRepository.getNextBillNo());
        });

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getChildren().addAll(clearBtn, saveBtn);

        root.getChildren().addAll(
                header, topCard, detailsRow,
                flightCard, footerBar, actions);

        scroll.setContent(root);
        return scroll;
    }

    private static void focusEntryDate() {
        if (entryDateField != null
                && entryDateField.getUserData() instanceof TextField tf) {
            tf.requestFocus();
        }
    }

    private static void syncDateFieldFromPicker(HBox dateField,
                                                DatePicker picker) {
        if (picker.getValue() == null) return;
        List<TextField> fields = dateField.getChildren().stream()
                .filter(n -> n instanceof TextField)
                .map(n -> (TextField) n)
                .toList();
        if (fields.size() < 3) return;
        fields.get(0).setText(String.format("%02d",
                picker.getValue().getDayOfMonth()));
        fields.get(1).setText(String.format("%02d",
                picker.getValue().getMonthValue()));
        fields.get(2).setText(String.format("%04d",
                picker.getValue().getYear()));
    }

    private static HBox createSmartDateField(DatePicker picker) {

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

        ddField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
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
                    Platform.runLater(PurchaseSalesView::focusEntryDate);
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

        mmField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
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
                    Platform.runLater(PurchaseSalesView::focusEntryDate);
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

        yyyyField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
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
                    syncToPicker.run();
                    Object next = yyyyField.getUserData();
                    if (next instanceof javafx.scene.Node node) {
                        Platform.runLater(node::requestFocus);
                    }
                }
                case BACK_SPACE -> {
                    e.consume();
                    yyyyField.clear();
                    Platform.runLater(() -> mmField.requestFocus());
                }
                case ESCAPE -> {
                    e.consume();
                    Platform.runLater(PurchaseSalesView::focusEntryDate);
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

    private static void setDateFieldNext(HBox dateField,
                                         javafx.scene.Node nextNode) {
        dateField.getChildren().stream()
                .filter(n -> n instanceof TextField)
                .map(n -> (TextField) n)
                .reduce((a, b) -> b)
                .ifPresent(yyyy -> yyyy.setUserData(nextNode));
    }

    private static String extractDigit(javafx.scene.input.KeyCode code) {
        return code.getName()
                .replace("Numpad ", "")
                .replace("Digit", "")
                .trim();
    }

    private static VBox card() {
        VBox card = new VBox(12);
        card.getStyleClass().add("premium-panel");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private static VBox titledCard(String titleText) {
        VBox card = card();
        Label t = new Label(titleText);
        t.getStyleClass().add("ledger-form-title");
        card.getChildren().add(t);
        return card;
    }

    private static GridPane grid4() {
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints l1 = new ColumnConstraints(); l1.setMinWidth(120);
        ColumnConstraints f1 = new ColumnConstraints(); f1.setHgrow(Priority.ALWAYS);
        ColumnConstraints l2 = new ColumnConstraints(); l2.setMinWidth(100);
        ColumnConstraints f2 = new ColumnConstraints(); f2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(l1, f1, l2, f2);
        return grid;
    }

    private static GridPane grid2() {
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(125);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);
        return grid;
    }

    private static TextField input(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("premium-input");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private static ComboBox<String> combo(String... values) {
        ComboBox<String> c = new ComboBox<>(
                FXCollections.observableArrayList(values));
        c.setPromptText("Select");
        c.setMaxWidth(Double.MAX_VALUE);
        c.getStyleClass().add("premium-combo");
        return c;
    }

    private static Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("form-label");
        l.setStyle("-fx-text-fill: black;");
        return l;
    }

    private static double parseDouble(String value) {
        try {
            if (value == null || value.isBlank()) return 0;
            return Double.parseDouble(value.trim());
        } catch (Exception e) { return 0; }
    }

    private static String format(double amount) {
        return String.format("%,.2f", amount);
    }

    private static int parseInt(String value) {
        try {
            if (value == null || value.isBlank()) return 1;
            return Integer.parseInt(value.trim());
        } catch (Exception e) { return 1; }
    }

    private static void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}