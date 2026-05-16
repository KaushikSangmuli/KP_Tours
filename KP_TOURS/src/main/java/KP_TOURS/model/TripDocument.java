package KP_TOURS.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TripDocument {

    private String uuid;

    private String tripUuid;

    private String fileName;

    private String filePath;

    private LocalDateTime createdAt;

    public TripDocument() {

        this.uuid =
                UUID.randomUUID().toString();

        this.createdAt =
                LocalDateTime.now();
    }

    public String getUuid() {
        return uuid;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {

        return "TripDocument{" +
                "uuid='" + uuid + '\'' +
                ", tripUuid='" + tripUuid + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}