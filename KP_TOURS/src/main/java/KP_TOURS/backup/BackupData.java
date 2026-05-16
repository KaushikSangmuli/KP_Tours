package KP_TOURS.backup;

import java.util.ArrayList;
import java.util.List;

public class BackupData {

    private List<TripBackupData> trips = new ArrayList<>();
    private List<DocumentBackupData> documents = new ArrayList<>();

    public List<TripBackupData> getTrips() {
        return trips;
    }

    public void setTrips(List<TripBackupData> trips) {
        this.trips = trips;
    }

    public List<DocumentBackupData> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentBackupData> documents) {
        this.documents = documents;
    }

    public static class TripBackupData {

        private String id;
        private String tripDate;
        private String name;
        private String sector;
        private String airlineName;
        private double sellAmount;
        private double purchaseAmount;
        private double profit;
        private String bookedBy;
        private String pnrNo;
        private String status;
        private String description;
        private String createdAt;
        private String updatedAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTripDate() {
            return tripDate;
        }

        public void setTripDate(String tripDate) {
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
        }

        public double getPurchaseAmount() {
            return purchaseAmount;
        }

        public void setPurchaseAmount(double purchaseAmount) {
            this.purchaseAmount = purchaseAmount;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class DocumentBackupData {

        private String uuid;
        private String tripUuid;
        private String fileName;
        private String filePath;
        private String createdAt;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getTripUuid() {
            return tripUuid;
        }

        public void setTripUuid(String tripUuid) {
            this.tripUuid = tripUuid;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}