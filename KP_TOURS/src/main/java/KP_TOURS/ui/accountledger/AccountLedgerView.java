package KP_TOURS.ui.accountledger;

import KP_TOURS.model.Account;
import KP_TOURS.model.LedgerRow;
import KP_TOURS.model.LedgerSummaryDTO;
import KP_TOURS.repository.AccountLedgerRepository;
import KP_TOURS.repository.AccountRepository;
import KP_TOURS.ui.dashboard.DashboardView;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

public class AccountLedgerView {

    private final AccountRepository accountRepository =
            new AccountRepository();

    private final AccountLedgerRepository ledgerRepository =
            new AccountLedgerRepository();

    private ComboBox<Account> accountCombo;

    private DatePicker fromDatePicker;

    private DatePicker toDatePicker;

    private Label outstandingLabel;

    private Label profitLabel;

    private Label totalDebitLabel;
    private Label totalCreditLabel;
    private Label closingBalanceLabel;

    private TableView<LedgerRow> table;

    public static Parent getView() {

        return new AccountLedgerView()
                .buildView();
    }

    private Parent buildView() {

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("main-content");

        Label title =
                new Label("Account Ledger");

        title.getStyleClass().add("section-title");

        HBox filterRow =
                createFilterRow();

        table =
                createTable();

        HBox footer =
                createFooter();

        VBox tableCard =
                new VBox(
                        10,
                        table,
                        footer
                );

        tableCard.getStyleClass()
                .add("premium-panel");

        tableCard.setPadding(
                new Insets(15)
        );

        root.getChildren().addAll(
                title,
                filterRow,
                tableCard
        );

        ScrollPane scrollPane =
                new ScrollPane(root);

        scrollPane.setFitToWidth(true);

        return scrollPane;
    }

    private HBox createFilterRow() {

        HBox row =
                new HBox(15);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        accountCombo =
                new ComboBox<>();

        accountCombo.setPromptText(
                "Select Account"
        );

        accountCombo.setPrefWidth(
                280
        );

        accountCombo.setItems(
                FXCollections.observableArrayList(
                        accountRepository.findAll()
                )
        );

        fromDatePicker =
                new DatePicker(
                        LocalDate.now().minusMonths(1)
                );

        toDatePicker =
                new DatePicker(
                        LocalDate.now()
                );

        outstandingLabel =
                new Label("Outstanding ₹0");

        outstandingLabel.setStyle(
                "-fx-font-size:14px;" +
                        "-fx-font-weight:bold;"
        );

        profitLabel =
                new Label("Profit ₹0");

        profitLabel.setStyle(
                "-fx-font-size:14px;" +
                        "-fx-font-weight:bold;"
        );

        Button loadButton =
                new Button("Load");

        loadButton.getStyleClass()
                .add("primary-button");

        loadButton.setOnAction(
                e -> loadLedger()
        );

        row.getChildren().addAll(
                new Label("Account"),
                accountCombo,
                new Label("From"),
                fromDatePicker,
                new Label("To"),
                toDatePicker,
                outstandingLabel,
                profitLabel,
                loadButton
        );

        return row;
    }

    private TableView<LedgerRow> createTable() {

        TableView<LedgerRow> tableView =
                new TableView<>();

        tableView.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<LedgerRow, String> dateCol =
                new TableColumn<>("Date");

        dateCol.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getDate()
                )
        );

        TableColumn<LedgerRow, String> particularsCol =
                new TableColumn<>("Particulars");

        particularsCol.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getParticulars()
                )
        );

        TableColumn<LedgerRow, Number> debitCol =
                new TableColumn<>("Debit");

        debitCol.setCellValueFactory(
                data -> new SimpleDoubleProperty(
                        data.getValue().getDebit()
                )
        );

        TableColumn<LedgerRow, Number> creditCol =
                new TableColumn<>("Credit");

        creditCol.setCellValueFactory(
                data -> new SimpleDoubleProperty(
                        data.getValue().getCredit()
                )
        );

        TableColumn<LedgerRow, String> remarkCol =
                new TableColumn<>("Remark");

        remarkCol.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getRemark()
                )
        );

        TableColumn<LedgerRow, String> voucherTypeCol =
                new TableColumn<>("Voucher Type");

        voucherTypeCol.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getVoucherType()
                )
        );

        TableColumn<LedgerRow, String> voucherNoCol =
                new TableColumn<>("Voucher No");

        voucherNoCol.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getVoucherNo()
                )
        );

        TableColumn<LedgerRow, Number> balanceCol =
                new TableColumn<>("Balance");

        balanceCol.setCellValueFactory(
                data -> new SimpleDoubleProperty(
                        data.getValue().getRunningBalance()
                )
        );

        tableView.getColumns().addAll(
                dateCol,
                particularsCol,
                debitCol,
                creditCol,
                remarkCol,
                voucherTypeCol,
                voucherNoCol,
                balanceCol
        );

        return tableView;
    }

    private void loadLedger() {

        Account account =
                accountCombo.getValue();

        if (account == null) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.WARNING,
                            "Please select account."
                    );

            alert.showAndWait();
            return;
        }

        LedgerSummaryDTO summary =
                ledgerRepository.generateLedger(
                        account.getUuid(),
                        account.getAccountGroup(),
                        fromDatePicker.getValue(),
                        toDatePicker.getValue()
                );

        outstandingLabel.setText(
                "Outstanding ₹"
                        + String.format(
                        "%,.2f",
                        summary.getOutstanding()
                )
        );

        profitLabel.setText(
                "Profit ₹"
                        + String.format(
                        "%,.2f",
                        summary.getTotalProfit()
                )
        );

        totalDebitLabel.setText(
                "Debit ₹" +
                        String.format(
                                "%,.2f",
                                summary.getTotalDebit()
                        )
        );

        totalCreditLabel.setText(
                "Credit ₹" +
                        String.format(
                                "%,.2f",
                                summary.getTotalCredit()
                        )
        );

        closingBalanceLabel.setText(
                "Closing ₹" +
                        String.format(
                                "%,.2f",
                                summary.getClosingBalance()
                        )
        );

        List<LedgerRow> rows =
                summary.getRows();

        table.setItems(
                FXCollections.observableArrayList(
                        rows
                )
        );
    }

    private HBox createFooter() {

        totalDebitLabel =
                new Label("Debit ₹0");

        totalCreditLabel =
                new Label("Credit ₹0");

        closingBalanceLabel =
                new Label("Closing ₹0");

        totalDebitLabel.getStyleClass()
                .add("metric-chip");

        totalCreditLabel.getStyleClass()
                .add("metric-chip");

        closingBalanceLabel.getStyleClass()
                .add("metric-chip-success");

        HBox footer =
                new HBox(
                        15,
                        totalDebitLabel,
                        totalCreditLabel,
                        closingBalanceLabel
                );

        footer.setAlignment(Pos.CENTER_RIGHT);

        return footer;
    }
}