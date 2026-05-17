package KP_TOURS.ui.dashboard;

import KP_TOURS.backup.BackupManager;
import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.model.Trip;
import KP_TOURS.model.TripDocument;
import KP_TOURS.model.TripStatus;
import KP_TOURS.repository.TripDocumentRepository;
import KP_TOURS.repository.TripRepository;
import KP_TOURS.ui.trip.TripFormDialog;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardView {

    private static final GridPane calendarGrid = new GridPane();
    private static final TableView<Trip> tripTable = new TableView<>();

    private static final Label monthLabel = new Label();
    private static final Label monthOverviewLabel = new Label();
    private static final Label selectedDateLabel = new Label();

    private static YearMonth currentMonth = YearMonth.now();
    private static LocalDate selectedDate = LocalDate.now();

    private static final TextField localSearchField = new TextField();
    private static final TextField globalSearchField = new TextField();

    private static boolean globalSearchMode = false;

    private static final Label totalTripsLabel = summaryValue("0");
    private static final Label totalSellLabel = summaryValue("₹ 0.00");
    private static final Label totalPurchaseLabel = summaryValue("₹ 0.00");
    private static final Label totalProfitLabel = summaryValue("₹ 0.00");
    private static final Label pendingTripsLabel = summaryValue("0");
    private static final Label cancelledTripsLabel = summaryValue("0");

    public static Parent getView() {

        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");

        root.setTop(buildHeader());
        root.setCenter(buildCenter());

        refreshCalendar();
        initializeTable();
        loadTripsForDate(selectedDate);
        updateSummaryCards();

        return root;
    }

    private static VBox buildHeader() {

        VBox wrapper = new VBox(22);
        wrapper.getStyleClass().add("premium-header");

        HBox titleRow = new HBox(18);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        ImageView logo =
                icon("logo.png", 50);

        logo.getStyleClass().add("app-logo");


        VBox titleBox = new VBox(2);

        Label appTitle = new Label("AK Technologies ");
        appTitle.getStyleClass().add("app-title");

        Label subtitle = new Label("Travel Desk Management");
        subtitle.getStyleClass().add("app-subtitle");

        titleBox.getChildren().addAll(appTitle, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        monthOverviewLabel.getStyleClass().add("month-overview");

        Button prevButton = new Button("‹ Previous");
        prevButton.getStyleClass().add("header-button");
        prevButton.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
            updateSummaryCards();
        });

        Button nextButton = new Button("Next ›");
        nextButton.getStyleClass().add("header-button");
        nextButton.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
            updateSummaryCards();
        });

        Button backupButton = new Button("Backup");
        backupButton.getStyleClass().add("header-button");
        backupButton.setOnAction(e -> BackupManager.createBackup());

        Button restoreButton = new Button("Restore");
        restoreButton.getStyleClass().add("header-button");
        restoreButton.setOnAction(e -> {
            BackupManager.restoreBackup();
            loadTripsForDate(selectedDate);
            refreshCalendar();
            updateSummaryCards();
        });

        titleRow.getChildren().addAll(
                logo,
                titleBox,
                spacer,
                monthOverviewLabel,
                prevButton,
                nextButton,
                backupButton,
                restoreButton
        );

        GridPane cardGrid = new GridPane();
        cardGrid.setHgap(16);
        cardGrid.setVgap(16);
        cardGrid.getStyleClass().add("summary-grid");

        VBox card1 = summaryCard("trip.png", "Total Trips", totalTripsLabel);
        VBox card2 = summaryCard("money.png", "Total Sell", totalSellLabel);
        VBox card3 = summaryCard("purchase.png", "Total Purchase", totalPurchaseLabel);
        VBox card4 = summaryCard("profit.png", "Total Profit", totalProfitLabel);
        VBox card5 = summaryCard("pending.png", "Pending", pendingTripsLabel);
        VBox card6 = summaryCard("cancelled.png", "Cancelled", cancelledTripsLabel);

        VBox[] cards = {
                card1,
                card2,
                card3,
                card4,
                card5,
                card6
        };

        Runnable refreshGrid = () -> {

            cardGrid.getChildren().clear();
            cardGrid.getColumnConstraints().clear();

            double width = wrapper.getWidth();

            int columns;

            if (width < 750) {
                columns = 2;
            } else if (width < 1100) {
                columns = 3;
            } else {
                columns = 6;
            }

            for (int i = 0; i < columns; i++) {
                ColumnConstraints col = new ColumnConstraints();
                col.setPercentWidth(100.0 / columns);
                col.setHgrow(Priority.ALWAYS);
                cardGrid.getColumnConstraints().add(col);
            }

            for (int i = 0; i < cards.length; i++) {
                cardGrid.add(cards[i], i % columns, i / columns);
            }
        };

        wrapper.widthProperty().addListener((obs, oldVal, newVal) -> refreshGrid.run());

        refreshGrid.run();

        wrapper.getChildren().addAll(
                titleRow,
                cardGrid
        );

        return wrapper;
    }

    private static HBox buildCenter() {

        HBox center = new HBox(22);
        center.getStyleClass().add("main-content");

        VBox calendarSection = buildCalendarSection();
        VBox tripSection = buildTripSection();

        HBox.setHgrow(tripSection, Priority.ALWAYS);

        center.getChildren().addAll(
                calendarSection,
                tripSection
        );

        return center;
    }

    private static VBox buildCalendarSection() {

        VBox root = new VBox(18);
        root.getStyleClass().add("premium-panel");
        root.setPrefWidth(470);

        Label title = new Label("Calendar");
        title.getStyleClass().add("section-title");

        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER);

        Button prev = new Button("‹");
        prev.getStyleClass().add("calendar-nav-button");
        prev.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
            updateSummaryCards();
        });

        monthLabel.getStyleClass().add("calendar-month-title");

        Button next = new Button("›");
        next.getStyleClass().add("calendar-nav-button");
        next.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
            updateSummaryCards();
        });

        controls.getChildren().addAll(
                prev,
                monthLabel,
                next
        );

        calendarGrid.setHgap(0);
        calendarGrid.setVgap(0);
        calendarGrid.getStyleClass().add("calendar-grid");

        Label hint = new Label("● Dots indicate number of trips on that day");
        hint.getStyleClass().add("calendar-hint");

        root.getChildren().addAll(
                title,
                controls,
                calendarGrid,
                hint
        );

        return root;
    }

    private static VBox buildTripSection() {

        VBox root = new VBox(16);
        root.getStyleClass().add("premium-panel");

        HBox top = new HBox(14);
        top.setAlignment(Pos.CENTER_LEFT);

        selectedDateLabel.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        globalSearchField.setPromptText("Search All Trips...");
        globalSearchField.getStyleClass().add("premium-search");
        globalSearchField.setPrefWidth(250);

        globalSearchField.textProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null || newVal.isBlank()) {
                globalSearchMode = false;
                loadTripsForDate(selectedDate);
                return;
            }

            globalSearchMode = true;
            searchGlobally(newVal);
        });

        Button addTripButton = new Button("+ Add Trip");
        addTripButton.getStyleClass().add("primary-button");

        addTripButton.setOnAction(e -> TripFormDialog.openAddDialog(selectedDate, () -> {
            loadTripsForDate(selectedDate);
            refreshCalendar();
            updateSummaryCards();
        }));

        top.getChildren().addAll(
                selectedDateLabel,
                spacer,
                globalSearchField,
                addTripButton
        );

        localSearchField.setPromptText("Search Selected Date Trips...");
        localSearchField.getStyleClass().add("premium-search");

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

        tripTable.getStyleClass().add("premium-table");

        root.getChildren().addAll(
                top,
                localSearchField,
                tripTable
        );

        VBox.setVgrow(tripTable, Priority.ALWAYS);

        return root;
    }

    private static void refreshCalendar() {

        calendarGrid.getChildren().clear();

        monthLabel.setText(
                currentMonth.getMonth().name().substring(0, 1)
                        + currentMonth.getMonth().name().substring(1).toLowerCase()
                        + " "
                        + currentMonth.getYear()
        );

        monthOverviewLabel.setText(monthLabel.getText() + " Overview");

        String[] weekDays = {
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri",
                "Sat",
                "Sun"
        };

        for (int i = 0; i < weekDays.length; i++) {
            Label dayLabel = new Label(weekDays[i]);
            dayLabel.getStyleClass().add("calendar-header");
            dayLabel.setPrefSize(62, 38);
            calendarGrid.add(dayLabel, i, 0);
        }

        LocalDate firstDay = currentMonth.atDay(1);

        int daysInMonth = currentMonth.lengthOfMonth();
        int startDay = firstDay.getDayOfWeek().getValue();

        int row = 1;
        int col = startDay - 1;

        for (int day = 1; day <= daysInMonth; day++) {

            LocalDate date = currentMonth.atDay(day);

            VBox cell = buildCalendarCell(date);

            calendarGrid.add(cell, col, row);

            col++;

            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private static VBox buildCalendarCell(LocalDate date) {

        VBox cell = new VBox(6);

        cell.setAlignment(Pos.TOP_LEFT);
        cell.setPadding(new Insets(8));
        cell.setPrefSize(62, 62);

        cell.getStyleClass().add("calendar-cell");

        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("calendar-today");
        }

        if (date.equals(selectedDate)) {
            cell.getStyleClass().add("calendar-selected");
        }

        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dateLabel.getStyleClass().add("calendar-date");

        long tripCount =
                TripCacheManager.getTripCache()
                        .stream()
                        .filter(trip ->
                                trip.getTripDate() != null
                                        && trip.getTripDate().equals(date)
                        )
                        .count();

        Label countLabel = new Label();

        if (tripCount > 0) {
            countLabel.setText("● " + tripCount);
            countLabel.getStyleClass().add("trip-dot");
        }

        cell.getChildren().add(dateLabel);

        if (tripCount > 0) {
            cell.getChildren().add(countLabel);
        }

        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            loadTripsForDate(date);
            refreshCalendar();
        });

        return cell;
    }

    private static void initializeTable() {

        if (!tripTable.getColumns().isEmpty()) {
            return;
        }

        TableColumn<Trip, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getName())
        );

        TableColumn<Trip, String> sector = new TableColumn<>("Sector");
        sector.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getSector())
        );

        TableColumn<Trip, String> airline = new TableColumn<>("Airline");
        airline.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAirlineName())
        );

        TableColumn<Trip, String> pnr = new TableColumn<>("PNR");
        pnr.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPnrNo())
        );
        TableColumn<Trip, String> bookedBy = new TableColumn<>("Booked By");
        bookedBy.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getBookedBy())
        );

        TableColumn<Trip, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getStatus() == null
                                ? ""
                                : cell.getValue().getStatus().name()
                )
        );

        TableColumn<Trip, Double> sellAmount = new TableColumn<>("Sell Amount");
        sellAmount.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getSellAmount())
        );

        TableColumn<Trip, Void> action = new TableColumn<>("Actions");

        name.setPrefWidth(150);
        sector.setPrefWidth(130);
        airline.setPrefWidth(150);
        pnr.setPrefWidth(120);
        bookedBy.setPrefWidth(100);
        status.setPrefWidth(110);
        sellAmount.setPrefWidth(110);
        action.setPrefWidth(250);

        action.setCellFactory(param -> new TableCell<>() {

            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button viewButton = new Button("View");

            private final HBox box = new HBox(
                    8,
                    editButton,
                    deleteButton,
                    viewButton
            );

            {
                box.setAlignment(Pos.CENTER_LEFT);

                editButton.getStyleClass().add("table-action-button");
                deleteButton.getStyleClass().add("table-action-button");
                viewButton.getStyleClass().add("table-action-button");

                editButton.setOnAction(event -> {

                    Trip trip =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    TripFormDialog.openEditDialog(trip, () -> {
                        loadTripsForDate(selectedDate);
                        refreshCalendar();
                        updateSummaryCards();
                    });
                });

                deleteButton.setOnAction(event -> {

                    Trip trip =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    deleteTrip(trip);
                });

                viewButton.setOnAction(event -> {

                    Trip trip =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    openDocumentListDialog(trip);
                });
            }

            @Override
            protected void updateItem(
                    Void item,
                    boolean empty
            ) {

                super.updateItem(item, empty);

                setGraphic(empty ? null : box);
            }
        });

        tripTable.getColumns().addAll(
                name,
                sector,
                airline,
                pnr,
                bookedBy,
                status,
                sellAmount,
                action
        );

        tripTable.setItems(
                FXCollections.observableArrayList()
        );
    }

    private static void loadTripsForDate(LocalDate date) {

        selectedDateLabel.setText(
                "Trips for "
                        + date.format(
                        DateTimeFormatter.ofPattern("dd MMM yyyy")
                )
        );

        tripTable.getItems().clear();

        tripTable.getItems().addAll(
                TripCacheManager.getTripCache()
                        .stream()
                        .filter(trip ->
                                trip.getTripDate() != null
                                        && trip.getTripDate().equals(date)
                        )
                        .toList()
        );
    }

    private static void updateSummaryCards() {

        var monthlyTrips =
                TripCacheManager.getTripCache()
                        .stream()
                        .filter(trip ->
                                trip.getTripDate() != null
                                        && trip.getTripDate().getMonth() == currentMonth.getMonth()
                                        && trip.getTripDate().getYear() == currentMonth.getYear()
                        )
                        .toList();

        totalTripsLabel.setText(
                String.valueOf(monthlyTrips.size())
        );

        totalSellLabel.setText(
                "₹ "
                        + formatAmount(
                        monthlyTrips.stream()
                                .mapToDouble(Trip::getSellAmount)
                                .sum()
                )
        );

        totalPurchaseLabel.setText(
                "₹ "
                        + formatAmount(
                        monthlyTrips.stream()
                                .mapToDouble(Trip::getPurchaseAmount)
                                .sum()
                )
        );

        totalProfitLabel.setText(
                "₹ "
                        + formatAmount(
                        monthlyTrips.stream()
                                .mapToDouble(Trip::getProfit)
                                .sum()
                )
        );

        long pendingCount =
                monthlyTrips.stream()
                        .filter(trip ->
                                trip.getStatus() != null
                                        && trip.getStatus() == TripStatus.PENDING
                        )
                        .count();

        long cancelledCount =
                monthlyTrips.stream()
                        .filter(trip ->
                                trip.getStatus() != null
                                        && trip.getStatus() == TripStatus.CANCELLED
                        )
                        .count();

        pendingTripsLabel.setText(
                String.valueOf(pendingCount)
        );

        cancelledTripsLabel.setText(
                String.valueOf(cancelledCount)
        );
    }

    private static VBox summaryCard(
            String icon,
            String title,
            Label value
    ) {

        VBox card = new VBox(6);
        card.getStyleClass().add("summary-card");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox =
                new StackPane(
                        icon(icon, 35)
                );

        iconBox.getStyleClass().add("summary-icon");

        VBox textBox = new VBox(3);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("summary-title");

        Label monthText = new Label("This Month");
        monthText.getStyleClass().add("summary-subtitle");

        textBox.getChildren().addAll(
                titleLabel,
                value,
                monthText
        );

        row.getChildren().addAll(
                iconBox,
                textBox
        );

        card.getChildren().add(row);

        // =====================================================
        // CARD CLICK ACTIONS
        // =====================================================

        card.setOnMouseClicked(event -> {

            switch (title.toLowerCase()) {

                case "pending" ->
                        loadTripsByStatus(
                                TripStatus.PENDING
                        );

                case "cancelled" ->
                        loadTripsByStatus(
                                TripStatus.CANCELLED
                        );

                case "total trips" ->
                        loadMonthlyTrips();
            }
        });

        return card;
    }

    private static Label summaryValue(String value) {

        Label label = new Label(value);
        label.getStyleClass().add("summary-value");

        return label;
    }

    private static void searchWithinSelectedDate(String keyword) {

        String search = keyword.toLowerCase();

        tripTable.getItems().clear();

        tripTable.getItems().addAll(
                TripCacheManager.getTripCache()
                        .stream()
                        .filter(trip ->
                                trip.getTripDate() != null
                                        && trip.getTripDate().equals(selectedDate)
                        )
                        .filter(trip -> matchesSearch(trip, search))
                        .toList()
        );
    }

    private static ImageView icon(
            String iconName,
            int size
    ) {

        ImageView imageView =
                new ImageView(
                        new Image(
                                DashboardView.class
                                        .getResourceAsStream(
                                                "/icons/" + iconName
                                        )
                        )
                );

        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);

        return imageView;
    }

    private static void searchGlobally(String keyword) {

        String search = keyword.toLowerCase();

        tripTable.getItems().clear();

        tripTable.getItems().addAll(
                TripCacheManager.getTripCache()
                        .stream()
                        .filter(trip -> matchesSearch(trip, search))
                        .toList()
        );
    }

    private static boolean matchesSearch(
            Trip trip,
            String search
    ) {

        return contains(trip.getName(), search)
                || contains(trip.getSector(), search)
                || contains(trip.getAirlineName(), search)
                || contains(trip.getPnrNo(), search)
                || contains(trip.getBookedBy(), search)
                || contains(
                trip.getStatus() == null
                        ? ""
                        : trip.getStatus().name(),
                search
        );
    }

    private static boolean contains(
            String value,
            String search
    ) {

        return value != null
                && value.toLowerCase().contains(search);
    }

    private static String formatAmount(double value) {

        return String.format("%,.2f", value);
    }

    private static void deleteTrip(Trip trip) {

        Alert confirm =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Trip");
        confirm.setHeaderText("Are you sure you want to delete this trip?");
        confirm.setContentText("This will delete the trip and all linked documents.");

        Optional<ButtonType> result =
                confirm.showAndWait();

        if (result.isEmpty()
                || result.get() != ButtonType.OK) {
            return;
        }

        try {

            TripDocumentRepository documentRepository =
                    new TripDocumentRepository();

            List<TripDocument> documents =
                    documentRepository.findByTripUuid(
                            trip.getId()
                    );

            for (TripDocument document : documents) {

                try {

                    if (document.getFilePath() != null) {
                        Files.deleteIfExists(
                                new File(
                                        document.getFilePath()
                                ).toPath()
                        );
                    }

                } catch (Exception ignored) {
                }
            }

            documentRepository.deleteByTripUuid(
                    trip.getId()
            );

            TripRepository tripRepository =
                    new TripRepository();

            boolean deleted =
                    tripRepository.delete(
                            trip.getId()
                    );

            if (deleted) {

                TripCacheManager.removeTrip(
                        trip.getId()
                );

                loadTripsForDate(selectedDate);
                refreshCalendar();
                updateSummaryCards();

                alert("Trip deleted successfully");
            }

        } catch (Exception e) {

            alert("Failed to delete trip");
        }
    }

    private static void openDocumentListDialog(Trip trip) {

        Stage stage = new Stage();

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Documents - " + trip.getName());

        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("trip-form-root");

        Label title = new Label("Documents");
        title.getStyleClass().add("trip-form-title");

        Label subtitle =
                new Label(
                        "Trip: "
                                + safe(trip.getName())
                                + " | PNR: "
                                + safe(trip.getPnrNo())
                );

        subtitle.getStyleClass().add("trip-form-subtitle");

        TableView<TripDocument> documentTable =
                new TableView<>();

        documentTable.getStyleClass().add("premium-table");

        TableColumn<TripDocument, String> fileNameColumn =
                new TableColumn<>("File Name");

        fileNameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getFileName()
                )
        );

        fileNameColumn.setPrefWidth(330);

        TableColumn<TripDocument, Void> actionColumn =
                new TableColumn<>("Actions");

        actionColumn.setPrefWidth(260);

        actionColumn.setCellFactory(param -> new TableCell<>() {

            private final Button viewButton =
                    new Button("View");

            private final Button downloadButton =
                    new Button("Download");

            private final Button deleteButton =
                    new Button("Delete");

            private final HBox box =
                    new HBox(
                            8,
                            viewButton,
                            downloadButton,
                            deleteButton
                    );

            {
                box.setAlignment(Pos.CENTER_LEFT);

                viewButton.getStyleClass().add("table-action-button");
                downloadButton.getStyleClass().add("table-action-button");
                deleteButton.getStyleClass().add("table-action-button");

                viewButton.setOnAction(event -> {

                    TripDocument document =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    viewDocument(document);
                });

                downloadButton.setOnAction(event -> {

                    TripDocument document =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    downloadDocument(document);
                });

                deleteButton.setOnAction(event -> {

                    TripDocument document =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    deleteDocument(
                            document,
                            documentTable
                    );
                });
            }

            @Override
            protected void updateItem(
                    Void item,
                    boolean empty
            ) {

                super.updateItem(item, empty);

                setGraphic(empty ? null : box);
            }
        });

        documentTable.getColumns().addAll(
                fileNameColumn,
                actionColumn
        );

        TripDocumentRepository repository =
                new TripDocumentRepository();

        documentTable.setItems(
                FXCollections.observableArrayList(
                        repository.findByTripUuid(
                                trip.getId()
                        )
                )
        );

        VBox.setVgrow(
                documentTable,
                Priority.ALWAYS
        );

        Button closeButton =
                new Button("Close");

        closeButton.getStyleClass()
                .add("secondary-button");

        closeButton.setOnAction(e ->
                stage.close()
        );

        HBox footer =
                new HBox(closeButton);

        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                title,
                subtitle,
                documentTable,
                footer
        );

        Scene scene =
                new Scene(
                        root,
                        720,
                        520
                );

        scene.getStylesheets().add(
                DashboardView.class
                        .getResource("/css/app.css")
                        .toExternalForm()
        );

        stage.setScene(scene);
        stage.showAndWait();
    }

    private static void viewDocument(
            TripDocument document
    ) {

        try {

            if (document.getFilePath() == null
                    || document.getFilePath().isBlank()) {

                alert("Document path not found");
                return;
            }

            File file =
                    new File(
                            document.getFilePath()
                    );

            if (!file.exists()) {

                alert("Document not found");
                return;
            }

            Desktop.getDesktop().open(file);

        } catch (Exception e) {

            alert("Failed to open document");
        }
    }

    private static void downloadDocument(
            TripDocument document
    ) {

        try {

            if (document.getFilePath() == null
                    || document.getFilePath().isBlank()) {

                alert("Document path not found");
                return;
            }

            File source =
                    new File(
                            document.getFilePath()
                    );

            if (!source.exists()) {

                alert("Document not found");
                return;
            }

            FileChooser chooser =
                    new FileChooser();

            chooser.setInitialFileName(
                    document.getFileName()
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

        } catch (Exception e) {

            alert("Failed to download document");
        }
    }

    private static void deleteDocument(
            TripDocument document,
            TableView<TripDocument> documentTable
    ) {

        Alert confirm =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Document");
        confirm.setHeaderText("Are you sure you want to delete this document?");
        confirm.setContentText(document.getFileName());

        Optional<ButtonType> result =
                confirm.showAndWait();

        if (result.isEmpty()
                || result.get() != ButtonType.OK) {
            return;
        }

        try {

            TripDocumentRepository repository =
                    new TripDocumentRepository();

            boolean deleted =
                    repository.deleteByUuid(
                            document.getUuid()
                    );

            if (deleted) {

                if (document.getFilePath() != null) {

                    Files.deleteIfExists(
                            new File(
                                    document.getFilePath()
                            ).toPath()
                    );
                }

                documentTable.getItems()
                        .remove(document);

                alert("Document deleted successfully");
            }

        } catch (Exception e) {

            alert("Failed to delete document");
        }
    }

    private static void loadTripsByStatus(
            TripStatus status
    ) {

        selectedDateLabel.setText(
                status.name() + " Trips - "
                        + currentMonth.getMonth().name().substring(0, 1)
                        + currentMonth.getMonth().name().substring(1).toLowerCase()
                        + " "
                        + currentMonth.getYear()
        );

        tripTable.getItems().clear();

        tripTable.getItems().addAll(
                TripCacheManager.getTripCache()
                        .stream()
                        .filter(trip ->
                                trip.getTripDate() != null
                                        && trip.getTripDate().getMonth() == currentMonth.getMonth()
                                        && trip.getTripDate().getYear() == currentMonth.getYear()
                        )
                        .filter(trip ->
                                trip.getStatus() != null
                                        && trip.getStatus() == status
                        )
                        .toList()
        );
    }

    private static void loadMonthlyTrips() {

        selectedDateLabel.setText(
                "All Trips - "
                        + currentMonth.getMonth().name().substring(0, 1)
                        + currentMonth.getMonth().name().substring(1).toLowerCase()
                        + " "
                        + currentMonth.getYear()
        );

        tripTable.getItems().clear();

        tripTable.getItems().addAll(
                TripCacheManager.getTripCache()
                        .stream()
                        .filter(trip ->
                                trip.getTripDate() != null
                                        && trip.getTripDate().getMonth() == currentMonth.getMonth()
                                        && trip.getTripDate().getYear() == currentMonth.getYear()
                        )
                        .toList()
        );
    }
    private static String safe(String value) {

        return value == null || value.isBlank()
                ? "-"
                : value;
    }

    private static void alert(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setContentText(message);

        alert.showAndWait();
    }
}