package KP_TOURS.ui.dashboard;

import KP_TOURS.backup.BackupManager;
import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.model.Trip;
import KP_TOURS.ui.trip.TripFormDialog;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;

public class DashboardView {

    // =========================================================
    // STATE
    // =========================================================

    private static final GridPane calendarGrid =
            new GridPane();

    private static final TableView<Trip> tripTable =
            new TableView<>();

    private static final Label monthLabel =
            new Label();

    private static final Label selectedDateLabel =
            new Label();

    private static YearMonth currentMonth =
            YearMonth.now();

    private static LocalDate selectedDate =
            LocalDate.now();
    private static final TextField localSearchField =
            new TextField();

    private static final TextField globalSearchField =
            new TextField();

    private static boolean globalSearchMode =
            false;

    // =========================================================
    // HEADER LABELS
    // =========================================================

    private static final Label totalTripsLabel =
            summaryValue("0");

    private static final Label totalSellLabel =
            summaryValue("0");

    private static final Label totalPurchaseLabel =
            summaryValue("0");

    private static final Label totalProfitLabel =
            summaryValue("0");

    // =========================================================
    // MAIN VIEW
    // =========================================================

    public static Parent getView() {

        BorderPane root =
                new BorderPane();

        root.setPadding(new Insets(15));

        root.setTop(buildHeader());

        root.setCenter(buildCenter());

        root.getStyleClass().add("dashboard-root");

        refreshCalendar();

        initializeTable();
        loadTripsForDate(selectedDate);
        updateSummaryCards();

        return root;
    }

    // =========================================================
    // HEADER
    // =========================================================

    private static VBox buildHeader() {

        VBox wrapper =
                new VBox(15);

        HBox top =
                new HBox(15);

        top.setAlignment(Pos.CENTER_LEFT);

        top.getChildren().addAll(
                summaryCard("Total Trips", totalTripsLabel),
                summaryCard("Total Sell", totalSellLabel),
                summaryCard("Total Purchase", totalPurchaseLabel),
                summaryCard("Total Profit", totalProfitLabel)
        );

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backupButton =
                new Button("Backup");

        backupButton.setOnAction(e -> {

            BackupManager.createBackup();
        });

        Button restoreButton =
                new Button("Restore");

        restoreButton.setOnAction(e -> {

            BackupManager.restoreBackup();

            // reload UI after restore

            loadTripsForDate(selectedDate);

            refreshCalendar();

            updateSummaryCards();
        });

        top.getChildren().addAll(
                spacer,
                backupButton,
                restoreButton
        );

        // =========================================
// GLOBAL SEARCH
// =========================================

        globalSearchField.setPromptText(
                "Search All Trips..."
        );

        globalSearchField.setPrefWidth(300);

        globalSearchField.textProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null || newVal.isBlank()) {

                globalSearchMode = false;

                loadTripsForDate(selectedDate);

                return;
            }

            globalSearchMode = true;

