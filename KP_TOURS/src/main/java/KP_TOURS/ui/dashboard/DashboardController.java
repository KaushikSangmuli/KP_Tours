
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.YearMonth;

public class DashboardController {

    // =========================================================
    // HEADER LABELS
    // =========================================================

    @FXML
    private Label totalTripsLabel;

    @FXML
    private Label totalSellLabel;

    @FXML
    private Label totalPurchaseLabel;

    @FXML
    private Label totalProfitLabel;

    @FXML
    private Label pendingTripsLabel;

    @FXML
    private Label cancelledTripsLabel;

    // =========================================================
    // CALENDAR SECTION
    // =========================================================

    @FXML
    private Label monthYearLabel;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;



    @FXML
    private Button addTripButton;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Trip> tripTable;

    @FXML
    private TableColumn<Trip, String> naamColumn;

    @FXML
    private TableColumn<Trip, String> sectorColumn;

    @FXML
    private TableColumn<Trip, String> airlineColumn;

    @FXML
    private TableColumn<Trip, String> pnrColumn;

    @FXML
    private TableColumn<Trip, String> statusColumn;

    @FXML
    private TableColumn<Trip, Double> profitColumn;

    @FXML
    private TableColumn<Trip, Void> actionColumn;

    // =========================================================
    // STATE
    // =========================================================

    private YearMonth currentMonth;

    private LocalDate selectedDate;

    // =========================================================
    // INITIALIZATION
    // =========================================================

    @FXML
    public void initialize() {

        currentMonth = YearMonth.now();

        selectedDate = LocalDate.now();

        initializeTable();

        refreshCalendar();

        refreshSummary();

        loadTripsForSelectedDate();
    }



    refreshSummary();
}

@FXML
private void onNextMonth() {

    currentMonth = currentMonth.plusMonths(1);

    refreshCalendar();

    refreshSummary();
}

// =========================================================
// TRIP ACTIONS
// =========================================================

@FXML
private void onAddTrip() {

    System.out.println("Open Add Trip Form");
}

@FXML
private void onSearch() {

    String keyword = searchField.getText();

    System.out.println("Searching: " + keyword);
}

// =========================================================
// UI REFRESH
// =========================================================

private void refreshCalendar() {

    monthYearLabel.setText(
            currentMonth.getMonth().name() +
                    " " +
                    currentMonth.getYear()
    );

    calendarGrid.getChildren().clear();

    // Calendar generation logic will come here
}


private void refreshSummary() {

    // Summary calculation logic

    totalTripsLabel.setText("0");
    totalSellLabel.setText("0");
    totalPurchaseLabel.setText("0");
    totalProfitLabel.setText("0");
    pendingTripsLabel.setText("0");
    cancelledTripsLabel.setText("0");
}

private void loadTripsForSelectedDate() {

    selectedDateLabel.setText(
            "Trips - " + selectedDate
    );

    // Table loading logic
}


private void initializeTable() {

    naamColumn.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getNaam()
            )
    );

    sectorColumn.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getSector()
            )
    );

    airlineColumn.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getAirlineName()
            )
    );

    pnrColumn.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getPnrNo()
            )
    );

    statusColumn.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getStatus().name()
            )
    );

    profitColumn.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleObjectProperty<>(
                    cell.getValue().getProfit()
            )
    );
}
}