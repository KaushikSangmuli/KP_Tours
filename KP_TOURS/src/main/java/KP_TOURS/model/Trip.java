package KP_TOURS.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Trip {

    private Integer id;
    private String uuid;
    private String purchaseSalesUuid;

    private LocalDate tripDate;

    private String name;
    private String sector;
    private String airlineName;

    private double sellAmount;
    private double purchaseAmount;
    private double profit;

    private String bookedBy;
    private String pnrNo;

    private TripStatus status;

    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Trip() {
        this.uuid = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = TripStatus.PENDING;
    }

    public void calculateProfit() {
        this.profit = this.sellAmount - this.purchaseAmount;
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPurchaseSalesUuid() {
        return purchaseSalesUuid;
    }

    public void setPurchaseSalesUuid(String purchaseSalesUuid) {
        this.purchaseSalesUuid = purchaseSalesUuid;
    }

    public LocalDate getTripDate() {
        return tripDate;
    }

    public void setTripDate(LocalDate tripDate) {
        this.tripDate = tripDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(double sellAmount) {
        this.sellAmount = sellAmount;
        calculateProfit();
    }

    public double getPurchaseAmount() {
        return purchaseAmount;
    }

    public void setPurchaseAmount(double purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
        calculateProfit();
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public String getPnrNo() {
        return pnrNo;
    }

    public void setPnrNo(String pnrNo) {
        this.pnrNo = pnrNo;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        return "Trip{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", sector='" + sector + '\'' +
                ", airlineName='" + airlineName + '\'' +
                ", tripDate=" + tripDate +
                ", status=" + status +
                '}';
    }
}