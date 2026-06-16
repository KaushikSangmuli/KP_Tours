package KP_TOURS.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreditNote {

    private String uuid;
    private String creditNoteNo;
    private LocalDate entryDate;

    // Linked bill
    private String purchaseSalesUuid;
    private String billNo;
    private LocalDate billDate;

    // Accounts
    private String customerUuid;
    private String creditorUuid;

    // Bill details (auto-filled, read-only in form)
    private int    qty;
    private double rate;
    private double purchaseAmount;
    private double amount;          // totalSale from PS

    // Refund amounts (editable)
    private double bankRefund;
    private double partyRefund;
    private double diff;            // amount - partyRefund

    // Meta
    private String ticketNo;
    private String remark;
    private String particulars;
    private String paymentMode;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CreditNote() {
        this.uuid      = UUID.randomUUID().toString();
        this.entryDate = LocalDate.now();
        this.qty       = 1;
        this.status    = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateDiff() {
        this.diff = this.amount - this.partyRefund;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCreditNoteNo() { return creditNoteNo; }
    public void setCreditNoteNo(String creditNoteNo) { this.creditNoteNo = creditNoteNo; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public String getPurchaseSalesUuid() { return purchaseSalesUuid; }
    public void setPurchaseSalesUuid(String uuid) { this.purchaseSalesUuid = uuid; }

    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }

    public LocalDate getBillDate() { return billDate; }
    public void setBillDate(LocalDate billDate) { this.billDate = billDate; }

    public String getCustomerUuid() { return customerUuid; }
    public void setCustomerUuid(String customerUuid) { this.customerUuid = customerUuid; }

    public String getCreditorUuid() { return creditorUuid; }
    public void setCreditorUuid(String creditorUuid) { this.creditorUuid = creditorUuid; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }

    public double getPurchaseAmount() { return purchaseAmount; }
    public void setPurchaseAmount(double purchaseAmount) { this.purchaseAmount = purchaseAmount; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; calculateDiff(); }

    public double getBankRefund() { return bankRefund; }
    public void setBankRefund(double bankRefund) { this.bankRefund = bankRefund; }

    public double getPartyRefund() { return partyRefund; }
    public void setPartyRefund(double partyRefund) { this.partyRefund = partyRefund; calculateDiff(); }

    public double getDiff() { return diff; }

    public String getTicketNo() { return ticketNo; }
    public void setTicketNo(String ticketNo) { this.ticketNo = ticketNo; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getParticulars() { return particulars; }
    public void setParticulars(String particulars) { this.particulars = particulars; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}