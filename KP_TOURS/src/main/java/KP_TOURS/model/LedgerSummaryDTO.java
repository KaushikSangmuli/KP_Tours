package KP_TOURS.model;

import java.util.ArrayList;
import java.util.List;

public class LedgerSummaryDTO {

    private double outstanding;

    private double totalProfit;

    private double totalDebit;
    private double totalCredit;
    private double closingBalance;

    private List<LedgerRow> rows =
            new ArrayList<>();

    public double getOutstanding() {
        return outstanding;
    }

    public double getTotalCredit(){
        return totalCredit;
    }

    public double getTotalDebit() {
        return totalDebit;
    }

    public double getClosingBalance() {
        return closingBalance;
    }

    public void setTotalCredit(double totalCredit) {
        this.totalCredit = totalCredit;
    }

    public void setClosingBalance(double closingBalance) {
        this.closingBalance = closingBalance;
    }

    public void setTotalDebit(double totalDebit) {
        this.totalDebit = totalDebit;
    }

    public void setOutstanding(double outstanding) {
        this.outstanding = outstanding;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public List<LedgerRow> getRows() {
        return rows;
    }

    public void setRows(List<LedgerRow> rows) {
        this.rows = rows;
    }
}