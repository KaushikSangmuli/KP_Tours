package KP_TOURS.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Trip {

    private final String id;

    private LocalDate tripDate;

    private String naam;
    private String sector;
    private String airlineName;

    private double sellAmount;
    private double purchaseAmount;
    private double profit;

    private String bookedBy;
    private String pnrNo;

    private TripStatus status;

    private String documentPath;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Trip() {

        this.id = UUID.randomUUID().toString();

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        this.status = TripStatus.PENDING;
    }

    // =========================================================
    // Business Methods
    // =========================================================

    public void calculateProfit() {
        this.profit = this.sellAmount - this.purchaseAmount;
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================
    // Getters and Setters
    // =========================================================

    public String getId() {
        return id;
    }

    public LocalDate getTripDate() {
        return tripDate;
    }

    public void setTripDate(LocalDate tripDate) {
        this.tripDate = tripDate;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
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

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // =========================================================
    // Utility Methods
    // =========================================================

    @Override
    public String toString() {
        return "Trip{" +
                "id='" + id + '\'' +
                ", naam='" + naam + '\'' +
                ", sector='" + sector + '\'' +
                ", airlineName='" + airlineName + '\'' +
                ", tripDate=" + tripDate +
                ", status=" + status +
                '}';
    }
}