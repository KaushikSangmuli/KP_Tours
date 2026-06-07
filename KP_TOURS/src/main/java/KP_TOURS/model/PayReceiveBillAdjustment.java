package KP_TOURS.model;

import java.time.LocalDateTime;


public class PayReceiveBillAdjustment {

    private String uuid;
    private String payReceiveUuid;
    private String purchaseSalesUuid;
    private String billNo;
    private double adjustedAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getPayReceiveUuid() { return payReceiveUuid; }
    public void setPayReceiveUuid(String payReceiveUuid) { this.payReceiveUuid = payReceiveUuid; }

    public String getPurchaseSalesUuid() { return purchaseSalesUuid; }
    public void setPurchaseSalesUuid(String purchaseSalesUuid) { this.purchaseSalesUuid = purchaseSalesUuid; }

    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }

    public double getAdjustedAmount() { return adjustedAmount; }
    public void setAdjustedAmount(double adjustedAmount) { this.adjustedAmount = adjustedAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}