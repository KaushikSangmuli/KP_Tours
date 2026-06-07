package KP_TOURS.ui.paymentsreceive;

import KP_TOURS.model.PayReceive;
import KP_TOURS.repository.PayReceiveRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class PayReceiveViewList {

    private final PayReceiveRepository repository =
            new PayReceiveRepository();

    private TableView<PayReceive> table;
    private TextField searchField;

    public static Parent getView() {
        return new PayReceiveViewList().buildView();
    }

    private Parent buildView() {

        VBox root = new VBox(16);
        root.setPadding(new Insets(22));
        root.getStyleClass().add("main-content");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);

        Label title = new Label("Pay / Receive Vouchers");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("View and cancel payment / receipt vouchers.");
        subtitle.getStyleClass().add("section-subtitle");

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("secondary-button");

        backBtn.setOnAction(e ->
                DashboardView.loadScreen(PayReceiveView.getView())
        );

        header.getChildren().addAll(
                titleBox,
                spacer,
                backBtn
        );

        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.getStyleClass().add("premium-panel");
        searchBar.setPadding(new Insets(16));

        searchField = new TextField();
        searchField.setPromptText("Search voucher no, type, payment mode, status...");
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

    private TableView<PayReceive> createTable() {

        TableView<PayReceive> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(620);

        TableColumn<PayReceive, String> voucherNoCol =
                new TableColumn<>("Voucher No");

        voucherNoCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getVoucherNo())
        );

        TableColumn<PayReceive, String> dateCol =
                new TableColumn<>("Date");

        dateCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getEntryDate() != null
                                ? data.getValue().getEntryDate().toString()
                                : ""
                )
        );

        TableColumn<PayReceive, String> typeCol =
                new TableColumn<>("Type");

        typeCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getEntryType() != null
                                ? data.getValue().getEntryType().name()
                                : ""
                )
        );

        TableColumn<PayReceive, String> paymentModeCol =
                new TableColumn<>("Payment Mode");

        paymentModeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPaymentMode())
        );

        TableColumn<PayReceive, Number> amountCol =
                new TableColumn<>("Amount");

        amountCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getTotalAmount())
        );

        TableColumn<PayReceive, String> statusCol =
                new TableColumn<>("Status");

        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getStatus() == null
                                ? "ACTIVE"
                                : data.getValue().getStatus()
                )
        );

        TableColumn<PayReceive, Void> actionCol =
                new TableColumn<>("Actions");

        actionCol.setCellFactory(col -> new TableCell<>() {

            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(8, cancelBtn);

            {
                box.setAlignment(Pos.CENTER);
                cancelBtn.getStyleClass().add("danger-button");

                cancelBtn.setOnAction(e -> {
                    PayReceive item =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    cancelVoucher(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                PayReceive voucher =
                        getTableView()
                                .getItems()
                                .get(getIndex());

                String status =
                        voucher.getStatus() == null
                                ? "ACTIVE"
                                : voucher.getStatus();

                cancelBtn.setDisable(
                        "CANCELLED".equalsIgnoreCase(status)
                );

                setGraphic(box);
            }
        });

        tableView.getColumns().addAll(
                voucherNoCol,
                dateCol,
                typeCol,
                paymentModeCol,
                amountCol,
                statusCol,
                actionCol
        );

        return tableView;
    }

    private void loadEntries() {

        List<PayReceive> entries =
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

    private void cancelVoucher(PayReceive item) {

        if (item == null) {
            return;
        }

        String status =
                item.getStatus() == null
                        ? "ACTIVE"
                        : item.getStatus();

        if ("CANCELLED".equalsIgnoreCase(status)) {
            alert("This voucher is already cancelled.");
            return;
        }

        Alert confirm =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Cancel Voucher");

        confirm.setHeaderText(
                "Cancel Voucher: " + item.getVoucherNo()
        );

        confirm.setContentText(
                "Are you sure you want to cancel this voucher?\n" +
                        "This voucher will stop affecting outstanding balances."
        );

        confirm.showAndWait().ifPresent(result -> {

            if (result != ButtonType.OK) {
                return;
            }

            boolean cancelled =
                    repository.cancelVoucher(
                            item.getUuid()
                    );

            if (cancelled) {
                alert("Voucher cancelled successfully.");
                loadEntries();
            } else {
                alert("Failed to cancel voucher.");
            }
        });
    }

    private void alert(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}