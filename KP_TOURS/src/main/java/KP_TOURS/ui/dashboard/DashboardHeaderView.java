package KP_TOURS.ui.dashboard;

import KP_TOURS.backup.BackupManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class DashboardHeaderView {


    public static final Label totalTripsLabel = summaryValue("0");
    public static final Label totalSellLabel = summaryValue("₹ 0.00");
    public static final Label totalPurchaseLabel = summaryValue("₹ 0.00");
    public static final Label totalProfitLabel = summaryValue("₹ 0.00");
    public static final Label pendingTripsLabel = summaryValue("0");
    public static final Label cancelledTripsLabel = summaryValue("0");

    private DashboardHeaderView() {
    }

    public static VBox getView() {

        VBox wrapper = new VBox(22);
        wrapper.getStyleClass().add("premium-header");

        HBox titleRow = new HBox(18);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        DashboardView.monthOverviewLabel
                .getStyleClass()
                .add("month-overview");

        Button prevButton = new Button("‹ Previous");
        prevButton.getStyleClass().add("header-button");

        prevButton.setOnAction(e -> {

            DashboardView.currentMonth =
                    DashboardView.currentMonth.minusMonths(1);

            DashboardView.refreshCalendar();

            DashboardView.updateSummaryCards();
        });

        Button nextButton = new Button("Next ›");
        nextButton.getStyleClass().add("header-button");

        nextButton.setOnAction(e -> {

            DashboardView.currentMonth =
                    DashboardView.currentMonth.plusMonths(1);

            DashboardView.refreshCalendar();

            DashboardView.updateSummaryCards();
        });

        Button backupButton = new Button("Backup");
        backupButton.getStyleClass().add("header-button");

        backupButton.setOnAction(e ->
                BackupManager.createBackup()
        );

        Button restoreButton = new Button("Restore");
        restoreButton.getStyleClass().add("header-button");

        restoreButton.setOnAction(e -> {

            BackupManager.restoreBackup();

            DashboardView.loadTripsForDate(
                    DashboardView.selectedDate
            );

            DashboardView.refreshCalendar();

            DashboardView.updateSummaryCards();
        });

        titleRow.getChildren().addAll(
                spacer,
                DashboardView.monthOverviewLabel,
                prevButton,
                nextButton,
                backupButton,
                restoreButton
        );

        GridPane cardGrid = new GridPane();

        cardGrid.setHgap(16);
        cardGrid.setVgap(16);

        cardGrid.getStyleClass().add("summary-grid");

        VBox card1 =
                DashboardView.summaryCard(
                        "trip.png",
                        "Total Trips",
                        totalTripsLabel
                );

        VBox card2 =
                DashboardView.summaryCard(
                        "money.png",
                        "Total Sell",
                        totalSellLabel
                );

        VBox card3 =
                DashboardView.summaryCard(
                        "purchase.png",
                        "Total Purchase",
                        totalPurchaseLabel
                );

        VBox card4 =
                DashboardView.summaryCardWithToggle(
                        "profit.png",
                        "Total Profit",
                        totalProfitLabel
                );

        VBox card5 =
                DashboardView.summaryCard(
                        "pending.png",
                        "Pending",
                        pendingTripsLabel
                );

        VBox card6 =
                DashboardView.summaryCard(
                        "cancelled.png",
                        "Cancelled",
                        cancelledTripsLabel
                );

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

                ColumnConstraints col =
                        new ColumnConstraints();

                col.setPercentWidth(
                        100.0 / columns
                );

                col.setHgrow(Priority.ALWAYS);

                cardGrid.getColumnConstraints()
                        .add(col);
            }

            for (int i = 0; i < cards.length; i++) {

                cardGrid.add(
                        cards[i],
                        i % columns,
                        i / columns
                );
            }
        };

        wrapper.widthProperty().addListener(
                (obs, oldVal, newVal) ->
                        refreshGrid.run()
        );

        refreshGrid.run();

        wrapper.getChildren().addAll(
                titleRow,
                cardGrid
        );

        return wrapper;
    }

    private static Label summaryValue(String value) {

        Label label = new Label(value);
        label.getStyleClass().add("summary-value");

        return label;
    }
}