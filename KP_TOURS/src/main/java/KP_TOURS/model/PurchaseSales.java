package KP_TOURS.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PurchaseSales {

    private String uuid;
    private String billNo;

    private LocalDate entryDate;

    private String purchaseType;
    private String purchaseFrom;
    private String customerUuid;

    private String description;
    private String purchaseRemark;
    private String salesRemark;

    private int qty;

    private double purchaseRate;
    private double sellRate;

    private double totalPurchase;
    private double totalSale;
    private double profit;

    private String paymentMode;

    private String pnrNo;
    private String sector;
    private String airlineName;
    private LocalDate travelDate;

    private String linkedTripUuid;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PurchaseSales() {
        this.uuid = UUID.randomUUID().toString();
        this.entryDate = LocalDate.now();
        this.qty = 1;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateTotals() {
        this.totalPurchase = this.qty * this.purchaseRate;
        this.totalSale = this.qty * this.sellRate;
        this.profit = this.totalSale - this.totalPurchase;
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public String getPurchaseFrom() {
        return purchaseFrom;
    }

    public void setPurchaseFrom(String purchaseFrom) {
        this.purchaseFrom = purchaseFrom;
    }

    public String getCustomerUuid() {
        return customerUuid;
    }

    public void setCustomerUuid(String customerUuid) {
        this.customerUuid = customerUuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPurchaseRemark() {
        return purchaseRemark;
    }

    public void setPurchaseRemark(String purchaseRemark) {
        this.purchaseRemark = purchaseRemark;
    }

    public String getSalesRemark() {
        return salesRemark;
    }

    public void setSalesRemark(String salesRemark) {
        this.salesRemark = salesRemark;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
        calculateTotals();
    }

    public double getPurchaseRate() {
        return purchaseRate;
    }

    public void setPurchaseRate(double purchaseRate) {
        this.purchaseRate = purchaseRate;
        calculateTotals();
    }

    public double getSellRate() {
        return sellRate;
    }

    public void setSellRate(double sellRate) {
        this.sellRate = sellRate;
        calculateTotals();
    }

    public double getTotalPurchase() {
        return totalPurchase;
    }

    public double getTotalSale() {
        return totalSale;
    }

    public double getProfit() {
        return profit;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPnrNo() {
        return pnrNo;
    }

    public void setPnrNo(String pnrNo) {
        this.pnrNo = pnrNo;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public String getLinkedTripUuid() {
        return linkedTripUuid;
    }

    public void setLinkedTripUuid(String linkedTripUuid) {
        this.linkedTripUuid = linkedTripUuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}