            searchGlobally(newVal);
        });

        HBox searchBarWrapper =
                new HBox(globalSearchField);

        searchBarWrapper.setAlignment(Pos.CENTER_RIGHT);

        wrapper.getChildren().addAll(
                top,
                searchBarWrapper
        );

        return wrapper;
    }

    // =========================================================
    // CENTER SECTION
    // =========================================================

    private static SplitPane buildCenter() {

        SplitPane splitPane =
                new SplitPane();

        splitPane.setDividerPositions(0.45);

        splitPane.getItems().addAll(
                buildCalendarSection(),
                buildTripSection()
        );

        return splitPane;
    }

    // =========================================================
    // CALENDAR SECTION
    // =========================================================

    private static VBox buildCalendarSection() {

        VBox root =
                new VBox(15);

        root.setPadding(new Insets(15));

        HBox controls =
                new HBox(15);

        controls.setAlignment(Pos.CENTER);

        Button prevButton =
                new Button("◀ Previous");

        Button nextButton =
                new Button("Next ▶");

        monthLabel.getStyleClass().add("month-label");

        prevButton.setOnAction(e -> {

            currentMonth =
                    currentMonth.minusMonths(1);

            refreshCalendar();
            updateSummaryCards();
        });

        nextButton.setOnAction(e -> {

            currentMonth =
                    currentMonth.plusMonths(1);

            refreshCalendar();
            updateSummaryCards();
        });

        controls.getChildren().addAll(
                prevButton,
                monthLabel,
                nextButton
        );

        calendarGrid.setHgap(8);
        calendarGrid.setVgap(8);

        root.getChildren().addAll(
                controls,
                calendarGrid
        );

        VBox.setVgrow(
                calendarGrid,
                Priority.ALWAYS
        );

        return root;
    }

    // =========================================================
    // TRIP SECTION
    // =========================================================

    private static VBox buildTripSection() {

        VBox root =
                new VBox(15);

        root.setPadding(new Insets(15));

        HBox top =
                new HBox(15);

        top.setAlignment(Pos.CENTER_LEFT);

        selectedDateLabel.setText(
                "Today's Trips"
        );

        selectedDateLabel.getStyleClass()
                .add("section-title");

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addTripButton =
                new Button("+ Add Trip");

        top.getChildren().addAll(
                selectedDateLabel,
                spacer,
                addTripButton
        );

        addTripButton.setOnAction(e -> {

            TripFormDialog.openAddDialog( selectedDate, () -> {

                loadTripsForDate(selectedDate);

                refreshCalendar();
                updateSummaryCards();
            });
        });

        // =========================================
// LOCAL SEARCH
// =========================================

        localSearchField.setPromptText(
                "Search Selected Date Trips..."
        );

        localSearchField.textProperty().addListener((obs, oldVal, newVal) -> {

            if (globalSearchMode) {
                return;
            }

            if (newVal == null || newVal.isBlank()) {

                loadTripsForDate(selectedDate);

                return;
            }

            searchWithinSelectedDate(newVal);
        });

        root.getChildren().addAll(
                top,
                localSearchField,
                tripTable
        );

        VBox.setVgrow(
                tripTable,
                Priority.ALWAYS
        );

        return root;
    }

    // =========================================================
    // CALENDAR RENDER
    // =========================================================

    private static void refreshCalendar() {

        calendarGrid.getChildren().clear();

        monthLabel.setText(
                currentMonth.getMonth().name()
                        + " "
                        + currentMonth.getYear()
        );

        LocalDate firstDay =
                currentMonth.atDay(1);

        int daysInMonth =
                currentMonth.lengthOfMonth();

        int startDay =
                firstDay.getDayOfWeek().getValue();

        int row = 1;
        int col = startDay - 1;

        // Week Headers

        String[] weekDays = {
                "Mon", "Tue", "Wed",
                "Thu", "Fri", "Sat", "Sun"
        };

        for (int i = 0; i < weekDays.length; i++) {

            Label dayLabel =
                    new Label(weekDays[i]);

            dayLabel.getStyleClass()
                    .add("calendar-header");

            dayLabel.setPrefWidth(70);

            calendarGrid.add(
                    dayLabel,
                    i,
                    0
            );
        }

        // Dates

        for (int day = 1; day <= daysInMonth; day++) {

            LocalDate date =
                    currentMonth.atDay(day);

            VBox cell =
                    buildCalendarCell(date);

            calendarGrid.add(
                    cell,
                    col,
                    row
            );

            col++;

            if (col > 6) {

                col = 0;

                row++;
            }
        }
    }

    // =========================================================
    // CALENDAR CELL
    // =========================================================


    private static VBox buildCalendarCell(LocalDate date) {

        VBox cell =
                new VBox(6);

        cell.setPadding(new Insets(8));

        cell.setPrefSize(90, 90);

        cell.getStyleClass()
                .add("calendar-cell");

        // =========================================
        // DATE LABEL
        // =========================================

        Label dateLabel =
                new Label(
                        String.valueOf(
                                date.getDayOfMonth()
                        )
                );

        dateLabel.getStyleClass()
                .add("calendar-date");

        // =========================================
        // TRIP COUNT
        // =========================================

        long tripCount =
                TripCacheManager
                        .getTripCache()
                        .stream()
                        .filter(trip ->

                                trip.getTripDate() != null
                                        &&

                                        trip.getTripDate().equals(date)
                        )
                        .count();

        Label tripCountLabel =
                new Label();

        if (tripCount > 0){
            tripCountLabel.setText(
                    tripCount + " Trips"
            );
        }

        tripCountLabel.getStyleClass()
                .add("trip-count-label");

        // =========================================
        // TODAY HIGHLIGHT
        // =========================================

        if (date.equals(LocalDate.now())) {

            cell.setStyle(
                    "-fx-border-color: #3b82f6;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 8;"
            );
        }

        // =========================================
        // SELECT DATE
        // =========================================

        cell.setOnMouseClicked(e -> {

            selectedDate = date;

            selectedDateLabel.setText(
                    "Trips - " + date
            );

            loadTripsForDate(date);
        });

        // =========================================
        // ADD TO CELL
        // =========================================

        cell.getChildren().add(dateLabel);

        if (tripCountLabel != null) {

            cell.getChildren().add(tripCountLabel);
        }
        return cell;
    }

    // =========================================================
    // TABLE
    // =========================================================

    private static void initializeTable() {

        TableColumn<Trip, String> naam =
                new TableColumn<>("Naam");

        naam.setCellValueFactory(cell ->

                new SimpleStringProperty(
                        cell.getValue().getNaam()
                )
        );

        TableColumn<Trip, String> sector =
                new TableColumn<>("Sector");



        TableColumn<Trip, String> airline =
                new TableColumn<>("Airline");

        TableColumn<Trip, String> pnr =
                new TableColumn<>("PNR");

        TableColumn<Trip, String> status =
                new TableColumn<>("Status");

        TableColumn<Trip, Double> profit =
                new TableColumn<>("Profit");

        TableColumn<Trip, Void> action =
                new TableColumn<>("Action");

        TableColumn<Trip, Void> documentAction =
                new TableColumn<>("Document");

        naam.setPrefWidth(150);
        sector.setPrefWidth(120);
        airline.setPrefWidth(150);
        pnr.setPrefWidth(120);
        status.setPrefWidth(100);
        profit.setPrefWidth(100);
        action.setPrefWidth(120);
        documentAction.setPrefWidth(220);


        naam.setCellValueFactory(cell ->

                new SimpleStringProperty(
                        cell.getValue().getNaam()
                )
        );


        profit.setCellValueFactory(cell ->

                new SimpleObjectProperty<>(
                        cell.getValue()
                                .getProfit()
                )
        );

        status.setCellValueFactory(cell ->

                new SimpleStringProperty(
                        cell.getValue()
                                .getStatus()
                                .name()
                )
        );

        pnr.setCellValueFactory(cell ->

                new SimpleStringProperty(
                        cell.getValue().getPnrNo()
                )
        );

        airline.setCellValueFactory(cell ->

                new SimpleStringProperty(
                        cell.getValue().getAirlineName()
                )
        );
        sector.setCellValueFactory(cell ->

                new SimpleStringProperty(
                        cell.getValue().getSector()
                )
        );

        action.setCellFactory(param -> new TableCell<>() {

            private final Button editButton =
                    new Button("Edit");

            {

                editButton.setOnAction(event -> {

                    Trip trip =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    TripFormDialog.openEditDialog(
                            trip,
                            () -> {

                                loadTripsForDate(selectedDate);

                                refreshCalendar();
                                updateSummaryCards();
                            }
                    );
                });
            }

            @Override
            protected void updateItem(
                    Void item,
                    boolean empty
            ) {

                super.updateItem(item, empty);

                if (empty) {

                    setGraphic(null);

                } else {

                    setGraphic(editButton);
                }
            }
        });


        documentAction.setCellFactory(param -> new TableCell<>() {

            private final Button viewButton =
                    new Button("View");

            private final Button downloadButton =
                    new Button("Download");

            private final HBox container =
                    new HBox(10, viewButton, downloadButton);

            {

                // =========================================
                // VIEW
                // =========================================

                viewButton.setOnAction(event -> {

                    try {

                        Trip trip =
                                getTableView()
                                        .getItems()
                                        .get(getIndex());

                        if (trip.getDocumentPath() == null
                                || trip.getDocumentPath().isBlank()) {

                            alert("No document attached");

                            return;
                        }

                        File file =
                                new File(
                                        trip.getDocumentPath()
                                );

                        if (!file.exists()) {

                            alert("Document not found");

                            return;
                        }

                        Desktop.getDesktop().open(file);

                    } catch (Exception ex) {

                        ex.printStackTrace();

                        alert("Failed to open document");
                    }
                });

                // =========================================
                // DOWNLOAD
                // =========================================

                downloadButton.setOnAction(event -> {

                    try {

                        Trip trip =
                                getTableView()
                                        .getItems()
                                        .get(getIndex());

                        if (trip.getDocumentPath() == null
                                || trip.getDocumentPath().isBlank()) {

                            alert("No document attached");

                            return;
                        }

                        File source =
                                new File(
                                        trip.getDocumentPath()
                                );

                        if (!source.exists()) {

                            alert("Document not found");

                            return;
                        }

                        javafx.stage.FileChooser chooser =
                                new javafx.stage.FileChooser();

                        chooser.setInitialFileName(
                                source.getName()
                        );

                        File destination =
                                chooser.showSaveDialog(null);

                        if (destination == null) {

                            return;
                        }

                        Files.copy(
                                source.toPath(),
                                destination.toPath(),
                                StandardCopyOption.REPLACE_EXISTING
                        );

                        alert("Document downloaded successfully");

                    } catch (Exception ex) {

                        ex.printStackTrace();

                        alert("Failed to download document");
                    }
                });
            }

            @Override
            protected void updateItem(
                    Void item,
                    boolean empty
            ) {

                super.updateItem(item, empty);

                if (empty) {

                    setGraphic(null);

                } else {

                    setGraphic(container);
                }
            }
        });

        tripTable.getColumns().addAll(
                naam,
                sector,
                airline,
                pnr,
                status,
                profit,
                action,
                documentAction
        );

        tripTable.setItems(
                FXCollections.observableArrayList()
        );


    }


    // =========================================================
    // LOAD TRIPS
    // =========================================================

    private static void loadTripsForDate(LocalDate date) {

        tripTable.getItems().clear();

        tripTable.getItems().addAll(

                TripCacheManager
                        .getTripCache()
                        .stream()
                        .filter(trip ->
                                trip.getTripDate() != null
                                        &&
                                        trip.getTripDate().equals(date)
                        )
                        .toList()
        );
    }

    private static void updateSummaryCards() {

        var monthlyTrips =
                TripCacheManager
                        .getTripCache()
                        .stream()
                        .filter(trip ->

                                trip.getTripDate() != null
                                        &&

                                        trip.getTripDate().getMonth()
                                                == currentMonth.getMonth()

                                        &&

                                        trip.getTripDate().getYear()
                                                == currentMonth.getYear()
                        )
                        .toList();

        // =========================================
        // TOTAL TRIPS
        // =========================================

        int totalTrips =
                monthlyTrips.size();

        // =========================================
        // TOTAL SELL
        // =========================================

        double totalSell =
                monthlyTrips.stream()
                        .mapToDouble(Trip::getSellAmount)
                        .sum();

        // =========================================
        // TOTAL PURCHASE
        // =========================================

        double totalPurchase =
                monthlyTrips.stream()
                        .mapToDouble(Trip::getPurchaseAmount)
                        .sum();

        // =========================================
        // TOTAL PROFIT
        // =========================================

        double totalProfit =
                monthlyTrips.stream()
                        .mapToDouble(Trip::getProfit)
                        .sum();

        // =========================================
        // UPDATE LABELS
        // =========================================

        totalTripsLabel.setText(
                String.valueOf(totalTrips)
        );

        totalSellLabel.setText(
                "₹ " + formatAmount(totalSell)
        );

        totalPurchaseLabel.setText(
                "₹ " + formatAmount(totalPurchase)
        );

        totalProfitLabel.setText(
                "₹ " + formatAmount(totalProfit)
        );
    }

    private static String formatAmount(double value) {

        return String.format("%,.2f", value);
    }
    // =========================================================
    // COMPONENTS
    // =========================================================


    private static VBox summaryCard(
            String title,
            Label value
    ) {

        VBox card =
                new VBox(10);

        card.setPadding(new Insets(15));

        card.setPrefWidth(180);

        card.getStyleClass()
                .add("summary-card");

        Label titleLabel =
                new Label(title);

        titleLabel.getStyleClass()
                .add("summary-title");

        card.getChildren().addAll(
                titleLabel,
                value
        );

        return card;
    }

    private static Label summaryValue(String value) {

        Label label =
                new Label(value);

        label.getStyleClass()
                .add("summary-value");

        return label;
    }

    private static void searchWithinSelectedDate(String keyword) {

        String search =
                keyword.toLowerCase();

        tripTable.getItems().clear();

        tripTable.getItems().addAll(

                TripCacheManager
                        .getTripCache()
                        .stream()

                        .filter(trip ->

                                trip.getTripDate() != null
                                        &&

                                        trip.getTripDate()
                                                .equals(selectedDate)
                        )

                        .filter(trip -> matchesSearch(trip, search))

                        .toList()
        );
    }

    private static void searchGlobally(String keyword) {

        String search =
                keyword.toLowerCase();

        tripTable.getItems().clear();

        tripTable.getItems().addAll(

                TripCacheManager
                        .getTripCache()
                        .stream()

                        .filter(trip ->
                                matchesSearch(trip, search)
                        )

                        .toList()
        );
    }

    private static boolean matchesSearch(
            Trip trip,
            String search
    ) {

        return contains(
                trip.getNaam(),
                search
        )

                ||

                contains(
                        trip.getSector(),
                        search
                )

                ||

                contains(
                        trip.getAirlineName(),
                        search
                )

                ||

                contains(
                        trip.getPnrNo(),
                        search
                )

                ||

                contains(
                        trip.getStatus().name(),
                        search
                );
    }

    private static boolean contains(
            String value,
            String search
    ) {

        return value != null
                &&
                value.toLowerCase()
                        .contains(search);
    }

    private static void alert(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setContentText(message);

        alert.showAndWait();
    }


}