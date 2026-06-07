package KP_TOURS.model;

public class OutstandingBill {

    private String purchaseSalesUuid;

    private String billNo;

    private String accountUuid;

    private String accountName;

    private double billAmount;

    private double adjustedAmount;

    private double outstandingAmount;

    private String status;

    private double tempAdjustAmount;

    public OutstandingBill() {
    }

    public OutstandingBill(
            String purchaseSalesUuid,
            String billNo,
            String accountUuid,
            String accountName,
            double billAmount,
            double adjustedAmount,
            double outstandingAmount,
            String status,
            double tempAdjustAmount
    ) {
        this.purchaseSalesUuid = purchaseSalesUuid;
        this.billNo = billNo;
        this.accountUuid = accountUuid;
        this.accountName = accountName;
        this.billAmount = billAmount;
        this.adjustedAmount = adjustedAmount;
        this.outstandingAmount = outstandingAmount;
        this.status = status;
        this.tempAdjustAmount=tempAdjustAmount;
    }

    public String getPurchaseSalesUuid() {
        return purchaseSalesUuid;
    }

    public void setPurchaseSalesUuid(String purchaseSalesUuid) {
        this.purchaseSalesUuid = purchaseSalesUuid;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getAccountUuid() {
        return accountUuid;
    }

    public void setAccountUuid(String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public double getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(double billAmount) {
        this.billAmount = billAmount;
    }

    public double getAdjustedAmount() {
        return adjustedAmount;
    }

    public void setAdjustedAmount(double adjustedAmount) {
        this.adjustedAmount = adjustedAmount;
    }

    public double getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(double outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTempAdjustAmount() {
        return tempAdjustAmount;
    }

    public void setTempAdjustAmount(double tempAdjustAmount) {
        this.tempAdjustAmount = tempAdjustAmount;
    }

    @Override
    public String toString() {
        return "OutstandingBill{" +
                "purchaseSalesUuid='" + purchaseSalesUuid + '\'' +
                ", billNo='" + billNo + '\'' +
                ", accountUuid='" + accountUuid + '\'' +
                ", accountName='" + accountName + '\'' +
                ", billAmount=" + billAmount +
                ", adjustedAmount=" + adjustedAmount +
                ", outstandingAmount=" + outstandingAmount +
                ", status='" + status + '\'' +
                '}';
    }
}